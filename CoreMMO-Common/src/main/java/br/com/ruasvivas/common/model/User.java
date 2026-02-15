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

    /**
     * Calcula o XP total necessário para alcançar o Nível X.
     * Fórmula: 50 * (Nivel^2) - (50 * Nivel)
     * Lvl 1: 0
     * Lvl 2: 100
     * Lvl 10: 4.500
     * Lvl 50: 122.500
     */
    public long getExpForLevel(int targetLevel) {
        if (targetLevel <= 1) return 0;
        return 50L * targetLevel * targetLevel - (50L * targetLevel);
    }

    /**
     * Adiciona XP e processa Level Up.
     * @return true se o jogador subiu de nível.
     */
    public boolean addExperience(long amount) {
        this.experience += amount;
        boolean leveledUp = false;

        // Loop while para suportar subir múltiplos níveis de uma vez (ex: Matou Boss)
        while (this.experience >= getExpForLevel(this.level + 1)) {
            this.level++;
            leveledUp = true;
        }
        return leveledUp;
    }

    /**
     * Retorna a porcentagem (0 a 100) do progresso atual para o próximo nível.
     * Útil para Scoreboards e Barras de Boss.
     */
    public int getProgressPercentage() {
        long currentLevelExp = getExpForLevel(this.level);
        long nextLevelExp = getExpForLevel(this.level + 1);

        long needed = nextLevelExp - currentLevelExp;
        long current = this.experience - currentLevelExp;

        if (needed <= 0) return 100; // Prevenção de divisão por zero

        return (int) ((current * 100.0) / needed);
    }

    /**
     * Recalcula os atributos máximos com base na classe e nível atuais.
     * Deve ser chamado ao upar de nível ou trocar de classe.
     */
    public void recalculateStats() {
        // Base Stats (Todo mundo começa com isso)
        double baseHealth = 20.0;
        double baseMana = 50.0;

        // Bônus de Classe * Nível
        double healthBonus = rpgClass.getHealthPerLevel() * this.level;
        double manaBonus = rpgClass.getManaPerLevel() * this.level;

        // Define os novos máximos
        // Nota: A vida atual não é setada aqui pois User não tem acesso ao Player do Bukkit
        this.maxMana = baseMana + manaBonus;

        // Garante que a mana atual não ultrapasse o máximo
        if (this.mana > this.maxMana) {
            this.mana = this.maxMana;
        }
    }

    /**
     * Calcula a Vida Máxima ideal (Para ser aplicada no Bukkit).
     */
    public double getCalculatedMaxHealth() {
        return 20.0 + (rpgClass.getHealthPerLevel() * this.level);
    }
}