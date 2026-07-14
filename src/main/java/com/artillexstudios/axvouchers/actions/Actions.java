package com.artillexstudios.axvouchers.actions;

import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axvouchers.actions.impl.ActionConsoleCommand;
import com.artillexstudios.axvouchers.actions.impl.ActionFirework;
import com.artillexstudios.axvouchers.actions.impl.ActionItem;
import com.artillexstudios.axvouchers.actions.impl.ActionMessage;
import com.artillexstudios.axvouchers.actions.impl.ActionPlayerCommand;
import com.artillexstudios.axvouchers.actions.impl.ActionSound;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.voucher.Voucher;
import com.artillexstudios.axvouchers.voucher.Vouchers;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Actions {
    private static final HashMap<String, Action> ACTIONS = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Actions.class);
    private static final Action CONSOLE_COMMAND = register(new ActionConsoleCommand());
    private static final Action FIREWORK = register(new ActionFirework());
    private static final Action ITEM = register(new ActionItem());
    private static final Action PLAYER_COMMAND = register(new ActionPlayerCommand());
    private static final Action SOUND = register(new ActionSound());
    private static final Action MESSAGE = register(new ActionMessage());

    public static Action register(Action action) {
        if (Config.debug) {
            log.info("Registering action with id: {}", action.getId());
        }

        ACTIONS.put(action.getId(), action);
        return action;
    }

    public static void run(Player player, Voucher voucher, List<String> actions, CompoundTag tag) {
        for (String rawAction : actions) {
            if (rawAction == null || rawAction.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(rawAction, "[", "]").toLowerCase(Locale.ENGLISH);
            String arguments = StringUtils.substringAfter(rawAction, "] ");

            if (Config.debug) {
                log.info("Looking for action with id {}.", id);
            }

            Action action = ACTIONS.get(id);
            if (action == null) continue;

            if (Config.debug) {
                log.info("Running action {} with arguments: {}", id, arguments);
            }

            action.run(player, voucher, arguments, voucher.placeholderCache().computeIfAbsent(Vouchers.placeholderString(tag), Vouchers::placeholders));
        }
    }
}
