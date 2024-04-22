package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionItem extends Action {
    private static final Logger log = LoggerFactory.getLogger(ActionItem.class);

    public ActionItem() {
        super("item");
    }

    @Override
    public void run(Player player, Voucher voucher, String arguments) {
        ItemStack item = voucher.getItem(arguments);
        if (item == null) {
            log.warn("There is no item with id {} in {} voucher's config! Registered item ids: {}.", arguments, voucher, voucher.getItemIds());
            return;
        }

        Scheduler.get().run(task -> {
            player.getInventory().addItem(item.clone());
        });
    }
}

