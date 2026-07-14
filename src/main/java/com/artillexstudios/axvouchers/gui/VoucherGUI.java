package com.artillexstudios.axvouchers.gui;

import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public enum VoucherGUI {
    INSTANCE;

    public void open(Player sender) {
        PaginatedGui gui = Gui.paginated()
                .disableAllInteractions()
                .title(MiniMessage.miniMessage().deserialize("<#DC143C>Vouchers"))
                .pageSize(21)
                .rows(5)
                .create();

        new GuiFiller(gui).fillBorder(new GuiItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        gui.setItem(38, new GuiItem(ItemBuilder.create(Material.ARROW).setName("<#DC143C>Previous page").get(), event -> {
            gui.previous();
        }));

        gui.setItem(42, new GuiItem(ItemBuilder.create(Material.ARROW).setName("<#DC143C>Next page").get(), event -> {
            gui.next();
        }));

        for (Voucher voucher : Vouchers.getVouchers()) {
            ItemStack itemStack = voucher.getForGUI();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                continue;
            }

            List<String> itemLore = meta.getLore();
            ItemBuilder builder = ItemBuilder.create(itemStack);
            List<String> lore = new ArrayList<>(itemLore == null ? List.of() : itemLore);
            lore.add("");
            lore.add("<#DC143C>Left click to get!");
            builder.setLore(lore);

            gui.addItem(new GuiItem(builder.get(), event -> {
                if (voucher.placeholders().isEmpty()) {
                    voucher.getItemStack(1, null).thenAccept(item -> {
                        ContainerUtils.INSTANCE.addOrDrop(sender.getInventory(), List.of(item), sender.getLocation());
                    });
                } else {
                    new VoucherPlaceholderGUI(sender, voucher).open();
                }
            }));
        }

        gui.open(sender);
    }
}
