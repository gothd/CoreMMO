package br.com.ruasvivas.infra.dao;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.api.database.IDatabase;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.common.model.RPGClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MariaDBUserDAO implements UserDAO {

    private Connection getConnection() throws SQLException {
        // Pega a conexão do Pool registrado no CoreRegistry
        return CoreRegistry.get(IDatabase.class).getConnection();
    }

    private Logger getLogger() {
        // Pega o Logger registrado ou usamos um gênérico
        return CoreRegistry.getSafe(Logger.class).orElse(Logger.getLogger("CoreMMO-Infra"));
    }

    @Override
    public void createUser(UUID uuid, String username) {
        String sql = "INSERT IGNORE INTO jogadores (uuid, username) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro ao criar jogador: " + e.getMessage());
        }
    }

    @Override
    public User loadUser(UUID uuid) {
        String sql = "SELECT * FROM jogadores WHERE uuid = ?";

        String sqlPerms = "SELECT permissao FROM jogadores_permissoes WHERE uuid = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(uuid, rs.getString("username"));

                user.setLevel(rs.getInt("nivel"));
                user.setExperience(rs.getLong("experiencia"));
                user.setCoins(rs.getLong("moedas"));
                user.setMana(rs.getDouble("mana"));
                user.setMaxMana(rs.getDouble("mana_max"));
                user.setGuildId(rs.getInt("guilda_id"));

                try {
                    user.setRpgClass(RPGClass.valueOf(rs.getString("classe")));
                } catch (Exception e) {
                    user.setRpgClass(RPGClass.NOVICE);
                }

                // Localização
                user.setLocation(
                        rs.getString("loc_mundo"),
                        rs.getDouble("loc_x"), rs.getDouble("loc_y"), rs.getDouble("loc_z"),
                        rs.getFloat("loc_yaw"), rs.getFloat("loc_pitch")
                );

                try (PreparedStatement psPerms = conn.prepareStatement(sqlPerms)) {
                    psPerms.setString(1, uuid.toString());
                    ResultSet rsPerms = psPerms.executeQuery();

                    List<String> loadedPerms = new ArrayList<>();
                    while (rsPerms.next()) {
                        loadedPerms.add(rsPerms.getString("permissao"));
                    }
                    user.setPermissions(loadedPerms);
                }

                return user;
            }
        } catch (SQLException e) {
            getLogger().severe("Erro SQL ao carregar jogador: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean saveUser(User user) {
        String sql = """
                    UPDATE jogadores SET
                    nivel=?, experiencia=?, moedas=?,
                    mana=?, mana_max=?,
                    loc_mundo=?, loc_x=?, loc_y=?, loc_z=?, loc_yaw=?, loc_pitch=?,
                    guilda_id=?,
                    classe=?,
                    ultimo_login=CURRENT_TIMESTAMP
                    WHERE uuid=?
                """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // Progresso
            ps.setInt(1, user.getLevel());
            ps.setLong(2, user.getExperience());
            ps.setLong(3, user.getCoins());

            // Mana
            ps.setDouble(4, user.getMana());
            ps.setDouble(5, user.getMaxMana());

            // Localização
            ps.setString(6, user.getWorldName());
            ps.setDouble(7, user.getX());
            ps.setDouble(8, user.getY());
            ps.setDouble(9, user.getZ());
            ps.setFloat(10, user.getYaw());
            ps.setFloat(11, user.getPitch());

            // Guilda
            if (user.getGuildId() > 0) {
                ps.setInt(12, user.getGuildId());
            } else {
                // Se for 0, mandamos NULL explicitamente
                ps.setNull(12, java.sql.Types.INTEGER);
            }

            // o NOME do Enum (ex: "MAGE")
            ps.setString(13, user.getRpgClass().name());

            // Where
            ps.setString(14, user.getUuid().toString());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            getLogger().severe("Erro ao salvar jogador: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void addPermission(UUID uuid, String permission) {
        String sql = "INSERT IGNORE INTO jogadores_permissoes (uuid, permissao) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, permission.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro ao adicionar permissão SQL: " + e.getMessage());
        }
    }

    @Override
    public void removePermission(UUID uuid, String permission) {
        String sql = "DELETE FROM jogadores_permissoes WHERE uuid = ? AND permissao = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, permission.toLowerCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            getLogger().severe("Erro ao remover permissão SQL: " + e.getMessage());
        }
    }
}