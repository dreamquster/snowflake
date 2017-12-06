package org.storm;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;
import static org.apache.zookeeper.Watcher.Event.EventType.None;

/**
 * Created by fm.chen on 2017/12/6.
 */
public class ZookeeperTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void ephemeralNodeReconnectTest() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.20.148:2181")
                .retryPolicy(retryPolicy)
                .namespace("storm")
                .build();

    }



    public  class ZNodeWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            Event.EventType eventType = event.getType();
            Event.KeeperState keeperState =  event.getState();
            String path = event.getPath();
            switch(event.getType()) {
                case None:
                    //connection Error：会自动重连
                    logger.info("[Watcher],Connecting...");
                    if(keeperState == Event.KeeperState.SyncConnected){
                        logger.info("[Watcher],Connected...");
                        //检测临时节点是否失效等。
                    }
                    break;
                case NodeCreated:
                    logger.info("[Watcher],NodeCreated:" + path);
                    break;
                case NodeDeleted:
                    logger.info("[Watcher],NodeDeleted:" + path);
                    break;
                default:
                    //
            }
        }
    }
}
