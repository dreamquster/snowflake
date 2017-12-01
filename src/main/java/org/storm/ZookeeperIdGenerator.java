package org.storm;

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.storm.configs.PropertiesFileService;
import org.storm.protobuf.SnowflakeClient;
import org.storm.protobuf.SnowflakeServer;
import org.storm.protobuf.SystemTimeResponse;
import org.storm.utils.ByteUtils;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;

/**
 * Created by fm.chen on 2017/11/28.
 */
@Service
public class ZookeeperIdGenerator implements IdGenerator, InitializingBean {

    private static final String PREV_NODE_PATH = "prevNodePath";

    private static final int THRESHOLD = 100; //ms

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CuratorFramework zkClient;

    @Autowired
    private PropertiesFileService propertiesFileService;

    @Autowired
    private SnowflakeServer snowflakeServer;

    private String prevNodePath;

    private Integer workId;

    private AtomicInteger seqGen;

    private Timer timer = new Timer();

    public ZookeeperIdGenerator() {
    }

    public ZookeeperIdGenerator(Integer workId, AtomicInteger seqGen) {
        this.workId = workId;
        this.seqGen = seqGen;
    }


    @Override
    public Long nextId() {
        return snowflakeId(workId, seqGen.getAndIncrement());
    }



    public Integer getWorkId()  {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    public AtomicInteger getSeqGen() {
        return seqGen;
    }

    public void setSeqGen(AtomicInteger seqGen) {
        this.seqGen = seqGen;
    }

    private static final Integer UPPER_BOUND = 0x3F; //11 1111 1111

    private static final Integer WORK_BITS = 10;

    private static final Integer WORK_SEQ_BITS = WORK_BITS + 10;

    private static final Integer EXPIRE_SECONDS = 5;

    public Long snowflakeId(Integer workId, Integer seq) {
        if (UPPER_BOUND < workId || UPPER_BOUND < seq) {
            throw new IllegalArgumentException(String.format("workId or seq exceed the upper limit:%d", UPPER_BOUND));
        }
        Long stamp = System.currentTimeMillis();
        stamp = stamp<<WORK_SEQ_BITS; //
        workId = workId<<WORK_BITS;
        stamp = stamp | workId | seq;
        return stamp;
    }

    private static final String PATH_PREFIX = "/id_generator/worker-";

    private static final String CLUSTER_PEER = "/running-temp/machine";

    private static final String LAST_UPDATE_TIME = "/last-update";

    private static final String FIELD_SEP = "-";

    private boolean isExistWorkNode() {
        if (null == prevNodePath) {
            return false;
        }

        Stat stat = null;
        try {
            stat = zkClient.checkExists().forPath(prevNodePath);
            if (stat == null) {
                return false;
            }
        } catch (Exception e) {
            logger.error("", e);
            return false;
        }


        return true;
    }

    // true: time of this machine is fall behind largely than others in cluster.
    private boolean existTimeTurnBack() {
        try {
            Long updateTime =  ByteUtils.bytesToLong(zkClient.getData().forPath(prevNodePath +  LAST_UPDATE_TIME));
            if (updateTime >= System.currentTimeMillis())  {
                return true;
            }

            List<String> childPaths = zkClient.getChildren().forPath(CLUSTER_PEER);
            List<ListenableFuture<SystemTimeResponse>> responses = new ArrayList<>(childPaths.size());
            long curTime = System.currentTimeMillis();
            for (String childPath : childPaths) {
                logger.info("child path:{}", childPath);
                SnowflakeClient client = getPeerClient(childPath);
                responses.add(client.asyncPeerSystemInfo());
            }
            BigDecimal sum = BigDecimal.ZERO;
            int count = 0;
            for (int i = 0; i < responses.size(); ++i) {
                ListenableFuture<SystemTimeResponse> response = responses.get(i);
                try {
                    sum = sum.add(BigDecimal.valueOf(response.get().getTime()));
                    ++count;
                } catch (InterruptedException | ExecutionException e) {
                    logger.error(String.format("%s failed to response", childPaths.get(i)),e);
                }
            }

            long avgTime = sum.divide(BigDecimal.valueOf(count)).longValue();
            if ((curTime + THRESHOLD) <= avgTime) { // TimeUnit.MILLISECONDS.
                return true;
            }
        } catch (Exception e) {
            logger.error(String.format("get last updated time error for %d", workId), e);
        }
        return false;
    }

    private SnowflakeClient getPeerClient(String childPath) {
        int idx = childPath.lastIndexOf("/");
        String hostPort = childPath.substring(idx + 1);
        String[] s = hostPort.split(FIELD_SEP);
        String host = s[0];
        Integer port = Integer.parseInt(s[1]);
        SnowflakeClient client = new SnowflakeClient(host, port);
        return client;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        prevNodePath = propertiesFileService.getProperty(PREV_NODE_PATH);
        if (!isExistWorkNode()) {
            createWorkNode();
        } else {
            if (existTimeTurnBack()) {
                throw new IllegalStateException("time of this machine is fall behind largely than others in cluster");
            }
        }

        registerIPPort();
        timer.scheduleAtFixedRate(periodUpdateTimeTask, new Date(), TimeUnit.SECONDS.toMillis(3));
        Integer workId = extractWorkId();
        propertiesFileService.saveSetProperty(PREV_NODE_PATH, workId);
        snowflakeServer.start();
    }

    private TimerTask periodUpdateTimeTask =  new TimerTask() {
        @Override
        public void run() {
            try {
                zkClient.setData().forPath(prevNodePath + LAST_UPDATE_TIME,
                        ByteUtils.longToBytes(System.currentTimeMillis()));
            } catch (Exception e) {
                logger.error("failed to periodically update time!", e);
            }
        }
    };

    private void createWorkNode() throws Exception {
        Long registerTime = System.currentTimeMillis();
        prevNodePath = zkClient.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(PATH_PREFIX, ByteUtils.longToBytes(registerTime));
    }

    private int extractWorkId() {
        int sepIdx = prevNodePath.indexOf("-");
        workId = Integer.parseInt(prevNodePath.substring(sepIdx + 1));
        logger.info("workId:{} with path:{}", workId, prevNodePath);
        return workId;
    }

    private void registerIPPort() throws Exception {
        InetSocketAddress address = snowflakeServer.getAddress();
        String hostPort = address.getAddress().getHostAddress() + FIELD_SEP + address.getPort();
        zkClient.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(CLUSTER_PEER + "/" + hostPort);
    }

}
