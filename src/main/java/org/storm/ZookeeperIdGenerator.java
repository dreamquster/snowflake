package org.storm;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by fm.chen on 2017/11/28.
 */
@Service
public class ZookeeperIdGenerator implements IdGenerator {


    @Autowired
    private CuratorFramework zkClient;

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



    public Integer getWorkId() {
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
}
