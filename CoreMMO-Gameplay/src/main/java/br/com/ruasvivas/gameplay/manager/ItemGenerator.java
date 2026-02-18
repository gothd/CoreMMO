package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.gameplay.util.BukkitConstants;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ItemGenerator {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public ItemGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private enum ItemTier {
        // T0: Comum (Início)
        COMMON(0, "Comum", 1, 0.0, 0.0, 10.0, NamedTextColor.GRAY),

        // T1-T4: Incomum (Níveis 5-20)
        UNCOMMON_1(1, "Inc I", 5, 1.0, 0.0, 20.0, NamedTextColor.GREEN),
        UNCOMMON_2(2, "Inc II", 10, 1.5, 0.0, 30.0, NamedTextColor.GREEN),
        UNCOMMON_3(3, "Inc III", 15, 2.0, 0.0, 45.0, NamedTextColor.GREEN),
        UNCOMMON_4(4, "Inc IV", 20, 2.5, 0.05, 60.0, NamedTextColor.GREEN),

        // T5-T8: Raro (Níveis 25-40)
        RARE_1(5, "Raro I", 25, 3.5, 0.05, 80.0, NamedTextColor.BLUE),
        RARE_2(6, "Raro II", 30, 4.5, 0.08, 100.0, NamedTextColor.BLUE),
        RARE_3(7, "Raro III", 35, 5.5, 0.10, 120.0, NamedTextColor.BLUE),
        RARE_4(8, "Raro IV", 40, 6.5, 0.12, 150.0, NamedTextColor.BLUE),

        // T9-T11: Épico (Níveis 45-60)
        EPIC_1(9, "Épico I", 45, 8.0, 0.15, 180.0, NamedTextColor.LIGHT_PURPLE),
        EPIC_2(10, "Épico II", 50, 10.0, 0.18, 220.0, NamedTextColor.LIGHT_PURPLE),
        EPIC_3(11, "Épico III", 55, 12.0, 0.20, 260.0, NamedTextColor.LIGHT_PURPLE),

        // T12-T13: Lendário (End Game - Cap 400 Armor Total)
        LEGENDARY_1(12, "Lendário", 60, 15.0, 0.25, 320.0, NamedTextColor.GOLD),
        LEGENDARY_2(13, "Divino", 70, 20.0, 0.30, 400.0, NamedTextColor.GOLD);

        final int id;
        final String tag;
        final int requiredLevel;
        final double damageBonus;
        final double speedBonus;
        final double armorSetTotal; // Valor final aproximado da peça
        final NamedTextColor color;

        ItemTier(int id, String tag, int reqLevel, double dmg, double spd, double armorSetTotal, NamedTextColor color) {
            this.id = id;
            this.tag = tag;
            this.requiredLevel = reqLevel;
            this.damageBonus = dmg;
            this.speedBonus = spd;
            this.armorSetTotal = armorSetTotal;
            this.color = color;
        }
    }

    public ItemStack generateLoot(int mobLevel) {
        Material material = selectBaseMaterial(mobLevel);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        // Seleção do Tier
        ItemTier tier = selectTier(mobLevel);

        // Nome Formatado: "Espada de Ferro [T3]"
        Component name = Component.translatable(item.getType())
                .color(tier.color)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" [" + tier.tag + "]", tier.color).decoration(TextDecoration.BOLD, true));

        meta.displayName(name);

        // Lore Informativa
        List<Component> lore = new ArrayList<>();
        // Level Gating na Lore
        lore.add(Component.text("Nível Necessário: " + tier.requiredLevel, NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Atributos:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

        // Atributos
        EquipmentSlot slot = getSlotForMaterial(material);
        Multimap<Attribute, AttributeModifier> defaults = material.getDefaultAttributeModifiers(slot);

        double displayDamage = 0;
        double displaySpeed = 4.0; // Velocidade base da mão vazia
        double displayArmor = 0;
        boolean hasStats = false;

        for (Map.Entry<Attribute, AttributeModifier> entry : defaults.entries()) {
            Attribute attr = entry.getKey();
            AttributeModifier originalMod = entry.getValue();
            double finalValue = originalMod.getAmount(); // O valor 'cru' (ex: 5.0 para Iron Sword)

            // Aplica os bônus do Tier
            if (attr == Attribute.ATTACK_DAMAGE) {
                finalValue += tier.damageBonus;
                // CORREÇÃO VISUAL: Soma +1.0 (Mão) apenas para mostrar na Lore
                displayDamage = finalValue + 1.0;
                hasStats = true;
            } else if (attr == Attribute.ATTACK_SPEED) {
                finalValue += tier.speedBonus;
                // CORREÇÃO VISUAL: Soma +4.0 (Base Speed) para mostrar valor positivo
                displaySpeed = 4.0 + finalValue;
                hasStats = true;
            } else if (attr == Attribute.ARMOR) {
                // Aqui substitui o valor vanilla pelo peso do Tier
                // Aplica peso baseado no slot para valorizar peças maiores
                double multiplier = getArmorMultiplier(slot);
                // Valor Final = (Total do Set * Multiplicador)
                finalValue = tier.armorSetTotal * multiplier;
                displayArmor = finalValue; // Armadura não tem base de 'corpo pelado', é direta
                hasStats = true;
            }

            // Recria o modificador (Aqui é usado o finalValue SEM somar o +1 da mão, pois o jogo já soma)
            if (finalValue != 0) {
                // Remove o atributo vanilla (padrão)
                meta.removeAttributeModifier(attr);

                if (attr == Attribute.ARMOR) {
                    // Salva NBT e zera o vanilla
                    // Isso impede que o Minecraft reduza o dano ou mostre a barra bugada.
                    meta.getPersistentDataContainer().set(BukkitConstants.RPG_ARMOR_KEY, PersistentDataType.DOUBLE, finalValue);

                    String uniqueKey = "rpg_dummy_" + UUID.randomUUID();
                    AttributeModifier dummyMod = new AttributeModifier(
                            new NamespacedKey(plugin, uniqueKey),
                            0.0,
                            originalMod.getOperation(),
                            originalMod.getSlotGroup()
                    );
                    meta.addAttributeModifier(attr, dummyMod);
                } else {
                    // Dano/Speed Vanilla modificado
                    String uniqueKey = "rpg_bonus_" + UUID.randomUUID();
                    AttributeModifier newMod = new AttributeModifier(
                            new NamespacedKey(plugin, uniqueKey),
                            finalValue,
                            originalMod.getOperation(),
                            originalMod.getSlotGroup()
                    );
                    meta.addAttributeModifier(attr, newMod);
                }
            }
        }

        // Esconde atributos nativos
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Lore Stats
        if (hasStats) {
            if (displayDamage > 1.0)
                lore.add(Component.text(" ⚔ Dano: " + String.format("%.1f", displayDamage), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            if (slot == EquipmentSlot.HAND && displaySpeed > 0)
                lore.add(Component.text(" 🗡 Velocidade: " + String.format("%.2f", displaySpeed), NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
            if (displayArmor > 0)
                lore.add(Component.text(" 🛡 Defesa: " + String.format("%.0f", displayArmor), NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
        }

        // Salva Metadados Críticos (NBT)
        meta.getPersistentDataContainer().set(BukkitConstants.RPG_ITEM_KEY, PersistentDataType.STRING, tier.name());
        meta.getPersistentDataContainer().set(BukkitConstants.RPG_REQ_LEVEL_KEY, PersistentDataType.INTEGER, tier.requiredLevel);

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // Proporção baseada no Vanilla (20 Total: 8 Peito, 6 Calça, 3 Cap, 3 Bota)
    private double getArmorMultiplier(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> 0.40; // 40% (Ex: 160 de 400)
            case LEGS -> 0.30;  // 30% (Ex: 120 de 400)
            case HEAD, FEET -> 0.15; // 15% (Ex: 60 de 400)
            default -> 0.0;
        };
    }

    private Material selectBaseMaterial(int level) {
        List<Material> tier1 = Arrays.asList(
                Material.WOODEN_SWORD, Material.WOODEN_AXE,
                Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
        );
        List<Material> tier2 = Arrays.asList(
                Material.STONE_SWORD, Material.STONE_AXE,
                Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS
        );
        List<Material> tier3 = Arrays.asList(
                Material.IRON_SWORD, Material.IRON_AXE,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS
        );
        List<Material> tier4 = Arrays.asList(
                Material.DIAMOND_SWORD, Material.DIAMOND_AXE,
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS
        );
        List<Material> tier5 = Arrays.asList(
                Material.NETHERITE_SWORD, Material.NETHERITE_AXE,
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
        );

        List<Material> pool;
        if (level < 5) pool = tier1;
        else if (level < 15) pool = tier2;
        else if (level < 30) pool = tier3;
        else if (level < 50) pool = tier4;
        else pool = tier5;

        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * FÓRMULA DE PROGRESSÃO
     * Score = RNG(0..100) + (Nível * Multiplicador)
     */
    private ItemTier selectTier(int mobLevel) {
        double luck = random.nextDouble() * 100;
        // Nível 50 Mob = +100 bonus -> Pode alcançar tiers altos
        double score = luck + (mobLevel * 2.0);

        if (score >= 190) return ItemTier.LEGENDARY_2; // Só Mob Lv 45+ com muita sorte
        if (score >= 170) return ItemTier.LEGENDARY_1;
        if (score >= 155) return ItemTier.EPIC_3;
        if (score >= 140) return ItemTier.EPIC_2;
        if (score >= 125) return ItemTier.EPIC_1;
        if (score >= 110) return ItemTier.RARE_4;
        if (score >= 95) return ItemTier.RARE_3;
        if (score >= 80) return ItemTier.RARE_2;
        if (score >= 65) return ItemTier.RARE_1;
        if (score >= 50) return ItemTier.UNCOMMON_4;
        if (score >= 40) return ItemTier.UNCOMMON_3;
        if (score >= 30) return ItemTier.UNCOMMON_2;
        if (score >= 20) return ItemTier.UNCOMMON_1;

        return ItemTier.COMMON;
    }

    private EquipmentSlot getSlotForMaterial(Material mat) {
        String name = mat.name();
        if (name.contains("HELMET")) return EquipmentSlot.HEAD;
        if (name.contains("CHESTPLATE") || name.contains("ELYTRA")) return EquipmentSlot.CHEST;
        if (name.contains("LEGGINGS")) return EquipmentSlot.LEGS;
        if (name.contains("BOOTS")) return EquipmentSlot.FEET;
        if (name.contains("SHIELD")) return EquipmentSlot.OFF_HAND;
        return EquipmentSlot.HAND;
    }
}