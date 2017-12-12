package org.storm;

import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.storm.configs.PropertiesFileService;
import org.storm.core.DBIdGenerator;
import org.storm.core.ZookeeperIdGenerator;
import org.storm.protobuf.SnowflakeServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted;
import static org.apache.zookeeper.Watcher.Event.EventType.None;

/**
 * Created by fm.chen on 2017/12/6.
 */
public class ZookeeperTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Integer zkPort = 2181;

    private final Integer basePort = 50050;

    private TestingServer zookeeperServer;

    @Before
    public void setUp() throws Exception {
        zookeeperServer = new TestingServer(zkPort);
        zookeeperServer.start();
    }

    @After
    public void setDown() throws IOException {
        zookeeperServer.close();
    }

    public class ZkIdGenThread extends Thread {

        private Integer rpcPort;

        private String dataDir;

        private Integer idsPerThread;

        private Boolean hasConflicted = false;

        private Set<Long> generatedIdSet;

        private Date baseDate;

        public ZkIdGenThread(Integer idsPerThread, Set<Long> generatedIdSet) {
            this.generatedIdSet = generatedIdSet;
            this.idsPerThread = idsPerThread;
        }

        public Integer getRpcPort() {
            return rpcPort;
        }

        public void setRpcPort(Integer rpcPort) {
            this.rpcPort = rpcPort;
        }

        public String getDataDir() {
            return dataDir;
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }

        public Boolean getHasConflicted() {
            return hasConflicted;
        }

        public void setHasConflicted(Boolean hasConflicted) {
            this.hasConflicted = hasConflicted;
        }

        public Date getBaseDate() {
            return baseDate;
        }

        public void setBaseDate(Date baseDate) {
            this.baseDate = baseDate;
        }

        private ZookeeperIdGenerator zookeeperIdGenerator() throws Exception {
            SnowflakeServer snowflakeServer = new SnowflakeServer(rpcPort);
            PropertiesFileService propertiesFileService = new PropertiesFileService(dataDir);
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString("localhost:2181")
                    .retryPolicy(retryPolicy)
                    .namespace("storm")
                    .build();
            client.start();
            ZookeeperIdGenerator zookeeperIdGenerator = new ZookeeperIdGenerator(client, propertiesFileService, snowflakeServer);
            zookeeperIdGenerator.setBaseDateTime(baseDate.getTime());
            zookeeperIdGenerator.afterPropertiesSet();
            return zookeeperIdGenerator;
        }

        private String printDateWorkIdSeqOf(Long v) {
            Integer mask = ZookeeperIdGenerator.SEQ_UPPER_BOUND;
            StringBuilder builder = new StringBuilder();
            builder.append( "Seq:" + String.valueOf(v & mask));
            v = v >> ZookeeperIdGenerator.SEQ_BITS;
            mask = ZookeeperIdGenerator.WORK_BOUND;
            builder.append( " workId:" + String.valueOf(v & mask));
            v = v >> (ZookeeperIdGenerator.WORK_SEQ_BITS - ZookeeperIdGenerator.SEQ_BITS);
            builder.append(" time:" + String.valueOf(v) + "\n");
            return builder.toString();
        }

        @Override
        public void run() {
            ZookeeperIdGenerator idGenerator = null;
            int j = 0;
            try {
                idGenerator = zookeeperIdGenerator();
                for (; j < idsPerThread; ++j) {
                    Long v = idGenerator.nextId();
                    synchronized(generatedIdSet) {
                        if (generatedIdSet.contains(v)) {
                            hasConflicted = true;
                            logger.debug("generate id: " + printDateWorkIdSeqOf(v));
                            for (Long lv : generatedIdSet) {
                                logger.debug("generatedIdSet id: " + printDateWorkIdSeqOf(lv));
                            }
                            logger.error("conflict range with {}", v);
                            return;
                        }
                        generatedIdSet.add(v);
                    }
                }
            } catch (Exception e) {
                logger.error("", e);
            } finally {
                logger.info("thread finished at {}", j);
            }
        }
    }

    @Test
    public void multiThreadIdTest() throws Exception {
        int threadNum = 10;
        int idsPerThread = 100000;
        Set<Long> generatedIdSet = Collections.synchronizedSet(new HashSet<>());
        List<Thread> threads = new ArrayList<>(threadNum);
        Date date = new Date();
        for (int i = 0; i < threadNum; ++i) {
            ZkIdGenThread thread = new ZkIdGenThread(idsPerThread, generatedIdSet);
            thread.setDataDir("./snowflake/zk-data-" + i + "/");
            thread.setRpcPort(basePort + i);
            thread.setBaseDate(date);
            threads.add(thread);
        }

        threads.forEach(e -> e.start());

        threads.forEach(e -> {
            try {
                e.join();
            } catch (InterruptedException ex) {
                logger.error("", ex);
            }
        });
        Assert.assertEquals(generatedIdSet.size(), threadNum*idsPerThread);
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
