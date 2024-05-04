package com.artillexstudios.axvouchers.voucher;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.items.component.DataComponent;
import com.artillexstudios.axapi.items.nbt.CompoundTag;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvouchers.actions.Actions;
import com.artillexstudios.axvouchers.config.Config;
import com.artillexstudios.axvouchers.database.DataHandler;
import com.artillexstudios.axvouchers.requirements.Requirements;
import net.kyori.adventure.text.Component;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Voucher {
    private static final Logger log = LoggerFactory.getLogger(Voucher.class);
    private final String id;
    private final Section section;
    private final HashMap<String, ItemStack> items = new HashMap<>();
    private final List<String> requirements = new ArrayList<>();
    private final List<String> actions = new ArrayList<>();
    private EnumeratedDistribution<List<String>> randomActions = null;
    private ItemStack itemStack = new ItemStack(Material.STONE);
    private boolean stackable = false;
    private boolean consume = false;
    private boolean confirm = false;
    private int cooldown = -1;
    private Component name = Component.empty();
    private List<Component> lore = List.of();
    private Material material = Material.STONE;
    private String texture = null;

    public Voucher(String id, Section section) {
        this.id = id;
        this.section = section;

        reload();
        Vouchers.register(this);
    }

    public void reload() {
        itemStack = new ItemBuilder(section.getSection("item")).setLore(List.of()).setName("").get();
        String name = section.getString("item.name");
        this.name = name == null ? this.name : StringUtils.format(name);
        List<String> lore = section.getStringList("item.lore");
        this.lore = lore == null ? this.lore : StringUtils.formatList(lore);
        String type = section.getString("item.type");
        if (type == null) {
            type = section.getString("item.material");
        }

        Material material = Material.matchMaterial(type.toUpperCase(Locale.ENGLISH));
        this.material = material == null ? Material.STONE : material;
        texture = section.getString("item.texture");

        items.clear();
        section.getOptionalMapList("items").ifPresent(list -> {
            for (Map<?, ?> map1 : list) {
                items.put(map1.get("id").toString(), new ItemBuilder((Map<Object, Object>) map1).get());
            }
        });

        stackable = section.getBoolean("stackable", stackable);
        consume = section.getBoolean("consume", consume);

        actions.clear();
        section.getOptionalStringList("actions").ifPresent(this.actions::addAll);

        requirements.clear();
        section.getOptionalStringList("requirements").ifPresent(this.requirements::addAll);

        randomActions = null;
        section.getOptionalMapList("random-actions").ifPresent(list -> {
            List<Pair<List<String>, Double>> randomActions = new ArrayList<>();

            for (Map<?, ?> map : list) {
                Map<Object, Object> castMap = (Map<Object, Object>) map;
                if (!castMap.containsKey("chance")) {
                    log.error("Found invalid random actions in voucher {}! Chance is not present!", getId());
                    continue;
                }

                if (!castMap.containsKey("actions")) {
                    log.error("Found invalid random actions in voucher {}! Actions are not present!", getId());
                    continue;
                }

                Double chance = ((Number) castMap.get("chance")).doubleValue();
                List<String> actions = (List<String>) castMap.get("actions");
                randomActions.add(Pair.create(actions, chance));
            }

            this.randomActions = new EnumeratedDistribution<>(randomActions);
        });

        confirm = section.getBoolean("confirm", confirm);
        cooldown = section.getInt("cooldown", cooldown);
    }

    public boolean canUse(Player player) {
        if (requirements.isEmpty()) {
            if (Config.DEBUG) {
                log.info("Requirements are empty!");
            }
            return true;
        }

        return Requirements.check(player, this, requirements);
    }

    public void doUse(Player player) {
        if (!actions.isEmpty()) {
            Actions.run(player, this, actions);
        }

        if (randomActions != null) {
            List<String> random = randomActions.sample();
            Actions.run(player, this, random);
        }
    }

    public ItemStack getForGUI() {
        return new ItemBuilder(section.getSection("item")).get();
    }

    public ItemStack getItemStack(int amount) {
        ItemStack stack = itemStack.clone();
        WrappedItemStack.edit(stack, (item) -> {
            CompoundTag tag = item.get(DataComponent.CUSTOM_DATA);
            tag.putString("axvouchers-id", getId());

            if (Config.DUPE_PROTECTION && !stackable) {
                UUID uuid = UUID.randomUUID();
                tag.putUUID("axvouchers-uuid", uuid);

                DataHandler.getInstance().insertAntidupe(uuid, amount);
            }
            item.set(DataComponent.CUSTOM_DATA, tag);
            return null;
        });
        stack.setAmount(amount);
        return stack;
    }

    public ItemStack getItem(String id) {
        return items.get(id);
    }

    public Component getName() {
        return name;
    }

    public List<Component> getLore() {
        return lore;
    }

    public String getItemIds() {
        return String.join(", ", items.keySet());
    }

    public Material getMaterial() {
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
