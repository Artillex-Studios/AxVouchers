package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ClassUtils;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class ActionPlayerCommand extends Action {

    public ActionPlayerCommand() {
        super("player");
    }

    @Override
    public void run(Player player, Voucher voucher, String arguments) {
        String formatted = arguments.replace("%player%", player.getName());
        if (ClassUtils.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        final String finalFormatted = formatted;
        Scheduler.get().run(task -> {
            player.performCommand(finalFormatted);
        });
    }
}
