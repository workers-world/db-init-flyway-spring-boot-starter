package com.books.dbinit.actuator;

import com.books.dbinit.config.DbInitProperties;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Endpoint(id = "db-init")
@ConditionalOnClass(Endpoint.class)
public class DbInitEndpoint {

    private final JdbcTemplate jdbcTemplate;
    private final DbInitProperties properties;

    public DbInitEndpoint(DataSource dataSource, DbInitProperties properties) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.properties = properties;
    }

    @ReadOperation
    public Map<String, Object> info() {
        String table = properties.getTable();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("history_table", table);

        try {
            List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
                    "SELECT installed_rank, version, description, type, script, checksum, " +
                    "installed_on, execution_time, success FROM " + table + " ORDER BY installed_rank");
            result.put("migrations", migrations);
            result.put("count", migrations.size());
        } catch (Exception e) {
            result.put("error", "无法查询迁移历史: " + e.getMessage());
        }

        return result;
    }
}
