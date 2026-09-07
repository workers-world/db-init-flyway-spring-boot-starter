package com.books.dbinit;

import org.springframework.boot.sql.init.dependency.AbstractBeansOfTypeDatabaseInitializerDetector;

import java.util.Set;

/**
 * 向 Spring Boot 的数据库初始化时序机制(spring-boot-sql)登记:
 * {@link DbInitMigrationInitializer} 是数据库初始化器——JPA EntityManagerFactory 等
 * 标注 {@code @DependsOnDatabaseInitialization} 的 Bean 会自动依赖它,
 * 从而保证「先迁移、后查库」。注册于 {@code META-INF/spring.factories}。
 */
public class DbInitInitializerDetector extends AbstractBeansOfTypeDatabaseInitializerDetector {

    @Override
    protected Set<Class<?>> getDatabaseInitializerBeanTypes() {
        return Set.of(DbInitMigrationInitializer.class);
    }
}
