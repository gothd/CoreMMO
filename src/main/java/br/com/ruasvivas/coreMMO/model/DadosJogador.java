package br.com.ruasvivas.coreMMO.model;

import org.bukkit.Location;

import java.util.UUID;

public class DadosJogador {

    // Identidade
    private final UUID uuid;
    private final String username;

    // Progresso
    private int nivel;
    private long experiencia;
    private long moedas;

    // Atributos
    private double mana;
    private double maxMana;

    // 0 significa "Sem Guilda" (no banco será NULL)
    private int guildaId;

    // Localização
    private String mundo;
    private double x, y, z;
    private float yaw, pitch;

    public DadosJogador(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.nivel = 1;
        this.experiencia = 0;
        this.moedas = 0;
        this.mana = 100;
        this.maxMana = 100;
    }

    // --- Getters e Setters ---
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public long getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(long experiencia) {
        this.experiencia = experiencia;
    }

    public long getMoedas() {
        return moedas;
    }

    public void setMoedas(long moedas) {
        this.moedas = moedas;
    }

    public double getMana() {
        return mana;
    }

    public double getMaxMana() {
        return maxMana;
    }

    // Setter Inteligente (Clamping)
    public void setMana(double mana) {
        // Garante que a mana nunca seja menor que 0 ou maior que o máximo
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }

    public void setMaxMana(double maxMana) {
        this.maxMana = maxMana;
    }

    public int getGuildaId() {
        return guildaId;
    }

    public void setGuildaId(int id) {
        this.guildaId = id;
    }

    // Método utilitário para facilitar verificações
    public boolean temGuilda() {
        return guildaId > 0;
    }

    // --- Localização ---
    // Método 1: Recebe dados brutos do Banco
    public void setLocalizacao(String mundo, double x, double y, double z, float yaw, float pitch) {
        this.mundo = mundo;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    // Método 2: Recebe objeto do Bukkit (Facilitador)
    public void setLocalizacao(Location loc) {
        this.mundo = loc.getWorld().getName();
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
    }

    // Getters de localização...
    public String getMundo() {
        return mundo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}