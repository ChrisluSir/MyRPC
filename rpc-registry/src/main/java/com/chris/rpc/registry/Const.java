package com.chris.rpc.registry;

/**
 * Const
 * author: Chris
 * time：2018.03.05 12:04
 */
public class Const {

    public static final int ZK_SESSION_TIMEOUT = 5000;//zk超时时间

    public static final String ZK_REGISTRY_PATH = "/registry";//注册节点
    public static final String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";//节点
}
