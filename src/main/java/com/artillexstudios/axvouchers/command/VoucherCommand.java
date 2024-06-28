package com.artillexstudios.axvouchers.command;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.command.argument.Arguments;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.gui.VoucherGUI;
import com.artillexstudios.axvouchers.gui.VoucherLogGUI;
import com.artillexstudios.axvouchers.utils.FileUtils;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MapArgumentBuilder;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public enum VoucherCommand {
    INSTANCE;

    public void load(JavaPlugin plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).skipReloadDatapacks(true).setNamespace("axvouchers"));
    }

    public void register() {
        new CommandTree("voucher")
                .withAliases("vouchers", "axvoucher", "axvouchers")
                .then(new LiteralArgument("give")
                        .withPermission("axvouchers.command.give")
                        .then(new PlayerArgument("player")
                                .then(Arguments.INSTANCE.voucher("voucher")
                                        .then(new IntegerArgument("amount")
                                                .then(new MapArgumentBuilder<String, String>("placeholders")
                                                        .withKeyMapper(s -> s)
                                                        .withValueMapper(s -> s)
                                                        .withKeyList(Vouchers.placeholders())
                                                        .withoutValueList(true)
                                                        .build()
                                                        .executes(context -> {
                                                            give(context.sender(), context.args().getUnchecked("player"), context.args().getUnchecked("voucher"), context.args().getUnchecked("amount"), context.args().getUnchecked("placeholders"));
                                                        })
                                                )
                                                .executes(context -> {
                                                    give(context.sender(), context.args().getUnchecked("player"), context.args().getUnchecked("voucher"), context.args().getUnchecked("amount"), null);
                                                })
                                        )
                                        .executes(context -> {
                                            give(context.sender(), context.args().getUnchecked("player"), context.args().getUnchecked("voucher"), 1, null);
                                        })
                                )
                        )
                )
                .then(new LiteralArgument("reload")
                        .withPermission("axvouchers.command.reload")
                        .executes(context -> {
                            long start = System.currentTimeMillis();
                            List<File> failed = new ArrayList<>();

                            Vouchers.placeholders().clear();
                            if (!Config.reload()) {
                                failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("config.yml").toFile());
                            }
                            if (!Messages.reload()) {
                                failed.add(FileUtils.PLUGIN_DIRECTORY.resolve("messages.yml").toFile());
                            }

                            Vouchers.loadAll();
                            failed.addAll(Vouchers.getFailed());

                            for (Voucher voucher : Vouchers.getVouchers()) {
                                Vouchers.placeholders().addAll(voucher.placeholders());
                            }

                            if (failed.isEmpty()) {
                                context.sender().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RELOAD_SUCCESS, Placeholder.parsed("time", String.valueOf(System.currentTimeMillis() - start))));
                            } else {
                                context.sender().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RELOAD_FAIL, Placeholder.parsed("time", String.valueOf(System.currentTimeMillis() - start)), Placeholder.parsed("files", String.join(", ", failed.stream().map(File::getName).toList()))));
                            }

                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.updateInventory();
                            }
                        })
                )
                .then(new LiteralArgument("list")
                        .withPermission("axvouchers.command.list")
                        .executes(context -> {
                            context.sender().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.LIST, Placeholder.parsed("vouchers", String.join(", ", Vouchers.getVoucherNames()))));
                        })
                )
                .then(new LiteralArgument("logs")
                        .withPermission("axvouchers.command.logs")
                        .then(new OfflinePlayerArgument("player")
                                .executesPlayer(context -> {
                                    Player sender = context.sender();
                                    OfflinePlayer user = context.args().getUnchecked("player");

                                    VoucherLogGUI.INSTANCE.open(sender, user);
                                })
                        )
                )
                .then(new LiteralArgument("gui")
                        .withPermission("axvouchers.command.gui")
                        .executesPlayer(context -> {
                            Player sender = context.sender();

                            VoucherGUI.INSTANCE.open(sender);
                        })
                )
                .register();
    }

    public void give(CommandSender sender, Player player, Voucher voucher, Integer amount, LinkedHashMap<String, String> placeholders) {
        if ((placeholders == null && !voucher.placeholders().isEmpty()) || (placeholders != null && placeholders.size() < voucher.placeholders().size())) {
            List<String> required = new ArrayList<>(voucher.placeholders());
            if (placeholders != null) {
                required.removeAll(placeholders.values());
            }
            sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.PLACEHOLDERS_NOT_SET, Placeholder.unparsed("placeholders", String.join(", ", required))));
            return;
        }

        ItemStack itemStack = voucher.getItemStack(amount, placeholders);
        TagResolver[] tagResolvers = null;
        if (!voucher.placeholders().isEmpty()) {
            tagResolvers = Vouchers.tagResolvers(Vouchers.placeholderString(WrappedItemStack.wrap(itemStack).get(DataComponents.customData())));
        }

        TagResolver.Single amountPlaceholder = Placeholder.parsed("amount", String.valueOf(amount));
        TagResolver.Single playerPlaceholder = Placeholder.parsed("player", player.getName());
        TagResolver.Single name = Placeholder.parsed("name", MiniMessage.miniMessage().serialize(StringUtils.format(voucher.getName(), tagResolvers == null ? new TagResolver[0] : tagResolvers)));

        ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), List.of(itemStack), player.getLocation());
        sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.GIVE, amountPlaceholder, playerPlaceholder, name));

        if (!Messages.RECEIVE.isBlank()) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.RECEIVE, amountPlaceholder, name));
        }
    }
}
