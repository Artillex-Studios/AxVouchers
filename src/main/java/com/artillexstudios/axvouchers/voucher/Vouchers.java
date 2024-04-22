package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.libs.yamlassist.YamlAssist;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axvouchers.config.Config;
import org.apache.commons.io.FileUtils;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Vouchers {
    private static final ConcurrentHashMap<String, Voucher> VOUCHERS = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Vouchers.class);
    private static final File VOUCHERS_DIRECTORY = com.artillexstudios.axvouchers.utils.FileUtils.PLUGIN_DIRECTORY.resolve("vouchers/").toFile();
    private static final List<File> FAILED_TO_LOAD = new ArrayList<>();

    public static void register(Voucher voucher) {
        if (VOUCHERS.containsKey(voucher.getId())) {
            log.warn("A voucher with id {} is already registered! Skipping!", voucher.getId());
            return;
        }

        VOUCHERS.put(voucher.getId(), voucher);

        if (Config.DEBUG) {
            log.info("Loaded voucher with id {}! Vouchers: {}.", voucher.getId(), VOUCHERS);
        }
    }

    public static void deregister(Voucher voucher) {
        VOUCHERS.remove(voucher.getId());

        if (Config.DEBUG) {
            log.info("Deregistered voucher {}! Vouchers: {}.", voucher.getId(), VOUCHERS);
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
        if (Config.DEBUG) {
            log.info("Clearing all vouchers and failed to loads!");
        }

        FAILED_TO_LOAD.clear();
        VOUCHERS.clear();

        if (VOUCHERS_DIRECTORY.mkdirs()) {
            com.artillexstudios.axvouchers.utils.FileUtils.copyFromResource("vouchers");
        }

        Collection<File> files = FileUtils.listFiles(VOUCHERS_DIRECTORY, new String[]{"yaml", "yml"}, true);

        if (Config.DEBUG) {
            log.info("Parsing voucher files: {}!", String.join(", ", files.stream().map(File::getName).toList()));
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

    public static UUID getUUID(ItemStack item) {
        WrappedItemStack wrappedItemStack = WrappedItemStack.wrap(item);
        if (!wrappedItemStack.hasTag()) {
            return null;
        }

        CompoundTag tag = wrappedItemStack.getCompoundTag();
        UUID uuid = tag.getUUID("axvouchers-uuid");

        if (Config.DEBUG) {
            log.info("Getting uuid from item! UUID: {}!", uuid == null ? "---" : uuid.toString());
        }

        return uuid;
    }

    public static Voucher fromItem(ItemStack item) {
        WrappedItemStack wrappedItemStack = WrappedItemStack.wrap(item);
        if (!wrappedItemStack.hasTag()) {
            return null;
        }

        CompoundTag tag = wrappedItemStack.getCompoundTag();
        if (!tag.contains("axvouchers-id")) {
            if (Config.DEBUG) {
                log.info("Tag does not contain axvouchers-id!");
            }
            return null;
        }

        String voucherId = tag.getString("axvouchers-id");
        if (Config.DEBUG) {
            log.info("Parsing from id {}!", voucherId);
        }
        return parse(voucherId);
    }

    public static List<File> getFailed() {
        return List.copyOf(FAILED_TO_LOAD);
    }
}
