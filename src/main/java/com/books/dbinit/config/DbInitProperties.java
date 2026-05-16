package com.books.dbinit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "db-init")
public class DbInitProperties {

    private boolean enabled = true;
    private List<String> locations = new ArrayList<>();
    private String table = "flyway_schema_history";
    private boolean validateOnMigrate = true;
    private boolean profileAware = true;
    private boolean baselineOnMigrate = false;
    private String baselineVersion;
    private boolean repair = false;
    private Map<String, DatasourceConfig> datasources = new HashMap<>();

    @Data
    public static class DatasourceConfig {
        private boolean enabled = true;
        private List<String> locations = new ArrayList<>();
        private String table;
        private boolean validateOnMigrate = true;
        private boolean baselineOnMigrate = false;
        private String baselineVersion;
    }
}
