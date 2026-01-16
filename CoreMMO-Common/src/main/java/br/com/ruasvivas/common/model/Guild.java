package br.com.ruasvivas.common.model;

import java.util.UUID;

public class Guild {

    private int id;
    private String name;
    private String tag;
    private UUID leaderUuid;

    // Dados Transitórios (Calculados na hora)
    private int totalKills;
    private int totalDeaths;
    private int memberCount;
    private int maxMembers;

    // Construtor para novas Guildas
    public Guild(String name, String tag, UUID leaderUuid) {
        this.name = name;
        this.tag = tag;
        this.leaderUuid = leaderUuid;
        this.maxMembers = 10;
    }

    // Construtor para carregar do BD
    public Guild(int id, String name, String tag, UUID leaderUuid) {
        this(name, tag, leaderUuid);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public String getTag() { return tag; }
    public UUID getLeaderUuid() { return leaderUuid; }

    public int getTotalKills() { return totalKills; }
    public void setTotalKills(int k) { this.totalKills = k; }

    public int getTotalDeaths() { return totalDeaths; }
    public void setTotalDeaths(int m) { this.totalDeaths = m; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int q) { this.memberCount = q; }

    public int getMaxMembers() { return maxMembers; }

    public double getKDR() {
        if (totalDeaths == 0) return totalKills;
        return (double) totalKills / (double) totalDeaths;
    }
}