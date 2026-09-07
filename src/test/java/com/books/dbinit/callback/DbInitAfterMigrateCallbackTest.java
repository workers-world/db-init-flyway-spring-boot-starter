package com.books.dbinit.callback;

import org.flywaydb.core.api.callback.Event;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DbInitAfterMigrateCallbackTest {

    private final DbInitAfterMigrateCallback callback = new DbInitAfterMigrateCallback();

    @Test
    void supportsPerMigrationAndBatchEvents() {
        assertThat(callback.supports(Event.AFTER_MIGRATE_APPLIED, null)).isTrue();
        assertThat(callback.supports(Event.AFTER_MIGRATE, null)).isTrue();
        // 其它事件(如 BEFORE_MIGRATE / AFTER_MIGRATE_ERROR)不订阅
        assertThat(callback.supports(Event.BEFORE_MIGRATE, null)).isFalse();
    }

    @Test
    void callbackNameIsStable() {
        assertThat(callback.getCallbackName()).isEqualTo("dbInitAfterMigrate");
    }
}
