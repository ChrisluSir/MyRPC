package com.chris.rpc.registry;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 用于client发现server节点的变化，实现负载均衡
 * ServiceDiscovery
 * author: Chris
 * time：2018.03.05 12:22
 */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> serverList = new ArrayList<String>();

    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;

        ZooKeeper zk = connectServer();
        if (zk != null) {
            watchNode(zk);
        }
    }

    /**
     * 发现新节点
     *
     * @return
     */
    public String discover() {
        String server = null;
        int size = serverList.size();
        //存在新节点，使用即可
        if (size > 0) {
            if (size == 1) {
                server = serverList.get(0);
                LOGGER.debug("using only server: {}", server);
            } else {
                //TODO: 设置负载均衡策略，或使用轮询访问
                server = serverList.get(new Random().nextInt(size));
                LOGGER.debug("using ramdom server: {}", server);
            }
        }
        return server;
    }

    /**
     * 监听节点的变化 (服务器上下线)
     *
     * @param zk
     */
    private void watchNode(final ZooKeeper zk) {
        try {
            //获取所有的子节点
            List<String> nodeList = zk.getChildren(Const.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    //节点改变
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<String>();
            //循环子节点
            for (String node : nodeList) {
                //获取节点中的额服务器地址
                byte[] bytes = zk.getData(Const.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            //将节点信息就在成员变量中
            this.serverList = dataList;
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }

    /**
     * 链接
     *
     * @return
     */
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Const.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        public void process(WatchedEvent event) {
                            if (event.getState() == Event.KeeperState.SyncConnected) {
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        return zk;
    }
}
























