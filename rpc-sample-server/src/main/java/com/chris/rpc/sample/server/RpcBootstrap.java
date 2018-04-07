package com.chris.rpc.sample.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 用户系统服务端的启动入口
 * 其意义是启动springcontext，从而构造框架中的RpcServer
 * 亦即：将用户系统中所有标注了RpcService注解的业务发布到RpcServer中
 * RpcBootstrap
 * author: Chris
 * time：2018.03.05 15:37
 */

public class RpcBootstrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("spring.xml");
        System.out.println("服务启动, 可提供RPC服务...");
    }
}
