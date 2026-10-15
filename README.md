# DB Init Flyway Spring Boot Starter

应用启动时用 Flyway 执行消费方自带的 SQL 迁移。本仓库**不含**任何业务 DDL；脚本放在你的应用 `classpath:db/migration`（可配置）。

- **groupId / artifactId**: `com.books:db-init-flyway-spring-boot-starter`
- **当前版本**: `1.0.4-SNAPSHOT`
- **要求**: JDK 17+，Spring Boot 4.x

## 依赖

```xml
<dependency>
  <groupId>com.books</groupId>
  <artifactId>db-init-flyway-spring-boot-starter</artifactId>
  <version>1.0.4-SNAPSHOT</version>
</dependency>
```

GitHub Packages：

```xml
<repository>
  <id>github-db-init-flyway</id>
  <url>https://maven.pkg.github.com/workers-world/db-init-flyway-spring-boot-starter</url>
</repository>
```

需自行提供 JDBC `DataSource`。MySQL 驱动 `mysql-connector-j` 为 optional（GPL-2.0 + Universal FOSS Exception），请按需自行引入。

## 与 Spring Boot 自带 Flyway 的关系

本 starter 在应用启动阶段主动跑迁移（`InitializingBean`），并尽量保证先于 JPA 初始化。若同时启用 Boot 原生 `spring.flyway.*`，请避免重复执行同一批脚本。

## 配置（前缀 `db-init`）

| 属性 | 默认 | 说明 |
|------|------|------|
| `db-init.enabled` | `true` | 总开关 |
| `db-init.locations` | `classpath:db/migration` | 迁移路径 |
| `db-init.table` | `flyway_schema_history` | 历史表名 |
| `db-init.validate-on-migrate` | `true` | migrate 前校验 |
| `db-init.profile-aware` | `true` | 按 active profile 追加路径后缀（如 `-dev`） |
| `db-init.baseline-on-migrate` | `false` | 空库 baseline |
| `db-init.baseline-version` | — | baseline 版本 |
| `db-init.repair` + `db-init.repair-confirm` | `false` | **双开关**同时为 true 才 `flyway.repair()`（仅限开发） |

### 单数据源示例

```yaml
db-init:
  enabled: true
  locations:
    - classpath:db/migration
```

### 多数据源

```yaml
db-init:
  locations:
    - classpath:db/migration
  datasources:
    primary:
      enabled: true
    reporting:
      enabled: true
      locations:
        - classpath:db/migration-reporting
      table: flyway_schema_history_reporting
```

per-DS 未配置的 `locations` / `table` / `validate-on-migrate` / `baseline-*` 会继承父级。

## Actuator（可选）

引入 `spring-boot-starter-actuator` 后暴露只读端点 `/actuator/dbinit`，查询 `flyway_schema_history`。

## 构建

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn verify
```

## CI

使用 [workers-world/java-actions](https://github.com/workers-world/java-actions) 的 `java-maven-sdk-ci`（pin `actions/v0.5.0`）。push `dev_*` 分支在 verify 通过后发布到 GitHub Packages。仓内需配置 Secret：`GITHUB_PACKAGES_TOKEN`、`GHA_TOKEN`。
