package org.storm.core;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by dknight on 2017/12/6.
 */
public class DBIdGenerator implements IdGenerator {


    private IdRange idRange;

    private IdRange nextRange;

    private Long currentId;

    private IdGeneratorRepo idGeneratorRepo;

    private String bizTag;

    @Override
    public synchronized Long nextId() {
        Long res = currentId;
        if (idRange.getEnd() ==  res) {
            idRange = nextRange;
            currentId = nextRange.getStart();
            return res;
        }

        if (idRange.exceedMid(res)) {
            nextRange = idGeneratorRepo.fetchIdBatch(bizTag);
        }
        currentId++;
        return res;
    }





}
