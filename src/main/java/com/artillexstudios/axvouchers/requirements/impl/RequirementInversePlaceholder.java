package com.artillexstudios.axvouchers.requirements.impl;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.config.Messages;
import com.artillexstudios.axvouchers.requirements.Requirement;
import com.artillexstudios.axvouchers.voucher.Voucher;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("LoggingSimilarMessage")
public class RequirementInversePlaceholder extends Requirement {
    private static final Logger log = LoggerFactory.getLogger(RequirementPlaceholder.class);

    public RequirementInversePlaceholder() {
        super("!placeholder");
    }

    @Override
    public boolean check(Player player, Voucher voucher, String arguments) {
        if (!ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            log.error("PlaceholderAPI is not present on this server, yet you tried to use placeholder requirements in voucher {}!", voucher.getId());
            return false;
        }

        // We don't want faulty expressions
        if (!arguments.contains("=")) {
            log.warn("Found an incomplete placeholder expression in voucher {} with arguments: {}", voucher.getId(), arguments);
            return false;
        }

        if (arguments.contains("|")) {
            String[] split = OR.split(arguments);
            for (String s : split) {
                // It could be, that there are = signs, but not in every expression
                if (!s.contains("=")) {
                    log.warn("Found an incomplete placeholder expression in voucher {} with arguments: {}", voucher.getId(), arguments);
                    return false;
                }

                String[] sides = EQUALS.split(s);
                String parsed = PlaceholderAPI.setPlaceholders(player, sides[0]);
                String value = sides[1];
                if (!parsed.equalsIgnoreCase(value)) {
                    return true;
                }
            }

            sendFail(player, Placeholder.parsed("value", String.join(Messages.OR, split)));
            return false;
        } else if (arguments.contains("&")) {
            String[] split = AND.split(arguments);
            for (String s : split) {
                if (!s.contains("=")) {
                    log.warn("Found an incomplete placeholder expression in voucher {} with arguments: {}", voucher.getId(), arguments);
                    return false;
                }

                String[] sides = EQUALS.split(s);
                String parsed = PlaceholderAPI.setPlaceholders(player, sides[0]);
                String value = sides[1];
                if (parsed.equalsIgnoreCase(value)) {
                    sendFail(player, Placeholder.parsed("value", value));
                    return false;
                }
            }

            return true;
        }

        String[] sides = EQUALS.split(arguments);
        String parsed = PlaceholderAPI.setPlaceholders(player, sides[0]);
        boolean equals = parsed.equalsIgnoreCase(sides[1]);
        if (equals) {
            sendFail(player, Placeholder.parsed("value", sides[1]));
        }

        return !equals;
    }

    @Override
    public void sendFail(Player player, TagResolver... resolvers) {
        if (Config.sendRequirementFail) {
            player.sendMessage(StringUtils.formatToString(Messages.PREFIX + Messages.FAIL_INVERSE_PLACEHOLDER, resolvers));
        }
    }
}
