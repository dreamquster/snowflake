package org.storm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.storm.core.DBIdGenerator;
import org.storm.core.IdGeneratorRepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

/**
 * Created by fm.chen on 2017/12/7.
 */
public class DBIdGeneratorTest {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JdbcTemplate jdbcTemplate;

    private EmbeddedDatabase db;

    private String bizTag = "test";

    @Before
    public void setUp() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        db = builder.setType(H2).addScript("snowflake-mysql.sql").build();
        // do stuff against the db (EmbeddedDatabase extends javax.sql.DataSource)


    }

    public class IdGenRunnable implements Runnable {

        private Integer idsPerThread;

        private Set<Long> generatedIdSet;

        private Boolean isFailed;

        public IdGenRunnable(Integer idsPerThread, Set<Long> outerSet, Boolean isFailed) {
            this.idsPerThread = idsPerThread;
            generatedIdSet = outerSet;
            this.isFailed = isFailed;
        }

        @Override
        public void run() {
            DBIdGenerator dbIdGenerator = null;
            int j = 0;
            try {
                dbIdGenerator = makeDBIdGen();
                for (; j < idsPerThread; ++j) {
                    Long v = dbIdGenerator.nextId();
                    synchronized(generatedIdSet) {
                        if (generatedIdSet.contains(v)) {
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
        int idsPerThread = 10000;
        Set<Long> generatedIdSet = Collections.synchronizedSet(new HashSet<>());
        Boolean isFailed = false;
        List<Thread> threads = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; ++i) {
            threads.add(new Thread(new IdGenRunnable(idsPerThread, generatedIdSet, isFailed)));
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
        Assert.assertFalse(isFailed);
    }

    private DBIdGenerator makeDBIdGen() throws Exception {
        IdGeneratorRepo repo = new IdGeneratorRepo(new JdbcTemplate(db));
        DBIdGenerator dbIdGenerator = new DBIdGenerator(repo, bizTag);
        dbIdGenerator.afterPropertiesSet();
        return dbIdGenerator;
    }
}
