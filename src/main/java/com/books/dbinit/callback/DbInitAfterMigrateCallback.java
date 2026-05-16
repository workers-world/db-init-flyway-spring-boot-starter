package com.books.dbinit.callback;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

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
