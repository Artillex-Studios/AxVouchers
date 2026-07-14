package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponents;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvouchers.AxVouchersPlugin;
import com.artillexstudios.axvouchers.actions.Actions;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.requirements.Requirements;
import net.kyori.adventure.text.Component;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Voucher {
    private final String id;
    private final Section section;
    private final HashMap<String, ItemStack> items = new HashMap<>();
    private final List<String> requirements = new ArrayList<>();
    private final List<String> actions = new ArrayList<>();
    private final List<String> placeholders = new ArrayList<>();
    private final ConcurrentHashMap<String, Component> nameCache = new ConcurrentHashMap<>(1);
    private final ConcurrentHashMap<String, List<Component>> loreCache = new ConcurrentHashMap<>(1);
    private final ConcurrentHashMap<String, Pair<String, String>[]> placeholderCache = new ConcurrentHashMap<>(1);
    private EnumeratedDistribution<List<String>> randomActions = null;
    private ItemStack itemStack = new ItemStack(Material.STONE);
    private boolean stackable = false;
    private boolean consume = false;
    private boolean confirm = false;
    private int cooldown = -1;
    private String name = "";
    private List<String> lore = List.of();
    private String material;
    private String texture = null;

    public Voucher(String id, Section section) {
        this.id = id;
        this.section = section;

        reload();
        Vouchers.placeholders().addAll(placeholders);
        Vouchers.register(this);
    }

    public void reload() {
        loreCache.clear();
        nameCache.clear();
        placeholderCache.clear();

        ItemBuilder builder = ItemBuilder.create(section.getSection("item"));
        if (Config.usePacketItems) {
            builder.setLore(List.of())
                    .setName("");

            itemStack = builder.get();
            itemStack.setType(Material.STICK);
        } else {
            itemStack = builder.get();
        }

        String name = section.getString("item.name");
        this.name = name == null ? this.name : name;
        this.lore = section.getStringList("item.lore", List.of());
        String type = section.getString("item.type");
        if (type == null) {
            type = section.getString("item.material");
        }
        this.material = type;
        texture = section.getString("item.texture");

        items.clear();
        section.getOptionalMapList("items").ifPresent(list -> {
            for (Map<?, ?> map1 : list) {
                items.put(map1.get("id").toString(), ItemBuilder.create((Map<Object, Object>) map1).get());
            }
        });

        stackable = section.getBoolean("stackable", stackable);
        consume = section.getBoolean("consume", consume);
        if (consume && Config.usePacketItems) {
            itemStack.setType(Material.GOLDEN_APPLE);
        }

        actions.clear();
        section.getOptionalStringList("actions").ifPresent(this.actions::addAll);

        requirements.clear();
        section.getOptionalStringList("requirements").ifPresent(this.requirements::addAll);

        randomActions = null;
        section.getOptionalMapList("random-actions").ifPresent(list -> {
            List<org.apache.commons.math3.util.Pair<List<String>, Double>> randomActions = new ArrayList<>();

            for (Map<?, ?> map : list) {
                Map<Object, Object> castMap = (Map<Object, Object>) map;
                if (!castMap.containsKey("chance")) {
                    LogUtils.error("Found invalid random actions in voucher {}! Chance is not present!", getId());
                    continue;
                }

                if (!castMap.containsKey("actions")) {
                    LogUtils.error("Found invalid random actions in voucher {}! Actions are not present!", getId());
                    continue;
                }

                Double chance = ((Number) castMap.get("chance")).doubleValue();
                List<String> actions = (List<String>) castMap.get("actions");
                randomActions.add(org.apache.commons.math3.util.Pair.create(actions, chance));
            }

            this.randomActions = new EnumeratedDistribution<>(randomActions);
        });

        confirm = section.getBoolean("confirm", confirm);
        cooldown = section.getInt("cooldown", cooldown);

        placeholders.clear();
        placeholders.addAll(section.getStringList("placeholders", List.of()));
    }

    public boolean canUse(Player player) {
        if (requirements.isEmpty()) {
            if (Config.debug) {
                LogUtils.info("Requirements are empty!");
            }
            return true;
        }

        return Requirements.check(player, this, requirements);
    }

    public void doUse(Player player, CompoundTag tag) {
        if (!actions.isEmpty()) {
            Actions.run(player, this, actions, tag);
        }

        if (randomActions != null) {
            List<String> random = randomActions.sample();
            Actions.run(player, this, random, tag);
        }
    }

    public ItemStack getForGUI() {
        return ItemBuilder.create(section.getSection("item")).get();
    }

    public CompletableFuture<ItemStack> getItemStack(int amount, LinkedHashMap<String, String> placeholders) {
        ItemStack stack = this.getItemStack0(amount, placeholders);
        if (Config.dupeProtection && !this.stackable) {
            UUID uuid = UUID.randomUUID();
            return AxVouchersPlugin.instance().handler().insertAntidupe(uuid, amount).thenApply(result -> {
                return WrappedItemStack.edit(stack, wrapped -> {
                    CompoundTag tag = wrapped.get(DataComponents.customData());
                    tag.putUUID("axvouchers-uuid", uuid);
                    wrapped.set(DataComponents.customData(), tag);
                    return wrapped;
                }).toBukkit();
            });
        }

        return CompletableFuture.completedFuture(stack);
    }

    private ItemStack getItemStack0(int amount, LinkedHashMap<String, String> placeholders) {
        ItemStack stack = this.itemStack.clone();
        WrappedItemStack.edit(stack, (item) -> {
            CompoundTag tag = item.get(DataComponents.customData());
            tag.putString("axvouchers-id", getId());

            if (placeholders != null && !placeholders.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                placeholders.forEach((key, value) -> {
                    builder.append(key).append('-').append(value).append(';');
                });

                builder.deleteCharAt(builder.length() - 1);
                tag.putString("axvouchers-placeholders", builder.toString());
            }
            item.set(DataComponents.customData(), tag);
            return null;
        });
        stack.setAmount(amount);
        return stack;
    }

    public ItemStack getItem(String id) {
        return items.get(id);
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getItemIds() {
        return String.join(", ", items.keySet());
    }

    public String getMaterial() {
        return material;
    }

    public boolean isConsume() {
        return consume;
    }

    public boolean isStackable() {
        return stackable;
    }

    public String getId() {
        return id.toLowerCase(Locale.ENGLISH);
    }

    public boolean isConfirm() {
        return confirm;
    }

    public int getCooldown() {
        return cooldown;
    }

    public String getTexture() {
        return texture;
    }

    public List<String> placeholders() {
        return placeholders;
    }

    public ConcurrentHashMap<String, Component> nameCache() {
        return nameCache;
    }

    public ConcurrentHashMap<String, List<Component>> loreCache() {
        return loreCache;
    }

    public ConcurrentHashMap<String, Pair<String, String>[]> placeholderCache() {
        return placeholderCache;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Voucher voucher)) return false;

        return Objects.equals(getId(), voucher.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
