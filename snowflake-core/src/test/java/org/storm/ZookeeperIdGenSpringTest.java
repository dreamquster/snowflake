package org.storm;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.storm.core.IdGenerator;
import org.storm.core.ZookeeperIdGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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

    private TestingServer zookeeperServer;

    @Before
    public void setUp() throws Exception {
        zookeeperServer = new TestingServer(2181);
        zookeeperServer.start();
    }

    @After
    public void setDown() throws IOException {
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

        @Override
        public void run() {
            for (int j = 0; j < idsPerThread; ++j) {
                Long v = idGenerator.nextId();
                synchronized(generatedIdSet) {
                    if (generatedIdSet.contains(v)) {
                        logger.error("conflict range with {}", v);
                        return;
                    }
                    generatedIdSet.add(v);
                }
            }
        }
    }

    @Test
    public void multiThreadTest() {
        int threadNum = 1;
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
