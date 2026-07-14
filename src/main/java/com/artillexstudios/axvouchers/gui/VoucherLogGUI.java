package com.artillexstudios.axvouchers.gui;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.type.ItemLore;
import com.artillexstudios.axapi.items.component.type.ProfileProperties;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.database.log.VoucherLogEntry;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class VoucherLogGUI {
    private final UUID NIL_UUID = new UUID(0, 0);
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss");
    private final DataHandler handler;

    public VoucherLogGUI(DataHandler handler) {
        this.handler = handler;
    }

    public void open(Player sender, OfflinePlayer user) {
        PaginatedGui gui = Gui.paginated()
                .disableAllInteractions()
                .title(MiniMessage.miniMessage().deserialize("<#DC143C>Log viewer"))
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

        this.handler.logs(user.getName()).thenAccept(logs -> {
            if (logs == null) {
                sender.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.NO_LOGS, Placeholder.parsed("name", user.getName())));
                return;
            }

            for (VoucherLogEntry entry : logs) {
                Voucher voucher = Vouchers.parse(entry.type());
                Pair<String, String>[] placeholders = voucher == null ? Vouchers.placeholders(entry.placeholders()) : voucher.placeholderCache().computeIfAbsent(entry.placeholders(), Vouchers::placeholders);
                String type = voucher == null ? "STONE" : voucher.getMaterial();
                for (Pair<String, String> placeholder : placeholders) {
                    type = type.replace(placeholder.first(), placeholder.getValue());
                }

                Material matched = Material.matchMaterial(type.toUpperCase(Locale.ENGLISH));
                WrappedItemStack wrapped = WrappedItemStack.wrap(new ItemStack(matched == null ? Material.STONE : matched));
                wrapped.set(DataComponents.customName(), voucher == null ? StringUtils.format(entry.type()) : voucher.nameCache().computeIfAbsent(entry.placeholders(), string -> {
                    TagResolver[] tagResolvers = new TagResolver[0];
                    if (!voucher.placeholders().isEmpty()) {
                        tagResolvers = Vouchers.tagResolvers(string);
                    }

                    return StringUtils.format(voucher.getName(), tagResolvers);
                }));

                if (voucher != null && voucher.getTexture() != null && matched == Material.PLAYER_HEAD) {
                    ProfileProperties properties = new ProfileProperties(NIL_UUID, "axvouchers");
                    properties.put("textures", new ProfileProperties.Property("textures", voucher.getTexture(), null));
                    wrapped.set(DataComponents.profile(), properties);
                }

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                if (voucher == null) {
                    lore.add(StringUtils.format("<white>Placeholders: <gray><placeholders>", Placeholder.unparsed("placeholders", entry.placeholders())));
                    lore.add(Component.empty());
                }

                lore.add(StringUtils.format("<white>Time: <#DC143C><time>", Placeholder.unparsed("time", format.format(entry.time()))));
                lore.add(StringUtils.format("<white>UUID: <#DC143C><uuid>", Placeholder.unparsed("uuid", entry.uuid() instanceof  UUID uuid ? uuid.toString() : (String) entry.uuid())));
                lore.add(StringUtils.format("<white>Remove reason: <#DC143C><reason>", Placeholder.unparsed("reason", entry.duped())));
                lore.add(StringUtils.format("<white>ID: <#DC143C><id>", Placeholder.unparsed("id", String.valueOf(entry.id()))));

                wrapped.set(DataComponents.lore(), new ItemLore(lore, List.of()));
                gui.addItem(new GuiItem(wrapped.toBukkit()));
            }

            Scheduler.get().runAt(sender.getLocation(), () -> {
                gui.open(sender);
            });
        });
    }
}
