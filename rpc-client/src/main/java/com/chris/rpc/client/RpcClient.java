package com.chris.rpc.client;

import com.chris.rpc.common.RpcDecoder;
import com.chris.rpc.common.RpcEncoder;
import com.chris.rpc.common.RpcRequest;
import com.chris.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 框架的RPC客户端 (用于发送RPC请求)
 * RpcClient
 * author: Chris
 * time：2018.03.05 15:09
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private Integer port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 通过netty客户端连接服务端，发送请求消息
     * @param request
     * @return
     * @throws Exception
     */
    public RpcResponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        // 向pipeline中添加编码、解码、业务处理的handler
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))  //Out
                                    .addLast(new RpcDecoder(RpcResponse.class)) //In-1
                                    .addLast(RpcClient.this);                   //In-2
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);

            //连接服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //将request对象写入outbundle处理后发出（即RpcEncoder编码器）
            future.channel().writeAndFlush(request).sync();

            // 用线程等待的方式决定是否关闭连接
            // 先在此阻塞，等待获取到服务端的返回后，被唤醒，从而关闭网络连接
            synchronized (obj) {
                obj.wait();
            }
            if (response != null) {
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }


    /**
     * 服务服务端的返回结果
     *
     * @param ctx
     * @param rpcResponse
     * @throws Exception
     */
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        //阻塞等待返回结果
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }
}
