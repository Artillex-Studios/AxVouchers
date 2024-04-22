package com.artillexstudios.axvouchers.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DatabaseType {
    H2,
    SQLite,
    MySQL;

    private static final Logger log = LoggerFactory.getLogger(DatabaseType.class);
    private static final DatabaseType[] values = values();

    public static DatabaseType parse(String name) {
        for (DatabaseType value : values) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        log.error("No database type found with id {}! Defaulting to H2!", name);
        return DatabaseType.H2;
    }
}
