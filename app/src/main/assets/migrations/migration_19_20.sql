-- // 1. Create a new table `new_Farms` with `accuracyArray` field
--                db.execSQL(
--                    """
--            CREATE TABLE new_Farms (
--                siteId           INTEGER NOT NULL,
--                remote_id        BLOB    NOT NULL,
--                farmerPhoto      TEXT    NOT NULL,
--                farmerName       TEXT    NOT NULL,
--                memberId         TEXT    NOT NULL,
--                village          TEXT    NOT NULL,
--                district         TEXT    NOT NULL,
--                purchases        REAL,
--                size             REAL    NOT NULL,
--                latitude         TEXT    NOT NULL,
--                longitude        TEXT    NOT NULL,
--                coordinates      TEXT,
--                accuracyArray    TEXT,   -- For storing accuracy array (one element for point, multiple for polygon)
--                synced           INTEGER NOT NULL DEFAULT 0,
--                scheduledForSync INTEGER NOT NULL DEFAULT 0,
--                createdAt        INTEGER NOT NULL,
--                updatedAt        INTEGER NOT NULL,
--                needsUpdate      INTEGER NOT NULL DEFAULT 0,
--                id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
--                FOREIGN KEY (siteId)
--                REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
--                                                    ON DELETE CASCADE
--            )
--        """.trimIndent()
--                )
--
--                // 2. Copy existing data from `Farms` to `new_Farms`, initializing `accuracyArray`
--                db.execSQL(
--                    """
--            INSERT INTO new_Farms (
--                siteId, remote_id, farmerPhoto, farmerName, memberId,
--                village, district, purchases, size, latitude, longitude,
--                coordinates, accuracyArray, synced, scheduledForSync,
--                createdAt, updatedAt, needsUpdate, id
--            )
--            SELECT
--                siteId, remote_id, farmerPhoto, farmerName, memberId,
--                village, district, purchases, size, latitude, longitude,
--                coordinates, '[]' AS accuracyArray, -- Initialize new field as an empty array
--                synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
--            FROM Farms
--        """.trimIndent()
--                )
--
--                // 3. Drop the old `Farms` table
--                db.execSQL("DROP TABLE Farms")
--
--                // 4. Rename the `new_Farms` table to `Farms`
--                db.execSQL("ALTER TABLE new_Farms RENAME TO Farms")
--            }

try {
    // 1. Create a new table `new_Farms` with `accuracyArray` field
    db.execSQL(
        """
        CREATE TABLE new_Farms (
            siteId           INTEGER NOT NULL,
            remote_id        BLOB    NOT NULL,
            farmerPhoto      TEXT    NOT NULL,
            farmerName       TEXT    NOT NULL,
            memberId         TEXT    NOT NULL,
            village          TEXT    NOT NULL,
            district         TEXT    NOT NULL,
            purchases        REAL,
            size             REAL    NOT NULL,
            latitude         TEXT    NOT NULL,
            longitude        TEXT    NOT NULL,
            coordinates      TEXT,
            accuracyArray    TEXT,   -- For storing accuracy array (one element for point, multiple for polygon)
            synced           INTEGER NOT NULL DEFAULT 0,
            scheduledForSync INTEGER NOT NULL DEFAULT 0,
            createdAt        INTEGER NOT NULL,
            updatedAt        INTEGER NOT NULL,
            needsUpdate      INTEGER NOT NULL DEFAULT 0,
            id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            FOREIGN KEY (siteId)
            REFERENCES CollectionSites (siteId) ON UPDATE NO ACTION
                                                ON DELETE CASCADE
        )
        """.trimIndent()
    );
    Log.d("Database", "Table new_Farms created successfully");

    // 2. Copy existing data from `Farms` to `new_Farms`, initializing `accuracyArray`
    db.execSQL(
        """
        INSERT INTO new_Farms (
            siteId, remote_id, farmerPhoto, farmerName, memberId,
            village, district, purchases, size, latitude, longitude,
            coordinates, accuracyArray, synced, scheduledForSync,
            createdAt, updatedAt, needsUpdate, id
        )
        SELECT
            siteId, remote_id, farmerPhoto, farmerName, memberId,
            village, district, purchases, size, latitude, longitude,
            coordinates, '[]' AS accuracyArray, -- Initialize new field as an empty array
            synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
        FROM Farms
        """.trimIndent()
    );
    Log.d("Database", "Data copied from Farms to new_Farms successfully");

    // 3. Drop the old `Farms` table
    db.execSQL("DROP TABLE IF EXISTS Farms");
    Log.d("Database", "Table Farms dropped successfully");

    // 4. Rename the `new_Farms` table to `Farms`
    db.execSQL("ALTER TABLE new_Farms RENAME TO Farms");
    Log.d("Database", "Table new_Farms renamed to Farms successfully");

} catch (SQLException e) {
    Log.e("Database", "Error during table migration", e);
}