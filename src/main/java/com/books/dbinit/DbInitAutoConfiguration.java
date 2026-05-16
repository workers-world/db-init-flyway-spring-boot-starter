package com.books.dbinit;

import com.books.dbinit.callback.DbInitAfterMigrateCallback;
import com.books.dbinit.config.DbInitProperties;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DbInitProperties.class)
@ConditionalOnProperty(
        prefix = "db-init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DbInitAutoConfiguration implements InitializingBean {

    private final DbInitProperties properties;
    private final ApplicationContext ctx;
    private final Environment env;
    private final List<Callback> customCallbacks;

    public DbInitAutoConfiguration(DbInitProperties properties, ApplicationContext ctx,
                                   Environment env,
                                   @Autowired(required = false) List<Callback> customCallbacks) {
        this.properties = properties;
        this.ctx = ctx;
        this.env = env;
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
                DataSource ds = ctx.getBean(name, DataSource.class);
                applyMigrations(ds, toFlywayConfig(config), name);
            });
        } else {
            DataSource ds = ctx.getBean(DataSource.class);
            applyMigrations(ds, toFlywayConfig(properties), "primary");
        }

        log.info("db-init Flyway 数据库迁移完成");
    }

    private void applyMigrations(DataSource ds, FlywayConfig fc, String dsName) {
        List<String> locations = resolveLocations(fc.locations);
        if (locations.isEmpty()) {
            locations = List.of("classpath:db/migration");
        }

        List<Callback> callbacks = new ArrayList<>();
        callbacks.add(new DbInitAfterMigrateCallback());
        callbacks.addAll(customCallbacks);

        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations(locations.toArray(new String[0]))
                .table(fc.table)
                .validateOnMigrate(fc.validateOnMigrate)
                .baselineOnMigrate(fc.baselineOnMigrate)
                .baselineVersion(fc.baselineVersion != null ? MigrationVersion.fromVersion(fc.baselineVersion) : null)
                .callbacks(callbacks.toArray(new Callback[0]))
                .load();

        if (properties.isRepair()) {
            log.warn("db-init repair 模式已启用，仅限开发环境使用！");
            flyway.repair();
        }

        MigrateResult result = flyway.migrate();
        String schema = result.schemaName != null ? result.schemaName : "default";
        String version = result.targetSchemaVersion != null ? result.targetSchemaVersion : "baseline";
        log.info("db-init [{}] 迁移完成: schema={} 已执行={} 版本={}",
                dsName, schema, result.migrationsExecuted, version);
    }

    private List<String> resolveLocations(List<String> configured) {
        List<String> result = new ArrayList<>(configured != null ? configured : List.of());
        if (properties.isProfileAware()) {
            for (String profile : env.getActiveProfiles()) {
                String suffix = "-" + profile;
                for (String loc : new ArrayList<>(result)) {
                    if (!loc.endsWith(suffix)) {
                        result.add(loc + suffix);
                    }
                }
            }
        }
        return result;
    }

    private FlywayConfig toFlywayConfig(DbInitProperties p) {
        return new FlywayConfig(
                p.getLocations(),
                p.getTable(),
                p.isValidateOnMigrate(),
                p.isBaselineOnMigrate(),
                p.getBaselineVersion());
    }

    private FlywayConfig toFlywayConfig(DbInitProperties.DatasourceConfig c) {
        return new FlywayConfig(
                c.getLocations(),
                c.getTable() != null ? c.getTable() : properties.getTable(),
                c.isValidateOnMigrate(),
                c.isBaselineOnMigrate(),
                c.getBaselineVersion() != null ? c.getBaselineVersion() : properties.getBaselineVersion());
    }

    private record FlywayConfig(List<String> locations, String table, boolean validateOnMigrate,
                                boolean baselineOnMigrate, String baselineVersion) {}
}
