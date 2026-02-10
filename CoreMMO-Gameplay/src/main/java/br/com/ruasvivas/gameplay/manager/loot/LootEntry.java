package br.com.ruasvivas.gameplay.manager.loot;

import org.bukkit.Material;

public class LootEntry {
    public enum DropType {VANILLA, RPG}

    private final DropType type;
    private final Material material; // Null se for RPG
    private final double baseChance;
    private final double levelBonus; // Chance extra por nível do mob
    private final int minAmount;
    private final int maxAmount;

    public LootEntry(DropType type, Material material, double baseChance, double levelBonus, int min, int max) {
        this.type = type;
        this.material = material;
        this.baseChance = baseChance;
        this.levelBonus = levelBonus;
        this.minAmount = min;
        this.maxAmount = max;
    }

    // Getters
    public DropType getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    public double getChance(int mobLevel) {
        return baseChance + (mobLevel * levelBonus);
    }

    public int getMinAmount() {
        return minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }
}