package com.books.dbinit;

import com.books.dbinit.config.DbInitProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DbInitPropertiesTest {

    @Test
    void perDatasourceNullBooleansInheritParent() {
        DbInitProperties parent = new DbInitProperties();
        parent.setValidateOnMigrate(true);
        parent.setBaselineOnMigrate(true);   // 父级开启(消费方场景:存量库基线)
        DbInitProperties.DatasourceConfig ds = new DbInitProperties.DatasourceConfig();
        // 未配置(此前为 primitive false,无法区分「未设置」)→ 必须继承父级
        assertThat(parent.effectiveValidateOnMigrate(ds)).isTrue();
        assertThat(parent.effectiveBaselineOnMigrate(ds)).isTrue();
    }

    @Test
    void perDatasourceExplicitValuesOverrideParent() {
        DbInitProperties parent = new DbInitProperties();
        parent.setBaselineOnMigrate(true);
        DbInitProperties.DatasourceConfig ds = new DbInitProperties.DatasourceConfig();
        ds.setBaselineOnMigrate(false);
        assertThat(parent.effectiveBaselineOnMigrate(ds)).isFalse();
    }

    @Test
    void perDatasourceTableAndLocationsInheritParent() {
        DbInitProperties parent = new DbInitProperties();
        parent.setTable("custom_history");
        parent.setLocations(List.of("classpath:db/migration"));
        DbInitProperties.DatasourceConfig ds = new DbInitProperties.DatasourceConfig();
        assertThat(parent.effectiveTable(ds)).isEqualTo("custom_history");
        assertThat(parent.effectiveLocations(ds)).containsExactly("classpath:db/migration");
        ds.setLocations(List.of("classpath:other/migration"));
        assertThat(parent.effectiveLocations(ds)).containsExactly("classpath:other/migration");
    }

    @Test
    void repairRequiresDoubleConfirmation() {
        DbInitProperties props = new DbInitProperties();
        props.setRepair(true);
        assertThat(props.isRepairEnabled()).isFalse();
        props.setRepairConfirm(true);
        assertThat(props.isRepairEnabled()).isTrue();
    }
}
