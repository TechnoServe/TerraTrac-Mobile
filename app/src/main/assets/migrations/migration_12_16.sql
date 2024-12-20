-- Step 1: Create a new temporary table with the updated schema
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
    synced           INTEGER NOT NULL DEFAULT 0,
    scheduledForSync INTEGER NOT NULL DEFAULT 0,
    createdAt        INTEGER NOT NULL,
    updatedAt        INTEGER NOT NULL,
    needsUpdate      INTEGER NOT NULL DEFAULT 0,
    id               INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    FOREIGN KEY (siteId)
        REFERENCES CollectionSites (siteId)
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

-- Step 2: Copy data from the old table to the new table, setting needsUpdate to 0
INSERT INTO new_Farms (
    siteId, remote_id, farmerPhoto, farmerName, memberId,
    village, district, purchases, size, latitude, longitude,
    coordinates, synced, scheduledForSync, createdAt, updatedAt, needsUpdate, id
)
SELECT
    siteId, remote_id, farmerPhoto, farmerName, memberId,
    village, district, purchases, size, latitude, longitude,
    coordinates, synced, scheduledForSync, createdAt, updatedAt, 0 AS needsUpdate, id
FROM Farms;

-- Step 3: Drop the old table
DROP TABLE Farms;

-- Step 4: Rename the new table to the original table name
ALTER TABLE new_Farms RENAME TO Farms;

