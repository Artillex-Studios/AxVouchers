package com.artillexstudios.axvouchers.config;

import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.database.DatabaseType;
import com.artillexstudios.axvouchers.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);
    public static boolean DUPE_PROTECTION = true;
    public static boolean PREVENT_CRAFTS = true;
    public static boolean SEND_REQUIREMENT_FAIL = true;
    public static DatabaseType DATABASE_TYPE = DatabaseType.H2;
    public static String DATABASE_ADDRESS = "127.0.0.1";
    public static int DATABASE_PORT = 3306;
    public static String DATABASE_DATABASE = "admin";
    public static String DATABASE_USERNAME = "admin";
    public static String DATABASE_PASSWORD = "admin";
    public static int DATABASE_MAXIMUM_POOL_SIZE = 10;
    public static int DATABASE_MINIMUM_IDLE = 10;
    public static int DATABASE_MAXIMUM_LIFETIME = 1800000;
    public static int DATABASE_KEEPALIVE_TIME = 0;
    public static int DATABASE_CONNECTION_TIMEOUT = 5000;
    public static boolean DEBUG = false;
    private static final Config INSTANCE = new Config();
    private com.artillexstudios.axapi.config.Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (config != null) {
            config.reload();
        } else {
            config = new com.artillexstudios.axapi.config.Config(file, AxVouchersPlugin.getInstance().getResource("config.yml"));
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Config was not loaded correctly! Using default values!");
            return;
        }

        DUPE_PROTECTION = config.getBoolean("dupe-protection", DUPE_PROTECTION);
        PREVENT_CRAFTS = config.getBoolean("prevent-crafts", PREVENT_CRAFTS);
        SEND_REQUIREMENT_FAIL = config.getBoolean("send-requirement-fail", SEND_REQUIREMENT_FAIL);
        DATABASE_TYPE = DatabaseType.parse(config.getString("database.type"));
        DATABASE_ADDRESS = config.getString("database.address", DATABASE_ADDRESS);
        DATABASE_PORT = config.getInt("database.port", DATABASE_PORT);
        DATABASE_DATABASE = config.getString("database.database", DATABASE_DATABASE);
        DATABASE_USERNAME = config.getString("database.username", DATABASE_USERNAME);
        DATABASE_PASSWORD = config.getString("database.password", DATABASE_PASSWORD);
        DATABASE_MAXIMUM_POOL_SIZE = config.getInt("database.pool.maximum-pool-size", DATABASE_MAXIMUM_POOL_SIZE);
        DATABASE_MINIMUM_IDLE = config.getInt("database.pool.minimum-idle", DATABASE_MINIMUM_IDLE);
        DATABASE_MAXIMUM_LIFETIME = config.getInt("database.pool.maximum-lifetime", DATABASE_MAXIMUM_LIFETIME);
        DATABASE_KEEPALIVE_TIME = config.getInt("database.pool.keepalive-time", DATABASE_KEEPALIVE_TIME);
        DATABASE_CONNECTION_TIMEOUT = config.getInt("database.pool.connection-timeout", DATABASE_CONNECTION_TIMEOUT);
        DEBUG = config.getBoolean("debug", DEBUG);
    }
}
