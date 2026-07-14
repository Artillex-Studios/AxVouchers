package com.artillexstudios.axvouchers.requirements.impl;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.requirements.Requirement;
import com.artillexstudios.axvouchers.voucher.Voucher;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class RequirementGamemode extends Requirement {

    public RequirementGamemode() {
        super("gamemode");
    }

    @Override
    public boolean check(Player player, Voucher voucher, String arguments) {
        if (arguments.contains("|")) {
            String[] or = OR.split(arguments);
            for (String s : or) {
                if (player.getGameMode().name().equalsIgnoreCase(s)) {
                    return true;
                }
            }

            sendFail(player, Placeholder.parsed("gamemode", String.join(Messages.OR, or)));
            return false;
        } else if (arguments.contains("&")) {
            String[] and = AND.split(arguments);
            for (String s : and) {
                if (!player.getGameMode().name().equalsIgnoreCase(s)) {
                    sendFail(player, Placeholder.parsed("gamemode", s));
                    return false;
                }
            }

            return true;
        }

        boolean equals = player.getGameMode().name().equalsIgnoreCase(arguments);
        if (!equals) {
            sendFail(player, Placeholder.parsed("gamemode", arguments));
        }

        return equals;
    }

    @Override
    public void sendFail(Player player, TagResolver... resolvers) {
        if (Config.sendRequirementFail) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.FAIL_GAMEMODE, resolvers));
        }
    }
}
