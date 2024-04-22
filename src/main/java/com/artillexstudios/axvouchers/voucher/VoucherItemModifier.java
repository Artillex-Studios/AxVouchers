package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.PacketItemModifierListener;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoucherItemModifier implements PacketItemModifierListener {

    @Override
    public void modifyItemStack(Player player, WrappedItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getCompoundTag();
        if (!tag.contains("axvouchers-id")) return;

        Voucher voucher = Vouchers.parse(tag.getString("axvouchers-id"));
        if (voucher == null) {
            return;
        }

        List<Component> lore = stack.getLore();
        if (lore == Collections.EMPTY_LIST) {
            // We can just insert our lore
            if (voucher.getLore() != null && !voucher.getLore().isEmpty()) {
                stack.setLore(voucher.getLore());
            }

            if (voucher.getName() != null && voucher.getName() != Component.empty()) {
                stack.setName(voucher.getName());
            }
        } else {
            // The item already has some lore. I guess we want to put it at the end maybe?
            List<Component> newLore = new ArrayList<>(voucher.getLore());
            newLore.addAll(lore);

            if (voucher.getLore() != null && !voucher.getLore().isEmpty()) {
                stack.setLore(newLore);
            }

            if (voucher.getName() != null && voucher.getName() != Component.empty()) {
                stack.setName(voucher.getName());
            }
        }

        stack.setMaterial(voucher.getMaterial());
        if (voucher.getTexture() != null && voucher.getMaterial() == Material.PLAYER_HEAD) {
            stack.setTexture(voucher.getTexture());
        }
    }
}
