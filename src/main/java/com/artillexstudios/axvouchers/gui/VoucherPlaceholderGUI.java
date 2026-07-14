package com.artillexstudios.axvouchers.gui;

import com.artillexstudios.axapi.gui.SignInput;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.voucher.Voucher;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoucherPlaceholderGUI {
    private final Player sender;
    private final Voucher voucher;
    private final LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
    private int amount = 1;

    public VoucherPlaceholderGUI(Player sender, Voucher voucher) {
        this.sender = sender;
        this.voucher = voucher;

        for (String placeholder : voucher.placeholders()) {
            this.valuesMap.put(placeholder, "");
        }
    }

    public void open() {
        Gui placeholderSelector = Gui.gui()
                .disableAllInteractions()
                .title(MiniMessage.miniMessage().deserialize("<#DC143C>Voucher placeholder editor"))
                .rows(5)
                .create();

        new GuiFiller(placeholderSelector).fillBorder(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        for (String placeholder : this.voucher.placeholders()) {
            placeholderSelector.addItem(new GuiItem(ItemBuilder.create(Material.OAK_SIGN).setName("<yellow>" + placeholder).setLore(List.of("", " <gray>- <white><value>", "<yellow>Click to edit!"), Placeholder.unparsed("value", this.valuesMap.getOrDefault(placeholder, ""))).get(), event -> {
                SignInput input = new SignInput.Builder()
                        .setLines(List.of(Component.empty(), Component.text("^^^^^^^^").color(NamedTextColor.WHITE), Component.text("Enter a new value for " + placeholder).color(NamedTextColor.WHITE), Component.empty()).toArray(new Component[0]))
                        .setHandler(((player, lines) -> {
                            this.valuesMap.put(placeholder, lines[0]);

                            Scheduler.get().run(task -> {
                                open();
                            });
                        }))
                        .build(this.sender);

                input.open();
            }));
        }

        placeholderSelector.setItem(43, new GuiItem(ItemBuilder.create(Material.ANVIL).setName("<#00FF00>Amount selector").setLore(List.of("", "<#00ff00>Currently selected: <amount>"), Placeholder.unparsed("amount", String.valueOf(this.amount))).get(), event -> {
            SignInput input = new SignInput.Builder()
                    .setLines(List.of(Component.empty(), Component.text("^^^^^^^^").color(NamedTextColor.WHITE), Component.text("Enter the new amount").color(NamedTextColor.WHITE), Component.empty()).toArray(new Component[0]))
                    .setHandler(((player, lines) -> {
                        try {
                            this.amount = Integer.parseInt(lines[0]);
                        } catch (NumberFormatException exception) {
                            this.amount = 1;
                        }

                        Scheduler.get().run(task -> {
                            this.open();
                        });
                    }))
                    .build(this.sender);

            input.open();
        }));

        placeholderSelector.setItem(44, new GuiItem(ItemBuilder.create(Material.LIME_CONCRETE).setName("<#00FF00>Accept").get(), event -> {
            for (Map.Entry<String, String> entry : this.valuesMap.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    this.sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.PLACEHOLDERS_NOT_SET, Placeholder.unparsed("placeholders", entry.getKey())));
                    this.open();
                    return;
                }
            }

            this.voucher.getItemStack(this.amount, this.valuesMap).thenAccept(item -> {
                this.sender.getInventory().addItem(item);
            });
        }));

        placeholderSelector.open(this.sender);
    }
}
