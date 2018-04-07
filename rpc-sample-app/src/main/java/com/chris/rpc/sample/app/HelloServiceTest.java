package com.chris.rpc.sample.app;

import com.chris.rpc.client.RpcProxy;
import com.chris.rpc.sample.pojo.Person;
import com.chris.rpc.sample.service.IHelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * HelloServiceTest
 * author: Chris
 * time：2018.03.05 15:54
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring.xml")
public class HelloServiceTest implements ApplicationContextAware {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloString() {
        //调用代理的crate方法，代理HelloService接口
        IHelloService helloService = rpcProxy.create(IHelloService.class);
        //调用代理的方法，执行invoke
        String result = helloService.hello("Chris");
        System.out.println("服务端返回结果为：" + result);
    }

    @Test
    public void helloObject() {
        //调用代理的crate方法，代理HelloService接口
        IHelloService helloService = rpcProxy.create(IHelloService.class);
        //调用代理的方法，执行invoke
        String result = helloService.hello(new Person("Alan", "27"));
        System.out.println("服务端返回结果为：" + result);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
