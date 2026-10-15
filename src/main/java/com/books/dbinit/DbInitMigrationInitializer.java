package com.books.dbinit;

import com.books.dbinit.callback.DbInitAfterMigrateCallback;
import com.books.dbinit.config.DbInitProperties;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * 承载实际迁移的初始化 Bean。
 *
 * <p>相对历史实现的两个关键修正:</p>
 * <ul>
 *   <li><b>时序契约</b>:迁移从自动配置类的 {@code afterPropertiesSet} 移入独立 Bean——
 *       通过 {@code META-INF/spring.factories} 注册的
 *       {@link DbInitInitializerDetector},JPA EntityManagerFactory 等声明了
 *       {@code @DependsOnDatabaseInitialization} 的 Bean 会自动依赖本 Bean,
 *       不再可能在迁移前抢先查库。</li>
 *   <li><b>多数据源配置穿透修复</b>:per-DS 未配置的 validate/baseline/table/locations
 *       均继承父级（见 {@link DbInitProperties} 的 effective* 方法）。</li>
 * </ul>
 */
@Slf4j
public class DbInitMigrationInitializer implements InitializingBean {

    /** 数据源解析:主模式忽略名称返回主数据源;多数据源模式按 Bean 名查找 */
    private final java.util.function.Function<String, DataSource> dataSourceLookup;
    private final DbInitProperties properties;
    private final List<String> activeProfiles;
    private final List<Callback> customCallbacks;

    public DbInitMigrationInitializer(java.util.function.Function<String, DataSource> dataSourceLookup,
                                      DbInitProperties properties,
                                      List<String> activeProfiles,
                                      List<Callback> customCallbacks) {
        this.dataSourceLookup = dataSourceLookup;
        this.properties = properties;
        this.activeProfiles = activeProfiles != null ? activeProfiles : List.of();
        this.customCallbacks = customCallbacks != null ? customCallbacks : List.of();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("db-init 开始 Flyway 数据库迁移");
        if (!properties.getDatasources().isEmpty()) {
            properties.getDatasources().forEach((name, config) -> {
                if (!config.isEnabled()) {
                    log.info("db-init 数据源 [{}] 已禁用，跳过", name);
                    return;
                }
                DataSource ds = dataSourceLookup.apply(name);
                applyMigrations(ds, properties.effectiveLocations(config),
                        properties.effectiveTable(config),
                        properties.effectiveValidateOnMigrate(config),
                        properties.effectiveBaselineOnMigrate(config),
                        properties.effectiveBaselineVersion(config), name);
            });
        } else {
            applyMigrations(dataSourceLookup.apply("primary"), properties.getLocations(), properties.getTable(),
                    properties.isValidateOnMigrate(), properties.isBaselineOnMigrate(),
                    properties.getBaselineVersion(), "primary");
        }
        log.info("db-init Flyway 数据库迁移完成");
    }

    void applyMigrations(DataSource ds, List<String> configuredLocations, String table,
                         boolean validateOnMigrate, boolean baselineOnMigrate,
                         String baselineVersion, String dsName) {
        List<String> locations = resolveLocations(configuredLocations, properties.isProfileAware(), activeProfiles);
        if (locations.isEmpty()) {
            locations = List.of("classpath:db/migration");
        }

        List<Callback> callbacks = new ArrayList<>();
        callbacks.add(new DbInitAfterMigrateCallback());
        callbacks.addAll(customCallbacks);

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations(locations.toArray(new String[0]))
                .table(table)
                .validateOnMigrate(validateOnMigrate)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion(baselineVersion != null ? MigrationVersion.fromVersion(baselineVersion) : null)
                .callbacks(callbacks.toArray(new Callback[0]))
                .load();

        if (properties.isRepairEnabled()) {
            log.warn("db-init repair 模式已启用(repair + repair-confirm 双开关)——"
                    + "它会改写已记录脚本的 checksum,使 validateOnMigrate 失效,严禁在生产环境使用!");
            flyway.repair();
        }

        MigrateResult result = flyway.migrate();
        String schema = result.schemaName != null ? result.schemaName : "default";
        String version = result.targetSchemaVersion != null ? result.targetSchemaVersion : "baseline";
        log.info("db-init [{}] 迁移完成: schema={} 已执行={} 版本={}",
                dsName, schema, result.migrationsExecuted, version);
    }

    /**
     * 生成迁移位置列表。profile-aware 时为每个基础位置追加一层 {@code -<profile>} 后缀;
     * 以基础列表为快照遍历,不会产生跨 profile 的笛卡尔积
     * (历史版本会对多 profile 生成 {@code db/migration-dev-docker} 之类的组合目录)。
     */
    static List<String> resolveLocations(List<String> configured, boolean profileAware,
                                         List<String> activeProfiles) {
        List<String> base = new ArrayList<>(configured != null ? configured : List.of());
        if (!profileAware || activeProfiles.isEmpty()) {
            return base;
        }
        List<String> result = new ArrayList<>(base);
        for (String profile : activeProfiles) {
            String suffix = "-" + profile;
            for (String loc : base) {
                if (!loc.endsWith(suffix)) {
                    result.add(loc + suffix);
                }
            }
        }
        return result;
    }
}
