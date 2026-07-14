package com.artillexstudios.axvouchers.requirements.impl;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.requirements.Requirement;
import com.artillexstudios.axvouchers.voucher.Voucher;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

public class RequirementInversePermission extends Requirement {

    public RequirementInversePermission() {
        super("!permission");
    }

    @Override
    public boolean check(Player player, Voucher voucher, String arguments) {
        if (arguments.contains("|")) {
            String[] or = OR.split(arguments);
            for (String s : or) {
                if (!player.hasPermission(s)) {
                    return true;
                }
            }

            sendFail(player, Placeholder.parsed("permission", String.join(Messages.OR, or)));
            return false;
        } else if (arguments.contains("&")) {
            String[] and = AND.split(arguments);
            for (String s : and) {
                if (player.hasPermission(s)) {
                    sendFail(player, Placeholder.parsed("permission", s));
                    return false;
                }
            }

            return true;
        }

        boolean hasPermission = player.hasPermission(arguments);
        if (hasPermission) {
            sendFail(player, Placeholder.parsed("permission", arguments));
        }

        return !hasPermission;
    }

    @Override
    public void sendFail(Player player, TagResolver... resolvers) {
        if (Config.sendRequirementFail) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.FAIL_INVERSE_PERMISSION, resolvers));
        }
    }
}
