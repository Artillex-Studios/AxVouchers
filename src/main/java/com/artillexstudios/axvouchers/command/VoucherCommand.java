package com.artillexstudios.axvouchers.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.hologram.Hologram;
import com.artillexstudios.axapi.hologram.HologramLine;
import com.artillexstudios.axapi.hologram.HologramPage;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.database.log.VoucherLog;
import com.artillexstudios.axvouchers.utils.FileUtils;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Command({"voucher", "vouchers", "axvoucher", "axvouchers"})
public class VoucherCommand {

    @Subcommand("give")
    @CommandPermission("axvouchers.command.give")
    public void give(CommandSender sender, Player player, Voucher voucher, @Optional @Default("1") int amount) {
        TagResolver.Single amountPlaceholder = Placeholder.parsed("amount", String.valueOf(amount));
        TagResolver.Single playerPlaceholder = Placeholder.parsed("player", MiniMessage.miniMessage().serialize(voucher.getName()));
        TagResolver.Single name = Placeholder.parsed("name", MiniMessage.miniMessage().serialize(voucher.getName()));

        ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), List.of(voucher.getItemStack(amount)), player.getLocation());
        sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.GIVE, amountPlaceholder, playerPlaceholder, name));

        if (!Messages.RECEIVE.isBlank()) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RECEIVE, amountPlaceholder, name));
        }
    }

    @Subcommand("reload")
    @CommandPermission("axvouchers.command.reload")
    public void reload(CommandSender sender) {
        long start = System.currentTimeMillis();
        List<File> failed = new ArrayList<>();

        if (!Config.reload()) {
            failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile());
        }
        if (!Messages.reload()) {
            failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("messages.yml").toFile());
        }

        Vouchers.loadAll();
        failed.addAll(Vouchers.getFailed());

        if (failed.isEmpty()) {
            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RELOAD_SUCCESS, Placeholder.parsed("time", String.valueOf(System.currentTimeMillis() - start))));
        } else {
            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RELOAD_FAIL, Placeholder.parsed("time", String.valueOf(System.currentTimeMillis() - start)), Placeholder.parsed("files", String.join(", ", failed.stream().map(File::getName).toList()))));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateInventory();
        }
    }

    @Subcommand("list")
    @CommandPermission("axvouchers.command.list")
    public void list(CommandSender sender) {
        sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.LIST, Placeholder.parsed("vouchers", String.join(", ", Vouchers.getVoucherNames()))));
    }

    @Subcommand("gui")
    @CommandPermission("axvouchers.command.gui")
    public void gui(Player sender) {
        PaginatedGui gui = Gui.paginated()
                .disableAllInteractions()
                .title(MiniMessage.miniMessage().deserialize("<#DC143C>Vouchers"))
                .pageSize(21)
                .rows(5)
                .create();

        new GuiFiller(gui).fillBorder(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        gui.setItem(38, new GuiItem(new ItemBuilder(Material.ARROW).setName("<#DC143C>Previous page").get(), event -> {
            gui.previous();
        }));

        gui.setItem(42, new GuiItem(new ItemBuilder(Material.ARROW).setName("<#DC143C>Next page").get(), event -> {
            gui.next();
        }));

        for (Voucher voucher : Vouchers.getVouchers()) {
            ItemStack itemStack = voucher.getForGUI();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                continue;
            }

            List<String> itemLore = meta.getLore();
            ItemBuilder builder = new ItemBuilder(itemStack);
            List<String> lore = new ArrayList<>(itemLore == null ? List.of() : itemLore);
            lore.add("");
            lore.add("<#DC143C>Left click to get!");
            builder.setLore(lore);

            gui.addItem(new GuiItem(builder.get(), event -> {
                ContainerUtils.INSTANCE.addOrDrop(sender.getInventory(), List.of(voucher.getItemStack(1)), sender.getLocation());
            }));
        }

        gui.open(sender);
    }

    @DefaultFor({"~", "~ help"})
    @CommandPermission("axvouchers.command.help")
    public void help(CommandSender sender) {
        for (String line : Messages.HELP) {
            sender.sendMessage(StringUtils.formatToString(line));
        }
    }

    @Subcommand("logs")
    @CommandPermission("axvouchers.command.logs")
    public void logs(Player sender, OfflinePlayer user) {
        PaginatedGui gui = Gui.paginated()
                .disableAllInteractions()
                .title(MiniMessage.miniMessage().deserialize("<#DC143C>Log viewer"))
                .pageSize(21)
                .rows(5)
                .create();

        new GuiFiller(gui).fillBorder(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        gui.setItem(38, new GuiItem(new ItemBuilder(Material.ARROW).setName("<#DC143C>Previous page").get(), event -> {
            gui.previous();
        }));

        gui.setItem(42, new GuiItem(new ItemBuilder(Material.ARROW).setName("<#DC143C>Next page").get(), event -> {
            gui.next();
        }));

        VoucherLog log = DataHandler.getInstance().getLogs(user.getName());
        if (log == null || log.getEntries().isEmpty()) {
            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.NO_LOGS, Placeholder.parsed("name", user.getName())));
            return;
        }

        for (VoucherLog.Entry entry : log.getEntries()) {
            Voucher voucher = Vouchers.parse(entry.type());
            ItemStack itemStack = voucher == null ? new ItemBuilder(Material.BEDROCK).setName(entry.type()).get() : voucher.getForGUI();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                continue;
            }

            List<String> itemLore = meta.getLore();
            ItemBuilder builder = new ItemBuilder(itemStack);
            List<String> lore = new ArrayList<>(itemLore == null ? List.of() : itemLore);
            lore.add("");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss");
            lore.add("<white>Time: <#DC143C>" + format.format(entry.time()));
            lore.add("<white>UUID: <#DC143C>" + entry.uuid().toString());
            lore.add("<white>Remove reason: <#DC143C>" + entry.duped());
            lore.add("<white>ID: <#DC143C>" + entry.id());
            builder.setLore(lore);

            gui.addItem(new GuiItem(builder.get()));
        }

        gui.open(sender);
    }

    private PacketEntity entity;

    @Subcommand("test")
    public void test(Player sender) {
        entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, sender.getLocation());
        entity.onInteract(event -> {
            event.getPlayer().sendMessage("Hii!");
        });
        System.out.println(entity.meta().getClass());
        entity.meta().glowing(true);
        entity.meta().customNameVisible(true);
        entity.meta().invisible(true);
        entity.meta().name(StringUtils.format("<red>Bruh"));
        entity.spawn();
    }

    @Subcommand("test2")
    public void test2(Player sender) {
        Hologram hologram = new Hologram(sender.getLocation(), "hologram");
        hologram.addLine("&#ff0000Sziaaa!", HologramLine.Type.TEXT);
        hologram.addLine(WrappedItemStack.wrap(new ItemStack(Material.ARROW)).toSNBT(), HologramLine.Type.ITEM_STACK);
    }

    @Subcommand("test3")
    public void test3(Player sender) {
        entity.teleport(sender.getLocation());
    }
}
