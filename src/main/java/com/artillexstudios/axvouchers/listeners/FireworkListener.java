package com.artillexstudios.axvouchers.listeners;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvouchers.actions.impl.ActionFirework;
import com.artillexstudios.axvouchers.config.Config;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class FireworkListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Firework firework)) return;
        if (!firework.getPersistentDataContainer().has(ActionFirework.FIREWORK_KEY, PersistentDataType.BYTE)) return;

        if (Config.debug) {
            LogUtils.debug("Cancelling event, because firework has firework key!");
        }

        event.setCancelled(true);
        event.setDamage(0);
    }
}
