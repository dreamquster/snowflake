package org.storm.core;

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
            nextRange = null;
            return res;
        }

        if (null == nextRange && idRange.exceedMid(res)) {
            nextRange = idGeneratorRepo.fetchIdBatch(bizTag);
        }
        currentId++;
        return res;
    }





}
