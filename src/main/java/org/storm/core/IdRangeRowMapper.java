package org.storm.core;


import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by dknight on 2017/12/6.
 */
public class IdRangeRowMapper implements RowMapper<IdRange> {
    @Override
    public IdRange mapRow(ResultSet rs, int rowNum) throws SQLException {
        IdRange range = new IdRange();
        range.setEnd(rs.getLong("max_id"));
        range.setStep(rs.getInt("step"));
        range.setStart(range.getEnd() - range.getStep());
        return range;
    }
}
