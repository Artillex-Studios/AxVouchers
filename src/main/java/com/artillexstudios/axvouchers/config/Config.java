package com.artillexstudios.axvouchers.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.database.DatabaseConfig;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config implements ConfigurationPart {
    @Comment("""
            WARNING! This only works for UNSTACKABLE vouchers!
            With this setting, we give each voucher an UUID,
            and prohibit the reuse of vouchers
            
            PLEASE READ! This setting does NOT support copying items in creative!
            If you want to do that, don't. Use the command to give the player a new voucher.
            """)
    public static boolean dupeProtection = true;
    @Comment("Whether to prevent the use of vouchers in crafting recipes")
    public static boolean preventCrafts = true;
    @Comment("Whether to use packet items for vouchers.")
    public static boolean usePacketItems = true;
    @Comment("If we should send a message when a requirement fails")
    public static boolean sendRequirementFail = true;
    public static DatabaseConfig database = new DatabaseConfig();
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code
            """)
    public static boolean debug = false;
    public static int configVersion = 1;
    private static final Config INSTANCE = new Config();
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxVouchersPlugin.instance().getResource("config.yml"))
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
