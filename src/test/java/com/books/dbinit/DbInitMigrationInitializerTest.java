package com.books.dbinit;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DbInitMigrationInitializerTest {

    @Test
    void profileSuffixesAreSingleLevelNoCrossProduct() {
        List<String> locations = DbInitMigrationInitializer.resolveLocations(
                List.of("classpath:db/migration"), true, List.of("dev", "docker"));
        assertThat(locations).containsExactly(
                "classpath:db/migration",
                "classpath:db/migration-dev",
                "classpath:db/migration-docker");
        // 修复前会对多 profile 生成 "classpath:db/migration-dev-docker" 组合目录
        assertThat(locations).noneMatch(loc -> loc.endsWith("-dev-docker"));
    }

    @Test
    void existingSuffixesAreNotDuplicated() {
        List<String> locations = DbInitMigrationInitializer.resolveLocations(
                List.of("classpath:db/migration-dev"), true, List.of("dev"));
        assertThat(locations).containsExactly("classpath:db/migration-dev");
    }

    @Test
    void profileAwareDisabledReturnsBaseLocations() {
        List<String> locations = DbInitMigrationInitializer.resolveLocations(
                List.of("classpath:db/migration"), false, List.of("dev"));
        assertThat(locations).containsExactly("classpath:db/migration");
    }

    @Test
    void emptyConfiguredLocationsReturnsEmptyForDefaultFallback() {
        assertThat(DbInitMigrationInitializer.resolveLocations(null, true, List.of("dev"))).isEmpty();
    }
}
