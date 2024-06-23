package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.PacketItemModifier;
import com.artillexstudios.axapi.items.PacketItemModifierListener;
import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.component.ItemLore;
import com.artillexstudios.axapi.items.component.ProfileProperties;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VoucherItemModifier implements PacketItemModifierListener {
    private static final UUID NIL_UUID = new UUID(0, 0);

    @Override
    public void modifyItemStack(Player player, WrappedItemStack stack, PacketItemModifier.Context context) {
        CompoundTag tag = stack.get(DataComponents.customData());
        if (!tag.contains("axvouchers-id")) {
            return;
        }

        Voucher voucher = Vouchers.parse(tag.getString("axvouchers-id"));
        if (voucher == null) {
            return;
        }

        if (context == PacketItemModifier.Context.EQUIPMENT) {
            stack.set(DataComponents.material(), voucher.getMaterial());

            if (voucher.getTexture() != null && voucher.getMaterial() == Material.PLAYER_HEAD) {
                ProfileProperties properties = new ProfileProperties(NIL_UUID, "axvouchers");
                properties.put("textures", new ProfileProperties.Property("textures", voucher.getTexture(), null));
                stack.set(DataComponents.profile(), properties);
            }
            return;
        }

        byte[] serialized = stack.serialize();
        tag.putByteArray("axvouchers-previous-state", serialized);

        List<Component> lore = stack.get(DataComponents.lore()).lines();

        if (lore.isEmpty()) {
            // We can just insert our lore
            if (!voucher.getLore().isEmpty()) {
                stack.set(DataComponents.lore(), new ItemLore(new ArrayList<>(voucher.getLore()), List.of()));
            }
        } else {
            // The item already has some lore. I guess we want to put it at the end maybe?
            List<Component> newLore = new ArrayList<>(voucher.getLore());
            newLore.addAll(lore);

            if (voucher.getLore() != null && !voucher.getLore().isEmpty()) {
                stack.set(DataComponents.lore(), new ItemLore(newLore));
            }
        }

        if (voucher.getName() != null && voucher.getName() != Component.empty()) {
            stack.set(DataComponents.customName(), voucher.getName());
        }

        stack.set(DataComponents.material(), voucher.getMaterial());
        if (voucher.getTexture() != null && voucher.getMaterial() == Material.PLAYER_HEAD) {
            ProfileProperties properties = new ProfileProperties(NIL_UUID, "axvouchers");
            properties.put("textures", new ProfileProperties.Property("textures", voucher.getTexture(), null));
            stack.set(DataComponents.profile(), properties);
        }
    }

    @Override
    public void restore(WrappedItemStack stack) {
        CompoundTag tag = stack.get(DataComponents.customData());
        if (!tag.contains("axvouchers-id")) {
            return;
        }

        Voucher voucher = Vouchers.parse(tag.getString("axvouchers-id"));
        if (voucher == null) {
            return;
        }

        byte[] previous = tag.getByteArray("axvouchers-previous-state");

        if (previous.length == 0) {
            return;
        }

        WrappedItemStack wrapped = WrappedItemStack.wrap(previous);
        ItemLore lore = wrapped.get(DataComponents.lore());
        stack.set(DataComponents.lore(), lore);
        stack.set(DataComponents.customName(), Component.empty());
        tag.remove("axvouchers-previous-state");
    }
}
