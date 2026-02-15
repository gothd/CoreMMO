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
    public final NamespacedKey RPG_ITEM_KEY;
    public final NamespacedKey RPG_ARMOR_KEY;

    public ItemGenerator(JavaPlugin plugin) {
        this.plugin = plugin;
        this.RPG_ITEM_KEY = new NamespacedKey(plugin, "rpg_item_data");
        this.RPG_ARMOR_KEY = new NamespacedKey(plugin, "rpg_armor_value");
    }

    private enum ItemTier {
        TIER_1(1, "T1", 0.0, 0.0, 0.0, NamedTextColor.GRAY),        // Comum
        TIER_2(2, "T2", 1.0, 0.0, 1.0, NamedTextColor.GREEN),       // Incomum
        TIER_3(3, "T3", 2.0, 0.1, 2.0, NamedTextColor.BLUE),        // Raro
        TIER_4(4, "T4", 3.5, 0.2, 3.0, NamedTextColor.LIGHT_PURPLE), // Épico
        TIER_5(5, "T5", 5.0, 0.3, 5.0, NamedTextColor.GOLD);        // Lendário

        final int level;
        final String tag;
        final double damageBonus;
        final double speedBonus;
        final double armorBonus;
        final NamedTextColor color;

        ItemTier(int level, String tag, double damageBonus, double speedBonus, double armorBonus, NamedTextColor color) {
            this.level = level;
            this.tag = tag;
            this.damageBonus = damageBonus;
            this.speedBonus = speedBonus;
            this.armorBonus = armorBonus;
            this.color = color;
        }
    }

    public ItemStack generateLoot(int mobLevel) {
        Material material = selectBaseMaterial(mobLevel);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        // Seleção baseada em fórmula
        ItemTier tier = selectTier(mobLevel);

        // Nome Formatado: "Espada de Ferro [T3]"
        Component name = Component.translatable(item.getType())
                .color(tier.color)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" [" + tier.tag + "]", tier.color).decoration(TextDecoration.BOLD, true));

        meta.displayName(name);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Nível do Item: " + mobLevel, NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Estatísticas:", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));

        // Cálculo dos Atributos
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
                // Soma o bônus, MAS NÃO aplica o atributo vanilla.
                // Apenas salva para uso visual e lógico.
                finalValue += tier.armorBonus;
                displayArmor = finalValue; // Armadura não tem base de 'corpo pelado', é direta
                hasStats = true;
            }

            // Recria o modificador (Aqui é usado o finalValue SEM somar o +1 da mão, pois o jogo já soma)
            if (finalValue != 0) {
                // Remove o atributo vanilla (padrão)
                meta.removeAttributeModifier(attr);

                // Se for ARMADURA, NÃO adicionamos o modificador vanilla!
                // Isso impede que o Minecraft reduza o dano ou mostre a barra bugada.
                if (attr == Attribute.ARMOR) {
                    // Salva APENAS no NBT (Visual/Lógica Custom)
                    meta.getPersistentDataContainer().set(BukkitConstants.RPG_ARMOR_KEY, PersistentDataType.DOUBLE, finalValue);
                }
                else {
                    // Para Dano/Speed: Recria o modificador vanilla para funcionar no cliente
                    String uniqueKey = "rpg_bonus_" + UUID.randomUUID();
                    AttributeModifier newMod = new AttributeModifier(
                            new NamespacedKey(plugin, uniqueKey),
                            finalValue,
                            originalMod.getOperation(),
                            originalMod.getSlotGroup()
                    );
                    // Adiciona o novo (RPG)
                    meta.addAttributeModifier(attr, newMod);
                }
            }
        }

        // Esconde atributos nativos
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Escreve a Lore com os valores corrigidos para leitura humana
        if (hasStats) {
            if (displayDamage > 1.0) {
                lore.add(Component.text(" ⚔ Dano: " + String.format("%.1f", displayDamage), NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            }
            if (slot == EquipmentSlot.HAND && displaySpeed > 0) {
                lore.add(Component.text(" 🗡 Velocidade: " + String.format("%.2f", displaySpeed), NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, false));
            }
            if (displayArmor > 0) {
                lore.add(Component.text(" 🛡 Armadura: " + String.format("%.1f", displayArmor), NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            }
        }

        // Salva dados no NBT
        meta.getPersistentDataContainer().set(RPG_ITEM_KEY, PersistentDataType.STRING, tier.tag);
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Material selectBaseMaterial(int level) {
        List<Material> tier1 = Arrays.asList(Material.WOODEN_SWORD, Material.WOODEN_AXE, Material.LEATHER_CHESTPLATE, Material.LEATHER_BOOTS);
        List<Material> tier2 = Arrays.asList(Material.STONE_SWORD, Material.STONE_AXE, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET);
        List<Material> tier3 = Arrays.asList(Material.IRON_SWORD, Material.IRON_AXE, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS);
        List<Material> tier4 = Arrays.asList(Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_BOOTS);
        List<Material> tier5 = Arrays.asList(Material.NETHERITE_SWORD, Material.NETHERITE_AXE, Material.NETHERITE_CHESTPLATE);

        List<Material> pool;
        if (level < 3) pool = tier1;
        else if (level < 5) pool = tier2;
        else if (level < 10) pool = tier3;
        else if (level < 20) pool = tier4;
        else pool = tier5;

        return pool.get(random.nextInt(pool.size()));
    }

    /**
     * FÓRMULA DE PROGRESSÃO
     * Score = RNG(0..100) + (Nível * Multiplicador)
     */
    private ItemTier selectTier(int level) {
        // Rola um dado de 0 a 100
        double luck = random.nextDouble() * 100;

        // Bônus de nível: Cada nível do mob adiciona 3.5 pontos ao score
        // Ex: Nível 1 = +3.5 | Nível 10 = +35 | Nível 20 = +70
        double levelBonus = level * 3.5;

        double score = luck + levelBonus;

        // Tabela de Pontuação (Thresholds)
        // Para conseguir T5 (145pts):
        // - Nível 1 (Max Score 103.5): IMPOSSÍVEL
        // - Nível 10 (Max Score 135):  IMPOSSÍVEL (Isso valoriza mobs high level)
        // - Nível 15 (Max Score 152):  Possível com muita sorte (Top 5% rolls)
        // - Nível 25 (Max Score 187):  Frequente

        if (score >= 145) return ItemTier.TIER_5; // Lendário
        if (score >= 115) return ItemTier.TIER_4; // Épico
        if (score >= 85)  return ItemTier.TIER_3; // Raro
        if (score >= 50)  return ItemTier.TIER_2; // Incomum

        return ItemTier.TIER_1; // Comum (Lixo/Padrão)
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