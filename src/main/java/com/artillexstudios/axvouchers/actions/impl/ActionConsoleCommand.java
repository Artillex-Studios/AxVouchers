package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.ClassUtils;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console");
    }

    @Override
    public void run(Player player, Voucher voucher, String arguments) {
        String formatted = arguments.replace("%player%", player.getName());
        if (ClassUtils.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        final String finalFormatted = formatted;
        Scheduler.get().run(task -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalFormatted);
        });
    }
}
