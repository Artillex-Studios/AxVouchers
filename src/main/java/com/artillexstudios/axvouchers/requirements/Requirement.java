package com.artillexstudios.axvouchers.requirements;

import com.artillexstudios.axvouchers.actions.Action;
import com.artillexstudios.axvouchers.voucher.Voucher;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public abstract class Requirement {
    public static final Pattern EQUALS = Pattern.compile("=");
    public static final Pattern OR = Pattern.compile("\\|");
    public static final Pattern AND = Pattern.compile("&");

    private final String id;

    public Requirement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract boolean check(Player player, Voucher voucher, String arguments);

    public abstract void sendFail(Player player, TagResolver... resolvers);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action action)) return false;

        return getId().equals(action.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}

