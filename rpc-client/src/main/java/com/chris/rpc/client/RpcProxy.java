package com.chris.rpc.client;

import com.chris.rpc.common.RpcRequest;
import com.chris.rpc.common.RpcResponse;
import com.chris.rpc.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC代理 (用于创建RPC服务代理)
 * RpcProxy
 * author: Chris
 * time：2018.03.05 15:01
 */
public class RpcProxy {

    private String serverAddress;

    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 创建代理
     *
     * @param interfaceClass
     * @param <T>
     * @return
     */
    public <T> T create(Class<?> interfaceClass) {
        //通过接口名，创建接口类的代理对象
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //创建RPCRequest，封装被代理类的属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        //拿到声明这个方法的业务接口名称
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        //查找服务
                        if (serviceDiscovery != null) {
                            serverAddress = serviceDiscovery.discover();
                        }
                        //随机获取服务的地址
                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        //创建netty实现的RpcClient，连接服务器
                        RpcClient client = new RpcClient(host, port);
                        //通过netty向服务端发送请求
                        RpcResponse response = client.send(request);
                        //返回信息
                        if (response.isError()) {
                            throw response.getError();
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }
}
