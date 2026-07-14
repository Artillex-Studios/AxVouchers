package com.artillexstudios.axvouchers;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseTypes;
import com.artillexstudios.axapi.database.impl.H2DatabaseType;
import com.artillexstudios.axapi.database.impl.MySQLDatabaseType;
import com.artillexstudios.axapi.database.impl.SQLiteDatabaseType;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.items.PacketItemModifier;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axvouchers.command.VoucherCommand;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.listeners.CraftListener;
import com.artillexstudios.axvouchers.listeners.FireworkListener;
import com.artillexstudios.axvouchers.listeners.PlayerListener;
import com.artillexstudios.axvouchers.listeners.VoucherUseListener;
import com.artillexstudios.axvouchers.voucher.VoucherItemModifier;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;

public class AxVouchersPlugin extends AxPlugin {
    private static AxVouchersPlugin INSTANCE;
    private DatabaseHandler databaseHandler;
    private VoucherCommand command;
    private DataHandler handler;

    public static AxVouchersPlugin instance() {
        return INSTANCE;
    }

    @Override
    public void updateFlags() {
        FeatureFlags.ENABLE_PACKET_LISTENERS.set(true);
    }

    @Override
    public void load() {
        INSTANCE = this;
        DatabaseTypes.register(new H2DatabaseType("com.artillexstudios.axvouchers.libs.h2"), true);
        DatabaseTypes.register(new SQLiteDatabaseType());
        DatabaseTypes.register(new MySQLDatabaseType());
        Config.reload();

        Config.database.tablePrefix("");
        this.databaseHandler = new DatabaseHandler(Config.database);
        this.handler = new DataHandler(this.databaseHandler);
        this.command = new VoucherCommand(this, this.handler);

        this.command.load();
    }

    @Override
    public void enable() {
        this.reload();
        this.handler.setup();
        Bukkit.getPluginManager().registerEvents(new FireworkListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoucherUseListener(this.handler), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        if (Config.preventCrafts) {
            Bukkit.getPluginManager().registerEvents(new CraftListener(), this);
        }

        if (Config.usePacketItems) {
            PacketItemModifier.registerModifierListener(new VoucherItemModifier());
        }

        loadCommands();
    }

    private void loadCommands() {
        CommandAPI.onEnable();

        this.command.register();
    }

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        manager.repository("https://repo.codemc.org/repository/maven-public/");
        manager.dependency("com{}h2database:h2:2.3.232");
        manager.dependency("dev{}triumphteam:triumph-gui:3.1.7");
        manager.dependency("com{}zaxxer:HikariCP:5.0.1");

        manager.relocate("dev{}triumphteam", "com.artillexstudios.axvouchers.libs.triumphgui");
        manager.relocate("org{}h2", "com.artillexstudios.axvouchers.libs.h2");
        manager.relocate("com{}zaxxer", "com.artillexstudios.axvouchers.libs.hikaricp");
    }

    @Override
    public void reload() {
        Config.reload();
        Messages.reload();

        Vouchers.loadAll();
    }

    @Override
    public void disable() {
        CommandAPI.onDisable();
        this.databaseHandler.close();
    }

    public DataHandler handler() {
        return this.handler;
    }
}
