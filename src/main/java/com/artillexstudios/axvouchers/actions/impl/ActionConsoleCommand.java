package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Pair;
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
    public void run(Player player, Voucher voucher, String arguments, Pair<String, String>[] placeholders) {
        String formatted = arguments.replace("%player%", player.getName());
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(player, formatted);
        }

        if (placeholders != null) {
            for (Pair<String, String> placeholder : placeholders) {
                formatted = formatted.replace(placeholder.getKey(), placeholder.getValue());
            }
        }

        final String finalFormatted = formatted;
        Scheduler.get().run(task -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalFormatted);
        });
    }
}
