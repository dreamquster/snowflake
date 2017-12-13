package org.storm.core;

import org.springframework.beans.factory.InitializingBean;

/**
 * Created by dknight on 2017/12/6.
 */
public class DBIdGenerator implements IdGenerator, InitializingBean {


    private IdRange idRange; // [start, end)

    private IdRange nextRange;

    private Long currentId;

    private IdGeneratorRepo idGeneratorRepo;

    private String bizTag;

    public DBIdGenerator(IdGeneratorRepo idGeneratorRepo, String bizTag) {
        this.idGeneratorRepo = idGeneratorRepo;
        this.bizTag = bizTag;
    }

    @Override
    public synchronized Long nextId() {
        if (idRange.getEnd().equals(currentId)) {
            idRange = nextRange;
            currentId = nextRange.getStart();
            nextRange = null;
        }

        if (null == nextRange && idRange.exceedMid(currentId)) {
            nextRange = idGeneratorRepo.fetchIdBatch(bizTag);
        }
        return currentId++;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        idRange = idGeneratorRepo.fetchIdBatch(bizTag);
        currentId = idRange.getStart();
    }
}
