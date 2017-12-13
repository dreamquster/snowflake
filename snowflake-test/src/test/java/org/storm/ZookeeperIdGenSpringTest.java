package org.storm;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.storm.core.IdGenerator;
import org.storm.core.ZookeeperIdGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by fm.chen on 2017/12/12.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@PropertySource("classpath:/application.properties")
public class ZookeeperIdGenSpringTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZookeeperIdGenerator zookeeperIdGenerator;

    private static TestingServer zookeeperServer;

    @BeforeClass
    public static void beforeClass() throws Exception {
        zookeeperServer = new TestingServer(2181);
        zookeeperServer.start();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        zookeeperServer.close();
    }

    public class ZkIdGenThread extends Thread {

        private Set<Long> generatedIdSet;

        private Integer idsPerThread;

        private IdGenerator idGenerator;

        public ZkIdGenThread(Integer idsPerThread, Set<Long> generatedIdSet) {
            this.generatedIdSet = generatedIdSet;
            this.idsPerThread = idsPerThread;
        }

        public IdGenerator getIdGenerator() {
            return idGenerator;
        }

        public void setIdGenerator(IdGenerator idGenerator) {
            this.idGenerator = idGenerator;
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
            for (int j = 0; j < idsPerThread; ++j) {
                Long v = idGenerator.nextId();
                synchronized(generatedIdSet) {
                    if (generatedIdSet.contains(v)) {
                        int cnt = countSeqInSameTime(v);
                        logger.error("conflict range with {} with count {}", v, cnt);
                        return;
                    }
                    generatedIdSet.add(v);
                }
            }
        }

        private int countSeqInSameTime(Long v) {
            int cnt = 0;
            for (Long lv : generatedIdSet) {
                Long time = lv >> ZookeeperIdGenerator.WORK_SEQ_BITS;
                if (time.equals((v >> ZookeeperIdGenerator.WORK_SEQ_BITS))) {
                    ++cnt;
                }
            }
            return cnt;
        }
    }

    @Test
    public void multiThreadTest() {
        int threadNum = 10;
        int idsPerThread = 100000;
        Set<Long> generatedIdSet = Collections.synchronizedSet(new HashSet<>());
        List<Thread> threads = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; ++i) {
            ZkIdGenThread thread = new ZkIdGenThread(idsPerThread, generatedIdSet);
            thread.setIdGenerator(zookeeperIdGenerator);
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
}
