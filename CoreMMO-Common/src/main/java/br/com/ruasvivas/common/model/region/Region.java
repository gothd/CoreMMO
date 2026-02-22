package br.com.ruasvivas.common.model.region;

import java.util.UUID;

public class Region {
    private String id; // Ex: "cidade_norte", "dungeon_orc"
    private RegionType type;
    private String worldName;

    // Coordenadas extremas (Bounding Box)
    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;

    // Prioridade para sobreposição (Arena dentro de Cidade)
    private int priority;

    // Se for GUILD_CLAIM, terá o ID da guilda. Se for = 0, é do Servidor (Admin).
    private int ownerGuildId;

    public Region(String id, RegionType type, String worldName,
                  double x1, double y1, double z1,
                  double x2, double y2, double z2, int priority) {
        this.id = id;
        this.type = type;
        this.worldName = worldName;

        // Garante que min seja sempre o menor e max o maior
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);

        this.priority = priority;
        this.ownerGuildId = 0;
    }

    public boolean contains(String world, double x, double y, double z) {
        if (!this.worldName.equalsIgnoreCase(world)) return false;
        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public RegionType getType() {
        return type;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getOwnerGuildId() {
        return ownerGuildId;
    }

    public void setOwnerGuildId(int ownerGuildId) {
        this.ownerGuildId = ownerGuildId;
    }

    public double getMinX() {
        return this.minX;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public double getMinZ() {
        return this.minZ;
    }

    public double getMaxZ() {
        return this.maxZ;
    }
}