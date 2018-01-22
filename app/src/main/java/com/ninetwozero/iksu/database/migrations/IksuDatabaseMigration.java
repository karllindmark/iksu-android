package com.ninetwozero.iksu.database.migrations;

import com.ninetwozero.iksu.utils.Constants;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class IksuDatabaseMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            migrateSchemaFrom0To1(schema);
            oldVersion++;
        }
    }

    private void migrateSchemaFrom0To1(final RealmSchema schema) {
        schema.get("Workout")
            .addField("monitoring", boolean.class)
            .addField("checkedIn", boolean.class);
            .addField(Constants.CHECKED_IN, boolean.class);
    }
}
