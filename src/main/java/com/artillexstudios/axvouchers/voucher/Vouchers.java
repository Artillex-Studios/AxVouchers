package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvouchers.config.Config;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class Vouchers {
    private static final ConcurrentHashMap<String, Voucher> VOUCHERS = new ConcurrentHashMap<>();
    private static final List<String> placeholders = new ArrayList<>();
    private static final File VOUCHERS_DIRECTORY = com.artillexstudios.axvouchers.utils.FileUtils.PLUGIN_DIRECTORY.resolve("vouchers/").toFile();
    private static final List<File> FAILED_TO_LOAD = new ArrayList<>();
    private static final Pattern SEMICOLON = Pattern.compile(";");
    private static final Pattern DASH = Pattern.compile("-");

    public static void register(Voucher voucher) {
        if (VOUCHERS.containsKey(voucher.getId())) {
            LogUtils.warn("A voucher with id {} is already registered! Skipping!", voucher.getId());
            return;
        }

        VOUCHERS.put(voucher.getId(), voucher);

        if (Config.debug) {
            LogUtils.info("Loaded voucher with id {}! Vouchers: {}.", voucher.getId(), VOUCHERS);
        }
    }

    public static void deregister(Voucher voucher) {
        VOUCHERS.remove(voucher.getId());

        if (Config.debug) {
            LogUtils.info("Deregistered voucher {}! Vouchers: {}.", voucher.getId(), VOUCHERS);
        }
    }

    public static Voucher parse(String name) {
        return VOUCHERS.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static Set<String> getVoucherNames() {
        return Set.copyOf(VOUCHERS.keySet());
    }

    public static Set<Voucher> getVouchers() {
        return Set.copyOf(VOUCHERS.values());
    }

    public static void loadAll() {
        if (Config.debug) {
            LogUtils.info("Clearing all vouchers and failed to loads!");
        }

        FAILED_TO_LOAD.clear();
        VOUCHERS.clear();

        if (VOUCHERS_DIRECTORY.mkdirs()) {
            com.artillexstudios.axvouchers.utils.FileUtils.copyFromResource("vouchers");
        }

        Collection<File> files = FileUtils.listFiles(VOUCHERS_DIRECTORY, new String[]{"yaml", "yml"}, true);

        if (Config.debug) {
            LogUtils.info("Parsing voucher files: {}!", String.join(", ", files.stream().map(File::getName).toList()));
        }

        for (File file : files) {
            if (!YamlUtils.suggest(file)) {
                FAILED_TO_LOAD.add(file);
                continue;
            }

            com.artillexstudios.axapi.config.Config config = new com.artillexstudios.axapi.config.Config(file);

            // If there is only one voucher in the file
            boolean oneFile = false;
            for (Object key : config.getBackingDocument().getKeys()) {
                if (key.toString().equalsIgnoreCase("item")) {
                    oneFile = true;
                    break;
                }
            }

            // There is only one voucher in the file
            if (oneFile) {
                String id = file.getName().replace(".yml", "").replace(".yaml", "");
                new Voucher(id, config.getBackingDocument());
            } else {
                // There are multiple vouchers in the file
                for (Object key : config.getBackingDocument().getKeys()) {
                    String id = key.toString();
                    new Voucher(id, config.getSection(id));
                }
            }
        }
    }

    public static UUID getUUID(CompoundTag tag) {
        UUID uuid = tag.getUUID("axvouchers-uuid");

        if (Config.debug) {
            LogUtils.info("Getting uuid from item! UUID: {}!", uuid == null ? "---" : uuid.toString());
        }

        return uuid;
    }

    public static TagResolver[] tagResolvers(String rawString) {
        if (rawString.isBlank() || !rawString.contains("-")) {
            return new TagResolver[0];
        }

        String[] split = SEMICOLON.split(rawString);
        TagResolver[] placeholders = new TagResolver[split.length];
        int i = 0;
        for (String placeholder : split) {
            String[] keyValuePair = DASH.split(placeholder);
            placeholders[i] = Placeholder.parsed(keyValuePair[0], keyValuePair[1]);
            i++;
        }

        return placeholders;
    }

    public static String placeholderString(CompoundTag tag) {
        if (!tag.contains("axvouchers-placeholders")) {
            return "";
        }

        String rawString = tag.getString("axvouchers-placeholders");
        if (rawString.isBlank() || !rawString.contains("-")) {
            return "";
        }

        return rawString;
    }

    public static Pair<String, String>[] placeholders(String rawString) {
        if (rawString.isBlank() || !rawString.contains("-")) {
            return new Pair[0];
        }

        String[] split = SEMICOLON.split(rawString);
        Pair<String, String>[] placeholders = new Pair[split.length];
        int i = 0;
        for (String placeholder : split) {
            String[] keyValuePair = DASH.split(placeholder);
            placeholders[i] = Pair.of("<" + keyValuePair[0] + ">", keyValuePair[1]);
            i++;
        }

        return placeholders;
    }

    public static Voucher fromItem(CompoundTag tag) {
        String voucherId = tag.getString("axvouchers-id");
        if (voucherId == null) {
            if (Config.debug) {
                LogUtils.info("Tag does not contain axvouchers-id!");
            }
            return null;
        }

        if (Config.debug) {
            LogUtils.info("Parsing from id {}!", voucherId);
        }
        return parse(voucherId);
    }

    public static List<String> placeholders() {
        return placeholders;
    }

    public static List<File> getFailed() {
        return List.copyOf(FAILED_TO_LOAD);
    }
}
