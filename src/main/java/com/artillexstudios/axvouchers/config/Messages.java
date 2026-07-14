package com.artillexstudios.axvouchers.config;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Messages {
    private static final Logger log = LoggerFactory.getLogger(Messages.class);
    public static String PREFIX = "<#DC143C>AxVouchers <gray>» ";
    public static String RELOAD_SUCCESS = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white> ms!";
    public static String RELOAD_FAIL = "<#ff0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information!";
    public static String VOUCHER_NOT_FOUND = "<#ff0000>There's no registered voucher with name <name>!";
    public static String COOLDOWN = "<#ff0000>You need to wait <white><time></white> more seconds to consume this voucher again!";
    public static String LIST = "<#00ff00>Currently loaded vouchers: <white><vouchers></white>.";
    public static String DUPED_VOUCHER = "<#ff0000>The voucher you have tried using has been removed due to it being duplicated!";
    public static String NO_LOGS = "<#ff0000>No logs found for player named <white><name></white>!";
    public static String OR = " or ";
    public static String GIVE = "<white>You have just given <player> <amount> <name> voucher!";
    public static String RECEIVE = "<white>You were given <amount> <name> voucher!";
    public static String CONFIRM = "<white>Please click again to use <name> voucher!";
    public static String PLACEHOLDERS_NOT_SET = "<red>You need to set the <white><placeholders></white> when giving this voucher!";
    public static List<String> HELP = List.of("", "<#DC143C><b>AxVouchers <white>|</white>", " <gray>- <white>/axvouchers reload <gray>| <white>Reload the plugin", " <gray>- <white>/axvouchers give <player> <voucher> (amount) <gray>| <white>Give a player (amount) vouchers to player <player> of type <voucher>", " <gray>- <white>/axvouchers list <gray>| <white>Lists all of the loaded vouchers", " <gray>- <white>/axvouchers gui <gray>| <white>Open the voucher get gui", " <gray>- <white>/axvouchers logs <player> <gray>| <white>View the voucher use logs of <player>", "");
    // Fail messages
    public static String FAIL_PERMISSION = "<#ff0000>You do not have permission <permission> to use this voucher!";
    public static String FAIL_INVERSE_PERMISSION = "<#ff0000>You have a permission <permission> that prevents you from using this voucher!";
    public static String FAIL_INVERSE_WORLD = "<#ff0000>You are in <world> world in which it's forbidden to use this voucher!";
    public static String FAIL_WORLD = "<#ff0000>You are not in world <world> which allows you to open this voucher in!";
    public static String FAIL_PLACEHOLDER = "<#ff0000>The placeholder parsing did not equal the specified value of <value>!";
    public static String FAIL_INVERSE_PLACEHOLDER = "<#ff0000>The placeholder parsing did equal to the specified value of <value>!";
    public static String FAIL_GAMEMODE = "<#ff0000>Your gamemode is not <gamemode>, so we forbade you to open this voucher!";
    public static String FAIL_INVERSE_GAMEMODE = "<#ff0000>Your gamemode is <gamemode>, in which you can't consume a voucher!";
    private static final Messages INSTANCE = new Messages();
    private Config config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        File file = FileUtils.PLUGIN_DIRECTORY.resolve("messages.yml").toFile();
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                return false;
            }
        }

        if (config != null) {
            config.reload();
        } else {
            config = new Config(file, AxVouchersPlugin.instance().getResource("messages.yml"));
        }

        refreshValues();
        return true;
    }

    private void refreshValues() {
        if (config == null) {
            log.error("Messages was not loaded correctly! Using default values!");
            return;
        }

        PREFIX = config.getString("prefix", PREFIX);
        RELOAD_SUCCESS = config.getString("reload.success", RELOAD_SUCCESS);
        RELOAD_FAIL = config.getString("reload.fail", RELOAD_FAIL);
        VOUCHER_NOT_FOUND = config.getString("voucher-not-found", VOUCHER_NOT_FOUND);
        COOLDOWN = config.getString("cooldown", COOLDOWN);
        LIST = config.getString("list", LIST);
        DUPED_VOUCHER = config.getString("duped-voucher", DUPED_VOUCHER);
        NO_LOGS = config.getString("no-logs", NO_LOGS);
        OR = config.getString("or", OR);
        GIVE = config.getString("give", GIVE);
        RECEIVE = config.getString("receive", RECEIVE);
        CONFIRM = config.getString("confirm", CONFIRM);
        PLACEHOLDERS_NOT_SET = config.getString("placeholders-not-set", PLACEHOLDERS_NOT_SET);
        HELP = config.getStringList("help", HELP);

        FAIL_PERMISSION = config.getString("fail.permission", FAIL_PERMISSION);
        FAIL_INVERSE_PERMISSION = config.getString("fail.inverse-permission", FAIL_INVERSE_PERMISSION);
        FAIL_INVERSE_WORLD = config.getString("fail.inverse-world", FAIL_INVERSE_WORLD);
        FAIL_WORLD = config.getString("fail.world", FAIL_WORLD);
        FAIL_PLACEHOLDER = config.getString("fail.placeholder", FAIL_PLACEHOLDER);
        FAIL_INVERSE_PLACEHOLDER = config.getString("fail.inverse-placeholder", FAIL_INVERSE_PLACEHOLDER);
        FAIL_GAMEMODE = config.getString("fail.gamemode", FAIL_GAMEMODE);
        FAIL_INVERSE_GAMEMODE = config.getString("fail.inverse-gamemode", FAIL_INVERSE_GAMEMODE);
    }
}
