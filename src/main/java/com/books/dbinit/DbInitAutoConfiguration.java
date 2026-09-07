package com.books.dbinit;

import com.books.dbinit.actuator.DbInitEndpoint;
import com.books.dbinit.config.DbInitProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Function;

/**
 * db-init 自动配置。
 *
 * <p>相对历史实现的变化:</p>
 * <ul>
 *   <li>不再在自动配置类上直接实现 {@code InitializingBean}:迁移逻辑移入
 *       {@link DbInitMigrationInitializer} 独立 Bean,借助 spring.factories 注册的
 *       {@link DbInitInitializerDetector} 获得「JPA 等 Bean 依赖数据库初始化」的时序契约。</li>
 *   <li>不再通过 jar 内隐藏的 {@code application.properties} 设置
 *       {@code spring.flyway.enabled=false}:Boot 4 的 Flyway 自动配置位于独立模块
 *       (spring-boot-flyway),本 starter 不与其冲突;若消费方同时引入该模块,
 *       应自行通过 {@code spring.autoconfigure.exclude} 二选一,避免双重迁移。</li>
 * </ul>
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(DbInitProperties.class)
@ConditionalOnProperty(
        prefix = "db-init", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DbInitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DbInitMigrationInitializer dbInitMigrationInitializer(
            DataSource primaryDataSource,
            ApplicationContext applicationContext,
            DbInitProperties properties,
            Environment environment,
            @Autowired(required = false) List<org.flywaydb.core.api.callback.Callback> customCallbacks) {
        Function<String, DataSource> lookup = name -> "primary".equals(name)
                ? primaryDataSource
                : applicationContext.getBean(name, DataSource.class);
        return new DbInitMigrationInitializer(lookup, properties,
                List.of(environment.getActiveProfiles()), customCallbacks);
    }

    @Bean
    @ConditionalOnClass(Endpoint.class)
    @ConditionalOnAvailableEndpoint(endpoint = DbInitEndpoint.class)
    public DbInitEndpoint dbInitEndpoint(ObjectProvider<DataSource> dataSource, DbInitProperties properties) {
        return new DbInitEndpoint(dataSource.getObject(), properties);
    }
}
