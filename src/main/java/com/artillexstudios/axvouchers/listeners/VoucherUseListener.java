package com.artillexstudios.axvouchers.listeners;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.voucher.RemovalReason;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class VoucherUseListener implements Listener {
    private static final UUID NIL_UUID = new UUID(0, 0);
    private static final HashMap<UUID, String> CONFIRM = new HashMap<>();
    private static final Cooldown<String> COOLDOWN = Cooldown.create();
    private static final Cooldown<UUID> SHORT_COOLDOWN = Cooldown.create();
    private final DataHandler handler;

    public static void clear(Player player) {
        CONFIRM.remove(player.getUniqueId());
    }

    public VoucherUseListener(DataHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Config.debug) {
            LogUtils.debug("Player interact event");
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() == null) {
            if (Config.debug) {
                LogUtils.debug("Event hand is null! How did this happen?");
            }
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) {
            if (Config.debug) {
                LogUtils.debug("Null or air item.");
            }
            return;
        }

        WrappedItemStack wrappedItemStack = WrappedItemStack.wrap(item);
        CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
        Boolean using = tag.getBoolean("axvouchers-using");
        if (using != null && using) {
            return;
        }

        Voucher voucher = Vouchers.fromItem(tag);

        if (voucher == null) {
            if (Config.debug) {
                LogUtils.debug("Voucher is null!");
            }
            return;
        }

        event.setCancelled(true);

        if (voucher.isConsume()) {
            if (Config.debug) {
                LogUtils.debug("Voucher is consume!");
            }
            event.setUseItemInHand(Event.Result.ALLOW);
            return;
        }

        long shortRemaining = SHORT_COOLDOWN.getRemaining(event.getPlayer().getUniqueId());
        if (shortRemaining > 0) {
            return;
        }

        long remaining = COOLDOWN.getRemaining(event.getPlayer().getUniqueId() + "-" + voucher.getId());
        if (remaining > 0) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.COOLDOWN, Placeholder.parsed("time", String.valueOf(Math.ceil((float) remaining / 1000)))));
            return;
        }

        String placeholderString = Vouchers.placeholderString(tag);
        UUID uuid = Vouchers.getUUID(tag);
        ItemStack clone = item.clone();
        tag.putBoolean("axvouchers-using", true);
        wrappedItemStack.set(DataComponents.customData(), tag);

        item.setAmount(0);
        if (Config.dupeProtection && !voucher.isStackable()) {
            if (uuid == null) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                this.handler.insertLog(event.getPlayer(), voucher, NIL_UUID, RemovalReason.UNKNOWN_UUID.name(), placeholderString);
                return;
            }

            this.handler.incrementUsed(uuid).thenAccept(success -> {
                if (!success) {
                    // the item was duped
                    this.handler.insertLog(event.getPlayer(), voucher, uuid, RemovalReason.MORE_THAN_ISSUED.name(), placeholderString);
                    event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                    return;
                }

                Scheduler.get().runAt(event.getPlayer().getLocation(), () -> {
                    this.use(event.getPlayer(), voucher, placeholderString, tag, item, clone, uuid, wrappedItemStack);
                });
            });
            return;
        }

        this.use(event.getPlayer(), voucher, placeholderString, tag, item, clone, uuid, wrappedItemStack);
    }

    private void use(Player player, Voucher voucher, String placeholderString, CompoundTag tag, ItemStack item, ItemStack clone, UUID uuid, WrappedItemStack wrappedItemStack) {
        String confirm = CONFIRM.remove(player.getUniqueId());

        if (voucher.canUse(player)) {
            if (Config.debug) {
                LogUtils.debug("Using voucher for player {}!", player.getName());
            }

            TagResolver[] resolvers = Vouchers.tagResolvers(placeholderString);

            if ((voucher.isConfirm() && confirm == null) || (voucher.isConfirm() && !Objects.equals(confirm, voucher.getId()))) {
                player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.CONFIRM, Placeholder.parsed("name", MiniMessage.miniMessage().serialize(StringUtils.format(voucher.getName(), resolvers)))));
                CONFIRM.put(player.getUniqueId(), voucher.getId());
                item.setAmount(clone.getAmount());
                return;
            }

            this.handler.insertLog(player, voucher, uuid == null ? NIL_UUID : uuid, RemovalReason.USE.name(), placeholderString);
            SHORT_COOLDOWN.addCooldown(player.getUniqueId(), 250);
            COOLDOWN.addCooldown(player.getUniqueId() + "-" + voucher.getId(), Duration.ofSeconds(voucher.getCooldown()).toMillis());
            item.setAmount(clone.getAmount() - 1);
            voucher.doUse(player, tag);
            tag.remove("axvouchers-using");
            wrappedItemStack.set(DataComponents.customData(), tag);
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) return;
        WrappedItemStack wrappedItemStack = WrappedItemStack.wrap(item);
        CompoundTag tag = wrappedItemStack.get(DataComponents.customData());
        Boolean using = tag.getBoolean("axvouchers-using");
        if (using != null && using) {
            return;
        }

        Voucher voucher = Vouchers.fromItem(tag);

        if (voucher == null) {
            return;
        }

        if (!voucher.isConsume()) {
            return;
        }

        event.setCancelled(true);

        long shortRemaining = SHORT_COOLDOWN.getRemaining(event.getPlayer().getUniqueId());
        if (shortRemaining > 0) {
            return;
        }

        long remaining = COOLDOWN.getRemaining(event.getPlayer().getUniqueId() + "-" + voucher.getId());
        if (remaining > 0) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.COOLDOWN, Placeholder.parsed("time", String.valueOf(Math.ceil((float) remaining / 1000)))));
            return;
        }

        String placeholderString = Vouchers.placeholderString(tag);
        UUID uuid = Vouchers.getUUID(tag);
        ItemStack clone = item.clone();
        tag.putBoolean("axvouchers-using", true);
        wrappedItemStack.set(DataComponents.customData(), tag);

        item.setAmount(0);
        if (Config.dupeProtection && !voucher.isStackable()) {
            if (uuid == null) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                this.handler.insertLog(event.getPlayer(), voucher, NIL_UUID, RemovalReason.UNKNOWN_UUID.name(), placeholderString);
                return;
            }

            this.handler.incrementUsed(uuid).thenAccept(success -> {
                if (!success) {
                    // the item was duped
                    this.handler.insertLog(event.getPlayer(), voucher, uuid, RemovalReason.MORE_THAN_ISSUED.name(), placeholderString);
                    event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                    return;
                }

                Scheduler.get().runAt(event.getPlayer().getLocation(), () -> {
                    this.use(event.getPlayer(), voucher, placeholderString, tag, item, clone, uuid, wrappedItemStack);
                });
            });
            return;
        }

        this.use(event.getPlayer(), voucher, placeholderString, tag, item, clone, uuid, wrappedItemStack);
    }
}
