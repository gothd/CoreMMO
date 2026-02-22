package br.com.ruasvivas.infra.dao;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.RegionDAO;
import br.com.ruasvivas.api.database.IDatabase;
import br.com.ruasvivas.common.model.region.Region;
import br.com.ruasvivas.common.model.region.RegionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MariaDBRegionDAO implements RegionDAO {

    private final IDatabase database;

    public MariaDBRegionDAO() {
        this.database = CoreRegistry.get(IDatabase.class);
    }

    private Logger getLogger() {
        return CoreRegistry.getSafe(Logger.class).orElse(Logger.getLogger("CoreMMO-Infra"));
    }

    @Override
    public void saveRegion(Region region) {
        String sql = """
                    INSERT INTO coremmo_regions (id, type, world, min_x, min_y, min_z, max_x, max_y, max_z, priority, owner_guild_id)\s
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\s
                    ON DUPLICATE KEY UPDATE\s
                    type=VALUES(type), world=VALUES(world),
                    min_x=VALUES(min_x), min_y=VALUES(min_y), min_z=VALUES(min_z),
                    max_x=VALUES(max_x), max_y=VALUES(max_y), max_z=VALUES(max_z),
                    priority=VALUES(priority), owner_guild_id=VALUES(owner_guild_id)
                """;

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, region.getId());
            ps.setString(2, region.getType().name());
            ps.setString(3, region.getWorldName());
            ps.setDouble(4, region.getMinX());
            ps.setDouble(5, region.getMinY());
            ps.setDouble(6, region.getMinZ());
            ps.setDouble(7, region.getMaxX());
            ps.setDouble(8, region.getMaxY());
            ps.setDouble(9, region.getMaxZ());
            ps.setInt(10, region.getPriority());
            ps.setInt(11, region.getOwnerGuildId());

            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro SQL ao salvar região: " + e.getMessage());
        }
    }

    @Override
    public void deleteRegion(String id) {
        String sql = "DELETE FROM coremmo_regions WHERE id = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro ao deletar região: " + e.getMessage());
        }
    }

    @Override
    public List<Region> loadAllRegions() {
        List<Region> regions = new ArrayList<>();
        String sql = "SELECT * FROM coremmo_regions";

        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RegionType type = RegionType.valueOf(rs.getString("type"));
                Region region = new Region(
                        rs.getString("id"),
                        type,
                        rs.getString("world"),
                        rs.getDouble("min_x"),
                        rs.getDouble("min_y"),
                        rs.getDouble("min_z"),
                        rs.getDouble("max_x"),
                        rs.getDouble("max_y"),
                        rs.getDouble("max_z"),
                        rs.getInt("priority")
                );
                region.setOwnerGuildId(rs.getInt("owner_guild_id"));
                regions.add(region);
            }
        } catch (SQLException e) {
            getLogger().severe("Erro ao carregar todas as regiões: " + e.getMessage());
        }
        return regions;
    }
}