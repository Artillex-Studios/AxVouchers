package com.artillexstudios.axvouchers.requirements;

import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.requirements.impl.RequirementGamemode;
import com.artillexstudios.axvouchers.requirements.impl.RequirementInverseGamemode;
import com.artillexstudios.axvouchers.requirements.impl.RequirementInversePermission;
import com.artillexstudios.axvouchers.requirements.impl.RequirementInversePlaceholder;
import com.artillexstudios.axvouchers.requirements.impl.RequirementInverseWorld;
import com.artillexstudios.axvouchers.requirements.impl.RequirementPermission;
import com.artillexstudios.axvouchers.requirements.impl.RequirementPlaceholder;
import com.artillexstudios.axvouchers.requirements.impl.RequirementWorld;
import com.artillexstudios.axvouchers.voucher.Voucher;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Requirements {
    private static final HashMap<String, Requirement> REQUIREMENTS = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(Requirements.class);
    private static final Requirement INVERSE_PERMISSION = register(new RequirementInversePermission());
    private static final Requirement INVERSE_PLACEHOLDER = register(new RequirementInversePlaceholder());
    private static final Requirement INVERSE_WORLD = register(new RequirementInverseWorld());
    private static final Requirement INVERSE_GAMEMODE = register(new RequirementInverseGamemode());
    private static final Requirement PERMISSION = register(new RequirementPermission());
    private static final Requirement PLACEHOLDER = register(new RequirementPlaceholder());
    private static final Requirement WORLD = register(new RequirementWorld());
    private static final Requirement GAMEMODE = register(new RequirementGamemode());

    public static Requirement register(Requirement requirement) {
        if (Config.debug) {
            log.info("Registering requirement with id: {}", requirement.getId());
        }

        REQUIREMENTS.put(requirement.getId(), requirement);
        return requirement;
    }

    public static boolean check(Player player, Voucher voucher, List<String> requirements) {
        for (String requirement : requirements) {
            if (requirement == null || requirement.isBlank()) {
                continue;
            }

            String id = StringUtils.substringBetween(requirement, "[", "]").toLowerCase(Locale.ENGLISH);
            String arguments = StringUtils.substringAfter(requirement, "] ");

            if (Config.debug) {
                log.info("Looking for requirement with id {}.", id);
            }

            Requirement req = REQUIREMENTS.get(id);
            if (req == null) continue;

            if (Config.debug) {
                log.info("Running requirement {} with arguments: {}", id, arguments);
            }

            if (!req.check(player, voucher, arguments)) {
                return false;
            }
        }

        return true;
    }
}
