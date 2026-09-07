package com.books.dbinit.callback;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

/**
 * Flyway 迁移日志回调。
 * <p>历史版本只订阅 {@code AFTER_MIGRATE}(整批结束事件,此时 migrationInfo 为 null),
 * 导致逐条迁移日志永不输出、且误报「baseline 已建立」。现同时订阅:</p>
 * <ul>
 *   <li>{@code AFTER_MIGRATE_APPLIED}:每条迁移执行后,输出 version/description/耗时;</li>
 *   <li>{@code AFTER_MIGRATE}:整批结束,输出汇总。</li>
 * </ul>
 * <p>Flyway 11.13+ 调整了 {@link Callback} 二进制接口，需与运行时 flyway-core 同版本编译。</p>
 */
@Slf4j
public class DbInitAfterMigrateCallback implements Callback {

    @Override
    public boolean supports(Event event, Context context) {
        return event == Event.AFTER_MIGRATE_APPLIED || event == Event.AFTER_MIGRATE;
    }

    @Override
    public boolean canHandleInTransaction(Event event, Context context) {
        return true;
    }

    @Override
    public void handle(Event event, Context context) {
        if (event == Event.AFTER_MIGRATE_APPLIED) {
            var mi = context.getMigrationInfo();
            if (mi == null) {
                return;
            }
            log.info("Flyway 迁移已应用: version={} description={} script={} executionTime={}ms",
                    mi.getVersion() != null ? mi.getVersion().getVersion() : "repeatable",
                    mi.getDescription(),
                    mi.getScript(),
                    mi.getExecutionTime());
            return;
        }
        log.info("Flyway 迁移批次执行完成");
    }

    @Override
    public String getCallbackName() {
        return "dbInitAfterMigrate";
    }
}
