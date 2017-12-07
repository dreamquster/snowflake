package org.storm.core;

import org.springframework.beans.factory.InitializingBean;

/**
 * Created by dknight on 2017/12/6.
 */
public class DBIdGenerator implements IdGenerator, InitializingBean {


    private IdRange idRange;

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
        Long res = currentId;
        if (idRange.getEnd() ==  res) {
            idRange = nextRange;
            currentId = nextRange.getStart();
            nextRange = null;
            return res;
        }

        if (null == nextRange && idRange.exceedMid(res)) {
            nextRange = idGeneratorRepo.fetchIdBatch(bizTag);
        }
        currentId++;
        return res;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        idRange = idGeneratorRepo.fetchIdBatch(bizTag);
        currentId = idRange.getStart();
    }
}
