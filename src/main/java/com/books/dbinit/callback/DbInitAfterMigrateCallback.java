package com.books.dbinit.callback;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

/**
 * Flyway 迁移完成后的日志回调。
 * <p>Flyway 11.13+ 调整了 {@link Callback} 二进制接口，需与运行时 flyway-core 同版本编译。
 */
@Slf4j
public class DbInitAfterMigrateCallback implements Callback {

    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.AFTER_MIGRATE;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        var mi = context.getMigrationInfo();
        if (mi == null) {
            log.info("Flyway 迁移完成: baseline 已建立");
            return;
        }
        log.info("Flyway 迁移完成: version={} description={} script={} executionTime={}ms",
                mi.getVersion() != null ? mi.getVersion().getVersion() : "repeatable",
                mi.getDescription(),
                mi.getScript(),
                mi.getExecutionTime());
    }

    @Override
    public String getCallbackName() {
        return "dbInitAfterMigrate";
    }
}
