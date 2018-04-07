package com.chris.rpc.sample.server;

import com.chris.rpc.sample.pojo.Person;
import com.chris.rpc.sample.service.IHelloService;
import com.chris.rpc.server.RpcService;

/**
 * 将HelloServiceImpl发布为PRC服务类
 * HelloServiceImpl
 * author: Chris
 * time：2018.03.05 15:42
 */
@RpcService(IHelloService.class)
public class HelloServiceImpl implements IHelloService {

    public String hello(String name) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：" + "Hello! " + name);
        return "Hello! " + name;
    }

    public String hello(Person person) {
        System.out.println("已经调用服务端接口实现，业务处理结果为：" + "Hello! " + person.getName() + " " + person.getAge());
        return "Hello! " + person.getName() + " " + person.getAge();
    }
}
