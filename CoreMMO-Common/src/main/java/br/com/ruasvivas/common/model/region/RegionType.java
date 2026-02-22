package br.com.ruasvivas.common.model.region;

public enum RegionType {
    SAFE_ZONE(false, false, false),      // Cidade (Sem PvP, Sem Mob, Sem Quebrar)
    PVP_ARENA(false, false, true),       // Arena na cidade (Com PvP, Sem Mob, Sem Quebrar)
    DUNGEON(false, true, false),         // Masmorra Padrão (Sem PvP, Com Mob, Sem Quebrar)
    CONTESTED_DUNGEON(false, true, true),// Masmorra de Guerra (Com PvP, Com Mob, Sem Quebrar)
    GUILD_CLAIM(false, true, false),     // Terreno de Guilda (Sem PvP entre si, Com Mob)
    WILDERNESS(true, true, true);        // Padrão (Tudo liberado)

    private final boolean canBuildDefault;
    private final boolean allowMobSpawn;
    private final boolean allowPvP;

    RegionType(boolean canBuildDefault, boolean allowMobSpawn, boolean allowPvP) {
        this.canBuildDefault = canBuildDefault;
        this.allowMobSpawn = allowMobSpawn;
        this.allowPvP = allowPvP;
    }

    public boolean isCanBuildDefault() { return canBuildDefault; }
    public boolean isAllowMobSpawn() { return allowMobSpawn; }
    public boolean isAllowPvP() { return allowPvP; }
}