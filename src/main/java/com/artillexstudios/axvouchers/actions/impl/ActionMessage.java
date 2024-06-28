package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class ActionMessage extends Action {

    public ActionMessage() {
        super("message");
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

        player.sendMessage(StringUtils.formatToString(formatted));
    }
}
