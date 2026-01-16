package br.com.ruasvivas.common.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Representa um jogador no ecossistema (Plugin e Web).
 * POJO Puro: Sem dependências de Bukkit.
 */
public class User {

    // Identidade
    private final UUID uuid;
    private final String username;

    // Lista de permissões (ex: "admin.ban", "vip.voar")
    // Inicializada como lista vazia para evitar NullPointerException
    private List<String> permissions = new ArrayList<>();

    // RPG e Progresso
    private RPGClass rpgClass = RPGClass.NOVICE; // Enum purificado criado anteriormente
    private int level;
    private long experience;
    private long coins;

    // Atributos de Combate
    private double mana;
    private double maxMana;

    // Social (0 = Sem Guilda)
    private int guildId;

    // Localização (Primitivos para compatibilidade Web)
    private String worldName;
    private double x, y, z;
    private float yaw, pitch;

    // Construtor básico
    public User(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.level = 1;
        this.experience = 0;
        this.coins = 0;
        this.mana = 100;
        this.maxMana = 100;
    }

    // --- Getters e Setters ---

    public UUID getUuid() { return uuid; }
    public String getUsername() { return username; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    // Helper para verificar permissão sem depender do Bukkit
    public boolean hasPermission(String node) {
        return permissions.contains(node) || permissions.contains("*");
    }

    public RPGClass getRpgClass() { return rpgClass; }
    public void setRpgClass(RPGClass rpgClass) { this.rpgClass = rpgClass; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public long getExperience() { return experience; }
    public void setExperience(long experience) { this.experience = experience; }

    public long getCoins() { return coins; }
    public void setCoins(long coins) { this.coins = coins; }

    public double getMana() { return mana; }
    public double getMaxMana() { return maxMana; }

    // Setter com validação de limites (Clamping)
    public void setMana(double mana) {
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }
    public void setMaxMana(double maxMana) { this.maxMana = maxMana; }

    public int getGuildId() { return guildId; }
    public void setGuildId(int guildId) { this.guildId = guildId; }

    public boolean hasGuild() { return guildId > 0; }

    // --- Manipulação de Localização ---

    public void setLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
}