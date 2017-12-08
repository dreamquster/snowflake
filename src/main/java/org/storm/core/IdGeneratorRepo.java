package org.storm.core;

import jdk.nashorn.internal.AssertsEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by dknight on 2017/12/6.
 */
public class IdGeneratorRepo {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    public IdGeneratorRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
    }


    public IdRange fetchIdBatch(String bizTag) {
        // the code in this method executes in a transactional context
        IdRange idRange = transactionTemplate.execute(status -> {
            jdbcTemplate.update("UPDATE id_generator SET max_id = max_id + step WHERE biz_tag=?", bizTag);
            IdRange maxRange = jdbcTemplate.queryForObject("SELECT max_id, step FROM id_generator WHERE biz_tag=?",
                    new Object[]{bizTag}, new IdRangeRowMapper());
            return maxRange;
        });
        logger.debug("fetch range:{}-{}", idRange.getStart(), idRange.getEnd());
        return idRange;
    }
}
