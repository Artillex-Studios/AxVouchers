package com.artillexstudios.axvouchers.listeners;

import com.artillexstudios.axvouchers.actions.impl.ActionFirework;
import com.artillexstudios.axvouchers.config.Config;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FireworkListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(FireworkListener.class);

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Firework firework)) return;
        if (!firework.getPersistentDataContainer().has(ActionFirework.FIREWORK_KEY, PersistentDataType.BYTE)) return;

        if (Config.DEBUG) {
            log.info("Cancelling event, because firework has firework key!");
        }

        event.setCancelled(true);
        event.setDamage(0);
    }
}
