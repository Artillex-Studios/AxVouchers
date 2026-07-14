package com.artillexstudios.axvouchers.actions.impl;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Locale;

public class ActionFirework extends Action {
    public static final NamespacedKey FIREWORK_KEY = new NamespacedKey(AxVouchersPlugin.instance(), "ax_firework");

    public ActionFirework() {
        super("firework");
    }

    @Override
    public void run(Player player, Voucher voucher, String arguments, Pair<String, String>[] placeholders) {
        String[] split = arguments.split(",");
        Color fireWorkColor = Color.fromRGB(Integer.valueOf(split[0].substring(1, 3), 16), Integer.valueOf(split[0].substring(3, 5), 16), Integer.valueOf(split[0].substring(5, 7), 16));
        Scheduler.get().run(task -> {
            Location location = player.getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }

            Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.valueOf(split[1].toUpperCase(Locale.ENGLISH))).withColor(fireWorkColor).build());
            meta.setPower(0);
            fw.setFireworkMeta(meta);
            fw.getPersistentDataContainer().set(FIREWORK_KEY, PersistentDataType.BYTE, (byte) 0);
            fw.detonate();
        });
    }
}
