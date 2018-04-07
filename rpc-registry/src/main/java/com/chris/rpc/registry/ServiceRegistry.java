package com.chris.rpc.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * 服务注册，zookeeper用于注册所有服务器的地址与端口，并对客户端提供服务发现的功能
 * ServiceRegistry
 * author: Chris
 * time：2018.03.05 12:05
 */
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        //zookeeper地址
        this.registryAddress = registryAddress;
    }

    /**
     * 创建zookeeper连接，并监听
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Const.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        //count减1，当count为0时释放所有等待的线程
                        latch.countDown();
                    }
                }
            });
            //当前线程等待，直到count变为0
            System.out.println("Waiting for zookeeper connect...");
            latch.await();
        } catch (Exception e) {
            LOGGER.error("connect fail", e);
            e.printStackTrace();
        }
        //连接成功
        return zk;
    }

    /**
     * 创建zookeeper连接
     *
     * @param data
     */
    public void register(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
                createNode(zk, data);
            }
        }
    }

    /**
     * 注册zookeeper节点
     * @param zk
     * @param data
     */
    private void createNode(ZooKeeper zk, String data) {
        try {
            //获取服务器地址及端口
            byte[] bytes = data.getBytes();
            if (zk.exists(Const.ZK_REGISTRY_PATH, null) == null) {
                zk.create(Const.ZK_REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            String path = zk.create(Const.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} --> {})", path, data);
        } catch (Exception e) {
            LOGGER.error("create node error", e);
        }
    }

}























