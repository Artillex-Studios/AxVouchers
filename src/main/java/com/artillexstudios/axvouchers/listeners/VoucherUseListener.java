package com.artillexstudios.axvouchers.listeners;

import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.voucher.RemovalReason;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class VoucherUseListener implements Listener {
    private static final UUID NIL_UUID = new UUID(0, 0);
    private static final Logger log = LoggerFactory.getLogger(VoucherUseListener.class);
    private static final HashMap<UUID, String> CONFIRM = new HashMap<>();
    private static final Cooldown<UUID> COOLDOWN = new Cooldown<>();

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Config.DEBUG) {
            log.info("Player interact event");
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() == null) {
            if (Config.DEBUG) {
                log.info("Event hand is null! How did this happen?");
            }
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) {
            if (Config.DEBUG) {
                log.info("Null or air item.");
            }
            return;
        }

        Voucher voucher = Vouchers.fromItem(item);

        if (voucher == null) {
            if (Config.DEBUG) {
                log.info("Voucher is null!");
            }
            return;
        }

        event.setCancelled(true);

        if (voucher.isConsume()) {
            if (Config.DEBUG) {
                log.info("Voucher is consume!");
            }
            event.setCancelled(false);
            return;
        }

        long remaining = COOLDOWN.getRemainingAsSeconds(event.getPlayer().getUniqueId());
        if (remaining > 0) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.COOLDOWN, Placeholder.parsed("time", String.valueOf(remaining))));
            return;
        }

        UUID uuid = Vouchers.getUUID(item);
        if (Config.DUPE_PROTECTION && !voucher.isStackable()) {
            if (uuid == null) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                DataHandler.DATA_THREAD.submit(() -> {
                    DataHandler.getInstance().insertLog(event.getPlayer(), voucher, NIL_UUID, RemovalReason.UNKNOWN_UUID.name());
                });
                return;
            }

            if (DataHandler.getInstance().isDuped(uuid)) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                DataHandler.DATA_THREAD.submit(() -> {
                    DataHandler.getInstance().insertLog(event.getPlayer(), voucher, uuid, RemovalReason.MORE_THAN_ISSUED.name());
                });
                return;
            }
        }

        String confirm = CONFIRM.remove(event.getPlayer().getUniqueId());

        if (voucher.canUse(event.getPlayer())) {
            if (Config.DEBUG) {
                log.info("Using voucher for player {}!", event.getPlayer().getName());
            }

            if ((voucher.isConfirm() && confirm == null) || (voucher.isConfirm() && !Objects.equals(confirm, voucher.getId()))) {
                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.CONFIRM, Placeholder.parsed("name", MiniMessage.miniMessage().serialize(voucher.getName()))));
                CONFIRM.put(event.getPlayer().getUniqueId(), voucher.getId());
                return;
            }

            if (Config.DUPE_PROTECTION && !voucher.isStackable() && uuid != null) {
                DataHandler.getInstance().incrementUsed(uuid);
            }

            DataHandler.DATA_THREAD.submit(() -> {
                DataHandler.getInstance().insertLog(event.getPlayer(), voucher, uuid == null ? NIL_UUID : uuid, RemovalReason.USE.name());
            });

            COOLDOWN.addCooldown(event.getPlayer().getUniqueId(), Duration.ofSeconds(voucher.getCooldown()).toMillis());
            item.setAmount(item.getAmount() - 1);
            voucher.doUse(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (item == null || item.getType().isAir()) return;
        Voucher voucher = Vouchers.fromItem(item);

        if (voucher == null) {
            return;
        }

        event.setCancelled(true);

        long remaining = COOLDOWN.getRemainingAsSeconds(event.getPlayer().getUniqueId());
        if (remaining > 0) {
            event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.COOLDOWN, Placeholder.parsed("time", String.valueOf(remaining))));
            return;
        }

        if (!voucher.isConsume()) {
            return;
        }

        UUID uuid = Vouchers.getUUID(item);
        if (Config.DUPE_PROTECTION && !voucher.isStackable()) {
            if (uuid == null) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                DataHandler.DATA_THREAD.submit(() -> {
                    DataHandler.getInstance().insertLog(event.getPlayer(), voucher, NIL_UUID, RemovalReason.UNKNOWN_UUID.name());
                });
                return;
            }

            if (DataHandler.getInstance().isDuped(uuid)) {
                item.setAmount(0);

                event.getPlayer().sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.DUPED_VOUCHER));
                DataHandler.DATA_THREAD.submit(() -> {
                    DataHandler.getInstance().insertLog(event.getPlayer(), voucher, uuid, RemovalReason.MORE_THAN_ISSUED.name());
                });
                return;
            }
        }

        String confirm = CONFIRM.remove(event.getPlayer().getUniqueId());

        if (voucher.canUse(event.getPlayer())) {
            if ((voucher.isConfirm() && confirm == null) || (voucher.isConfirm() && !Objects.equals(confirm, voucher.getId()))) {
                CONFIRM.put(event.getPlayer().getUniqueId(), voucher.getId());
                return;
            }

            if (Config.DUPE_PROTECTION && !voucher.isStackable() && uuid != null) {
                DataHandler.getInstance().incrementUsed(uuid);
            }

            DataHandler.DATA_THREAD.submit(() -> {
                DataHandler.getInstance().insertLog(event.getPlayer(), voucher, uuid == null ? NIL_UUID : uuid, RemovalReason.USE.name());
            });

            COOLDOWN.addCooldown(event.getPlayer().getUniqueId(), Duration.ofSeconds(voucher.getCooldown()).toMillis());
            item.setAmount(item.getAmount() - 1);
            voucher.doUse(event.getPlayer());
        }
    }

    public static void clear(Player player) {
        CONFIRM.remove(player.getUniqueId());
        COOLDOWN.remove(player.getUniqueId());
    }
}
