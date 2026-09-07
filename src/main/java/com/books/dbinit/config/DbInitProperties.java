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

    /**
     * 危险操作双开关:repair 与 repair-confirm 同时为 true 才执行 {@code flyway.repair()}。
     * repair 会把已记录脚本的 checksum 改写为本地值,令 validateOnMigrate 形同虚设,
     * 因此必须二次确认,且仅限开发环境。
     */
    private boolean repair = false;
    private boolean repairConfirm = false;

    private Map<String, DatasourceConfig> datasources = new HashMap<>();

    public boolean isRepairEnabled() {
        return repair && repairConfirm;
    }

    /** 多数据源的 table:per-DS 未配置(null)时继承父级 */
    public String effectiveTable(DatasourceConfig c) {
        return c.getTable() != null ? c.getTable() : table;
    }

    /** 多数据源的 validateOnMigrate:null(未配置)继承父级,显式 false/true 以 per-DS 为准 */
    public boolean effectiveValidateOnMigrate(DatasourceConfig c) {
        return c.getValidateOnMigrate() != null ? c.getValidateOnMigrate() : validateOnMigrate;
    }

    /** 多数据源的 baselineOnMigrate:null(未配置)继承父级 */
    public boolean effectiveBaselineOnMigrate(DatasourceConfig c) {
        return c.getBaselineOnMigrate() != null ? c.getBaselineOnMigrate() : baselineOnMigrate;
    }

    /** 多数据源的 baselineVersion:null(未配置)继承父级 */
    public String effectiveBaselineVersion(DatasourceConfig c) {
        return c.getBaselineVersion() != null ? c.getBaselineVersion() : baselineVersion;
    }

    /** 多数据源的 locations:per-DS 未配置(null/空)时继承父级 */
    public List<String> effectiveLocations(DatasourceConfig c) {
        return c.getLocations() != null && !c.getLocations().isEmpty() ? c.getLocations() : locations;
    }

    @Data
    public static class DatasourceConfig {
        private boolean enabled = true;
        /** null/空 → 继承父级 locations(历史版本不继承,导致多数据源模式回落默认目录) */
        private List<String> locations;
        private String table;
        /** null → 继承父级(历史版本为 primitive boolean,无法区分「未设置」与 false) */
        private Boolean validateOnMigrate;
        /** null → 继承父级 */
        private Boolean baselineOnMigrate;
        private String baselineVersion;
    }
}
