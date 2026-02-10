package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.gameplay.manager.loot.LootEntry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class LootManager {

    private final JavaPlugin plugin;
    private final ItemGenerator itemGenerator;
    private final Map<EntityType, List<LootEntry>> lootTable = new HashMap<>();
    private final Random random = new Random();

    public LootManager(JavaPlugin plugin, ItemGenerator itemGenerator) {
        this.plugin = plugin;
        this.itemGenerator = itemGenerator;
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig(); // Garante que leu do disco antes de processar
        lootTable.clear();
        FileConfiguration config = plugin.getConfig();

        // BÔNUS GLOBAL
        double globalLevelBonus = config.getDouble("loot-system.global-level-bonus", 0.0);

        ConfigurationSection mobsSection = config.getConfigurationSection("loot-system.mobs");

        if (mobsSection == null) return;

        for (String key : mobsSection.getKeys(false)) {
            try {
                EntityType type = EntityType.valueOf(key.toUpperCase());
                List<LootEntry> entries = new ArrayList<>();

                List<Map<?, ?>> dropsList = mobsSection.getMapList(key + ".drops");
                for (Map<?, ?> rawMap : dropsList) {
                    // O YAML do Bukkit sempre usa chaves String, então é seguro.
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dropMap = (Map<String, Object>) rawMap;

                    String typeStr = (String) dropMap.getOrDefault("type", "VANILLA");

                    // Uso do método auxiliar para números
                    double baseChance = getDouble(dropMap, "chance", 0.1);
                    double specificBonus = getDouble(dropMap, "level-bonus", 0.0);

                    // APLICA O BÔNUS GLOBAL (Soma ao bônus específico)
                    double totalLevelBonus = specificBonus + globalLevelBonus;

                    if (typeStr.equalsIgnoreCase("RPG")) {
                        entries.add(new LootEntry(LootEntry.DropType.RPG, null, baseChance, totalLevelBonus, 1, 1));
                    } else {
                        String matStr = (String) dropMap.get("material");
                        // Mesma lógica segura para Integers
                        int min = getInt(dropMap, "min", 1);
                        int max = getInt(dropMap, "max", 1);

                        if (matStr != null) {
                            Material mat = Material.valueOf(matStr);
                            entries.add(new LootEntry(LootEntry.DropType.VANILLA, mat, baseChance, totalLevelBonus, min, max));
                        }
                    }
                }

                lootTable.put(type, entries);
                plugin.getLogger().info("Loot carregado para: " + type.name() + " (" + entries.size() + " drops)");

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Erro ao carregar loot para " + key, e);
            }
        }
    }

    /**
     * Gera uma lista de itens baseada na sorte e no nível do mob.
     */
    public List<ItemStack> getDropsFor(EntityType type, int mobLevel) {
        List<ItemStack> drops = new ArrayList<>();
        List<LootEntry> entries = lootTable.get(type);

        if (entries == null) return drops;

        for (LootEntry entry : entries) {
            // Calcula chance com bônus de nível
            double chance = entry.getChance(mobLevel);

            // Roda os dados (RNG)
            if (random.nextDouble() <= chance) {
                if (entry.getType() == LootEntry.DropType.RPG) {
                    // Chama nosso gerador complexo da aula anterior
                    drops.add(itemGenerator.generateLoot(mobLevel));
                } else {
                    // Item Vanilla simples
                    int amount = random.nextInt((entry.getMaxAmount() - entry.getMinAmount()) + 1) + entry.getMinAmount();
                    drops.add(new ItemStack(entry.getMaterial(), amount));
                }
            }
        }
        return drops;
    }

    // --- Métodos Auxiliares para evitar ClassCastException (Integer vs Double) ---

    private double getDouble(Map<String, Object> map, String key, double def) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue(); // Converte qualquer número (1 ou 1.0) para double
        }
        return def;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return def;
    }
}