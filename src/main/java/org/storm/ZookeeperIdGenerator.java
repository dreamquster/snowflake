package org.storm;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fm.chen on 2017/11/28.
 */
@Service
public class ZookeeperIdGenerator implements IdGenerator, InitializingBean {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CuratorFramework zkClient;

    @Value("${data-dir}")
    private String dataDir;

    private String prevNodePath;

    private Integer workId;

    private AtomicInteger seqGen;

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
        Stat stat = new Stat();
        try {
            zkClient.getData().storingStatIn(stat).forPath(PATH_PREFIX);
            stat.getMzxid();
        } catch (Exception e) {
            logger.error("", e);
        }

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

    private static final Integer UPPER_BOUND = 0x3F; //111 1111 1111

    private static final Integer WORK_BITS = 10;

    private static final Integer WORK_SEQ_BITS = WORK_BITS + 10;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!isExistWorkNode()) {
            createWorkNode();
        }

        extractWorkId();

    }

    private void createWorkNode() throws Exception {
        Long registerTime = System.currentTimeMillis();
        prevNodePath = zkClient.create()
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                .forPath(PATH_PREFIX, new byte[]{registerTime.byteValue()});
    }

    private void extractWorkId() {
        int sepIdx = prevNodePath.indexOf("-");
        workId = Integer.parseInt(prevNodePath.substring(sepIdx + 1));
        logger.info("workId:{} with path:{}", workId, prevNodePath);
    }
}
