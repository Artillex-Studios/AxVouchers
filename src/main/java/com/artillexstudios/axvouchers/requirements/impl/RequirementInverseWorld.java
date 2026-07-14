package com.artillexstudios.axvouchers.requirements.impl;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.requirements.Requirement;
import com.artillexstudios.axvouchers.voucher.Voucher;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class RequirementInverseWorld extends Requirement {

    public RequirementInverseWorld() {
        super("!world");
    }

    @Override
    public boolean check(Player player, Voucher voucher, String arguments) {
        String worldName = player.getWorld().getName();
        if (arguments.contains("|")) {
            String[] or = OR.split(arguments);
            for (String s : or) {
                if (!worldName.equalsIgnoreCase(s)) {
                    return true;
                }
            }

            sendFail(player, Placeholder.parsed("world", String.join(Messages.OR, or)));
            return false;
        } else if (arguments.contains("&")) {
            String[] and = AND.split(arguments);
            for (String s : and) {
                if (worldName.equalsIgnoreCase(s)) {
                    sendFail(player, Placeholder.parsed("world", s));
                    return false;
                }
            }

            return true;
        }

        boolean equals = worldName.equalsIgnoreCase(arguments);
        if (equals) {
            sendFail(player, Placeholder.parsed("world", arguments));
        }
        return !equals;
    }

    @Override
    public void sendFail(Player player, TagResolver... resolvers) {
        if (Config.sendRequirementFail) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.FAIL_INVERSE_WORLD, resolvers));
        }
    }
}
