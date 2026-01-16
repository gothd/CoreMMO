package br.com.ruasvivas.common.model;

public enum RPGClass {

    // Constante em Inglês (Código) -> Texto em PT-BR (Cliente)
    NOVICE("Novato", "GRAY", "LEATHER_HELMET",
            null, 10, 2.0,
            "O início da jornada.", "Regeneração acelerada."),

    WARRIOR("Guerreiro", "RED", "IRON_SWORD",
            "NOVICE", 100, 1.0,
            "Mestre do combate.", "Dano físico massivo."),

    MAGE("Mago", "BLUE", "BLAZE_ROD",
            "NOVICE", 100, 1.0,
            "Dominador arcano.", "Dano mágico em área."),

    ARCHER("Arqueiro", "GREEN", "CROSSBOW",
            "NOVICE", 100, 1.0,
            "Precisão letal.", "Agilidade e Crítico.");

    // Campos internos
    private final String displayName;
    private final String colorName;      // Ex: "RED", "BLUE" (Web friendly)
    private final String iconMaterial;   // Ex: "IRON_SWORD" (Web friendly)

    private final String parentClass;    // Nome da classe anterior
    private final int maxLevel;
    private final double manaRegen;
    private final String[] description;

    RPGClass(String displayName, String colorName, String iconMaterial,
             String parentClass, int maxLevel, double manaRegen,
             String... description) {
        this.displayName = displayName;
        this.colorName = colorName;
        this.iconMaterial = iconMaterial;
        this.parentClass = parentClass;
        this.maxLevel = maxLevel;
        this.manaRegen = manaRegen;
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

    // Helper para verificar se é classe inicial
    public boolean isStarter() {
        return parentClass == null;
    }
}