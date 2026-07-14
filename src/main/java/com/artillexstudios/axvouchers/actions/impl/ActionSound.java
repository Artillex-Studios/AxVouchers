package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ActionSound extends Action {
    private static final Logger log = LoggerFactory.getLogger(ActionSound.class);

    public ActionSound() {
        super("sound");
    }

    @Override
    public void run(Player player, Voucher voucher, String arguments, Pair<String, String>[] placeholders) {
        String[] split = arguments.split(",");
        if (split.length != 3) {
            log.warn("You need to add {} to fix the sound!", split.length == 1 ? "the volume parameter, separating it from the sound name with a , and the pitch parameter, separating it from the volume with a ," : split.length == 2 ? "the pitch parameter, separating it from the volume with a ," : "");
            return;
        }

        Sound sound = Sound.valueOf(split[0].toUpperCase(Locale.ENGLISH));
        float volume = Float.parseFloat(split[1]);
        float pitch = Float.parseFloat(split[2]);

        Scheduler.get().run(task -> {
            player.playSound(player, sound, volume, pitch);
        });
    }
}