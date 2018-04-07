package com.chris.rpc.server;

import com.chris.rpc.common.RpcRequest;
import com.chris.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 处理具体的业务调用
 * 通过构造时传入的"业务接口及实现"handlerMap，来调用客户端所请求的业务方法
 * 并将业务方法返回值封装成response对象写入下一个handler (即编码handler：RpcEncoder)
 * RpcHandler
 * author: Chris
 * time：2018.03.05 13:52
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 接受消息、处理消息、返回结果
     *
     * @param ctx
     * @param rpcRequest
     * @throws Exception
     */
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());
        Object result = handler(rpcRequest);
        response.setResult(result);
        //写入outbundle(即RpcEncoder)进行下一步处理(即编码)后发送到channel中给客户端
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 根据request来处理具体的业务调用
     * 调用时通过反射的方式来实现
     *
     * @param rpcRequest
     * @return
     * @throws Exception
     */
    private Object handler(RpcRequest rpcRequest) throws Exception {
        String className = rpcRequest.getClassName();

        //拿到实现类的对象
        Object o = handlerMap.get(className);

        //拿到要调用的方法名、参数类型、参数
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();

        //拿到接口类
        Class<?> forName = Class.forName(className);

        //调用实现类对象的指定方法并返回结果
        Method method = forName.getMethod(methodName, parameterTypes);
        return method.invoke(o, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}





















