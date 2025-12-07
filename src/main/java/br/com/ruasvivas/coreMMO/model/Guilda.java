package br.com.ruasvivas.coreMMO.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Guilda {

    private int id;
    private String nome;
    private String tag;
    private UUID liderUuid;

    // Dados Transitórios (Calculados na hora)
    private int totalKills;
    private int totalMortes;
    private int quantidadeMembros;
    private int membrosMax;

    // Construtor para novas guildas
    public Guilda(String nome, String tag, UUID liderUuid) {
        this.nome = nome;
        this.tag = tag;
        this.liderUuid = liderUuid;
        this.membrosMax = 10;
    }

    // Construtor para carregar do banco
    public Guilda(int id, String nome, String tag, UUID liderUuid) {
        this(nome, tag, liderUuid);
        this.id = id;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getTag() {
        return tag;
    }

    public UUID getLiderUuid() {
        return liderUuid;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(int k) {
        this.totalKills = k;
    }

    public int getTotalMortes() {
        return totalMortes;
    }

    public void setTotalMortes(int m) {
        this.totalMortes = m;
    }

    public int getQuantidadeMembros() {
        return quantidadeMembros;
    }

    public void setQuantidadeMembros(int q) {
        this.quantidadeMembros = q;
    }

    public int getMembrosMax() {
        return membrosMax;
    }

    public double getKDR() {
        if (totalMortes == 0) return totalKills;
        return (double) totalKills / (double) totalMortes;
    }
}