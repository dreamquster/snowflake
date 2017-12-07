package org.storm.core;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dknight on 2017/12/6.
 */
public class IdGeneratorRepo {

    private JdbcTemplate jdbcTemplate;

    public IdGeneratorRepo(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public IdRange fetchIdBatch(String bizTag) {
        jdbcTemplate.update("UPDATE id_generator SET max_id = max_id + step WHERE biz_tag=?", bizTag);
        IdRange maxRange = jdbcTemplate.queryForObject("SELECT max_id, step FROM id_generator WHERE biz_tag=?",
                new Object[]{bizTag}, new IdRangeRowMapper());
        return maxRange;
    }
}
