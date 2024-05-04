package com.artillexstudios.axvouchers;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.items.PacketItemModifier;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.libs.libby.Library;
import com.artillexstudios.axapi.libs.libby.logging.LogLevel;
import com.artillexstudios.axapi.utils.FeatureFlags;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.command.VoucherCommand;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.database.impl.H2DataHandler;
import com.artillexstudios.axvouchers.database.impl.MySQLDataHandler;
import com.artillexstudios.axvouchers.database.impl.SQLiteDataHandler;
import com.artillexstudios.axvouchers.listeners.CraftListener;
import com.artillexstudios.axvouchers.listeners.FireworkListener;
import com.artillexstudios.axvouchers.listeners.PlayerListener;
import com.artillexstudios.axvouchers.listeners.VoucherUseListener;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.VoucherItemModifier;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.exception.CommandErrorException;

public class AxVouchersPlugin extends AxPlugin {
    private static AxVouchersPlugin INSTANCE;
    private DataHandler dataHandler;

    public static AxVouchersPlugin getInstance() {
        return INSTANCE;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    @Override
    public void updateFlags() {
        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
        FeatureFlags.DEBUG.set(true);
    }

    @Override
    public void enable() {
        INSTANCE = this;

        loadLibraries();

        reload();

        dataHandler = switch (Config.DATABASE_TYPE) {
            case H2 -> new H2DataHandler();
            case SQLite -> new SQLiteDataHandler();
            case MySQL -> new MySQLDataHandler();
        };
        dataHandler.setup();

        Bukkit.getPluginManager().registerEvents(new FireworkListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoucherUseListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        if (Config.PREVENT_CRAFTS) {
            Bukkit.getPluginManager().registerEvents(new CraftListener(), this);
        }

        PacketItemModifier.registerModifierListener(new VoucherItemModifier());

        loadCommands();
    }

    private void loadCommands() {
        BukkitCommandHandler handler = BukkitCommandHandler.create(this);

        handler.registerValueResolver(Voucher.class, resolver -> {
            String voucherName = resolver.popForParameter();
            Voucher voucher = Vouchers.parse(voucherName);
            if (voucher == null) {
                resolver.actor().as(BukkitCommandActor.class).getSender().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.VOUCHER_NOT_FOUND, Placeholder.parsed("name", voucherName)));
                throw new CommandErrorException();
            }

            return voucher;
        });

        handler.getAutoCompleter().registerParameterSuggestions(Voucher.class, (args, sender, command) -> Vouchers.getVoucherNames());

        handler.register(new VoucherCommand());

        if (handler.isBrigadierSupported()) {
            handler.registerBrigadier();
        }
    }

    private void loadLibraries() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
        libraryManager.addMavenCentral();
        Library sqLite = Library.builder()
                .groupId("org.xerial")
                .artifactId("sqlite-jdbc")
                .version("3.42.0.0")
                .relocate("org{}sqlite", "com.artillexstudios.axvouchers.libs.sqlite")
                .build();

        Library h2 = Library.builder()
                .groupId("com.h2database")
                .artifactId("h2")
                .version("2.2.220")
                .relocate("org{}h2", "com.artillexstudios.axvouchers.libs.h2")
                .build();

        libraryManager.setLogLevel(LogLevel.DEBUG);
        libraryManager.loadLibrary(sqLite);
        libraryManager.loadLibrary(h2);
    }

    @Override
    public void reload() {
        Config.reload();
        Messages.reload();

        Vouchers.loadAll();
    }

    @Override
    public void disable() {
        dataHandler.disable();
        DataHandler.DATA_THREAD.stop();
    }
}
