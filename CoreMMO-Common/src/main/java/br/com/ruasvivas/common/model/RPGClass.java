package br.com.ruasvivas.common.model;

public enum RPGClass {

    // Constante em Inglês (Código) -> Texto em PT-BR (Cliente)
    // Base: 20 HP, 50 Mana
    NOVICE("Novato", "GRAY", "LEATHER_HELMET",
            null, 10, 2.0,
            1.0, 2.0, // +1 HP/lvl, +2 Mana/lvl
            "O início da jornada.", "Regeneração acelerada."),

    // Tank: Muita Vida, Pouca Mana
    WARRIOR("Guerreiro", "RED", "IRON_SWORD",
            "NOVICE", 100, 1.0,
            5.0, 1.0, // +5 HP/lvl (Tank), +1 Mana/lvl
            "Mestre do combate.", "Dano físico massivo."),

    // Glass Cannon: Pouca Vida, Muita Mana
    MAGE("Mago", "BLUE", "BLAZE_ROD",
            "NOVICE", 100, 2.0,
            2.0, 10.0, // +2 HP/lvl, +10 Mana/lvl
            "Dominador arcano.", "Dano mágico em área."),

    // Balanced
    ARCHER("Arqueiro", "GREEN", "CROSSBOW",
            "NOVICE", 100, 1.5,
            3.0, 4.0, // +3 HP/lvl, +4 Mana/lvl
            "Precisão letal.", "Agilidade e Crítico.");

    // Campos internos
    private final String displayName;
    private final String colorName;      // Ex: "RED", "BLUE" (Web friendly)
    private final String iconMaterial;   // Ex: "IRON_SWORD" (Web friendly)
    private final String parentClass;    // Nome da classe anterior
    private final int maxLevel;
    private final double manaRegen;
    private final double healthPerLevel;
    private final double manaPerLevel;
    private final String[] description;

    RPGClass(String displayName, String colorName, String iconMaterial,
             String parentClass, int maxLevel, double manaRegen,
             double healthPerLevel, double manaPerLevel,
             String... description) {
        this.displayName = displayName;
        this.colorName = colorName;
        this.iconMaterial = iconMaterial;
        this.parentClass = parentClass;
        this.maxLevel = maxLevel;
        this.manaRegen = manaRegen;
        this.healthPerLevel = healthPerLevel;
        this.manaPerLevel = manaPerLevel;
        this.description = description;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public String getColorName() {
        return colorName;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public String getParentClass() {
        return parentClass;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getManaRegen() {
        return manaRegen;
    }

    public String[] getDescription() {
        return description;
    }

    public double getHealthPerLevel() {
        return healthPerLevel;
    }

    public double getManaPerLevel() {
        return manaPerLevel;
    }

    public boolean isStarter() {
        return parentClass == null;
    }
}