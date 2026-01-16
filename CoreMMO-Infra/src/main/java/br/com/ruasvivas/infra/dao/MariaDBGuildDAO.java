package br.com.ruasvivas.infra.dao;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.GuildDAO;
import br.com.ruasvivas.api.database.IDatabase;
import br.com.ruasvivas.common.model.Guild;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MariaDBGuildDAO implements GuildDAO {

    private Connection getConnection() throws SQLException {
        return CoreRegistry.get(IDatabase.class).getConnection();
    }

    private Logger getLogger() {
        return CoreRegistry.getSafe(Logger.class).orElse(Logger.getLogger("CoreMMO-Infra"));
    }

    @Override
    public void createGuild(Guild guild) {
        String sql = "INSERT INTO guildas (nome, tag, lider_uuid) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guild.getName());
            ps.setString(2, guild.getTag());
            ps.setString(3, guild.getLeaderUuid().toString());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) guild.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            getLogger().severe("Erro ao criar guilda: " + e.getMessage());
        }
    }

    @Override
    public List<Guild> fetchAll() {
        List<Guild> list = new ArrayList<>();
        String sql = "SELECT * FROM guildas";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Guild g = new Guild(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("tag"),
                        UUID.fromString(rs.getString("lider_uuid"))
                );
                // Calcula estatísticas on-the-fly para o cache inicial
                updateStatistics(g);
                list.add(g);
            }
        } catch (SQLException e) {
            getLogger().severe("Erro ao buscar guildas: " + e.getMessage());
        }
        return list;
    }

    @Override
    public void updateStatistics(Guild guild) {
        String sql = "SELECT COUNT(*) as membros, SUM(kills_pve + kills_pvp) as kills, SUM(mortes) as mortes FROM jogadores WHERE guilda_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guild.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                guild.setMemberCount(rs.getInt("membros"));
                guild.setTotalKills(rs.getInt("kills"));
                guild.setTotalDeaths(rs.getInt("mortes"));
            }
        } catch (SQLException e) {
            getLogger().severe("Erro ao calcular stats da guilda: " + e.getMessage());
        }
    }

    @Override
    public void deleteGuild(int id) {
        String sql = "DELETE FROM guildas WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro ao deletar guilda: " + e.getMessage());
        }
    }
}