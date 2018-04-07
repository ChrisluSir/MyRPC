package com.chris.rpc.server;

import com.chris.rpc.common.RpcDecoder;
import com.chris.rpc.common.RpcEncoder;
import com.chris.rpc.common.RpcRequest;
import com.chris.rpc.common.RpcResponse;
import com.chris.rpc.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * 框架的RPC服务器 (用于将用户系统的业务类发布为RPC服务)
 * 使用时可有用户通过spring-bean的方式注入到用户的业务系统中
 * 由于本类实现了ApplcationContextAware和InitalizingBean
 * spring构造本对象时会调用setApplicationContext()方法，从而可以在方法中通过自定义注解获得用户的业务接口和实现
 * 还会调用afterPropertiesSet()方法，在方法中启动netty服务器
 * RpcServer
 * author: Chris
 * time：2018.03.05 13:13
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;

    private ServiceRegistry serviceRegistry;

    //用于存储业务接口和实现类的实例对象(由spring所构造)
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    private RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    //服务器绑定的地址和端口,由spring在构造本类时从配置文件中传入
    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        //用于向zookeeper注册名称服务的工具类
        this.serviceRegistry = serviceRegistry;
    }


    /**
     * 通过注解，获取标志了rpc服务注解的业务类的：接口及Impl对象，存储到handlerMap中
     *
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object o : serviceBeanMap.values()) {
                //从业务实现类中的自定义注解中获取到value，从而获取到业务接口的全名
                String interfaceName = o.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(interfaceName, o);
            }
        }
    }

    /**
     * 在此启动netty服务，绑定handler流水线
     * 1.接受请求数据进行反序列化得到request对象
     * 2.根据request中的参数，让RpcHandler从handlerMap中找到对应的业务Impl，调用指定方法，获取返回结果
     * 3.将业务调用结果封装到response对象中并序列化响应给客户端
     *
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //启动netty服务
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //TODO: 绑定handler业务流水线
                            socketChannel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))  //注册解码反序列化 In-1
                                    .addLast(new RpcEncoder(RpcResponse.class)) //注册编码序列化 Out
                                    .addLast(new RpcHandler(handlerMap));       //注册RpcHandler In-2
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            //绑定端口
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            LOGGER.debug("server started on port {}", port);
            System.out.println("Server Started On Port " + port +"...");

            if (serviceRegistry != null) {
                //向zookeeper注册服务节点
                serviceRegistry.register(serverAddress);
            }

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
