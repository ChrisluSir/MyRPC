package com.chris.rpc.sample.service;

import com.chris.rpc.sample.pojo.Person;

/**
 * IHelloService
 * author: Chris
 * time：2018.03.04 23:39
 */
public interface IHelloService {

    String hello(String name);

    String hello(Person person);
}
