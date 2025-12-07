package br.com.ruasvivas.coreMMO.dao;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.banco.GerenteBanco;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.sql.Types;

public class JogadorDAO {

    private final CoreMMO plugin;
    private final GerenteBanco gerenteBanco;

    public JogadorDAO(CoreMMO plugin, GerenteBanco gerenteBanco) {
        this.plugin = plugin;
        this.gerenteBanco = gerenteBanco;
    }

    public void criarJogador(Player player) {
        String sql = "INSERT IGNORE INTO jogadores (uuid, username) VALUES (?, ?)";

        try (Connection conn = gerenteBanco.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.executeUpdate();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar jogador!", e);
        }
    }

    public DadosJogador carregarJogador(UUID uuid) {
        String sql = "SELECT * FROM jogadores WHERE uuid = ?";

        try (Connection conn = gerenteBanco.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DadosJogador dados = new DadosJogador(uuid, rs.getString("username"));

                // Preenchendo Progresso
                dados.setNivel(rs.getInt("nivel"));
                dados.setExperiencia(rs.getLong("experiencia"));
                dados.setMoedas(rs.getLong("moedas"));

                // Preenchendo a Mana
                dados.setMana(rs.getDouble("mana"));
                dados.setMaxMana(rs.getDouble("mana_max"));

                // Preenchendo a Guilda
                dados.setGuildaId(rs.getInt("guilda_id"));

                // Preenchendo Localização
                dados.setLocalizacao(rs.getString("loc_mundo"), rs.getDouble("loc_x"), rs.getDouble("loc_y"),
                        rs.getDouble("loc_z"), rs.getFloat("loc_yaw"), rs.getFloat("loc_pitch"));

                return dados;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao carregar jogador!", e);
        }
        return null;
    }

    public boolean salvarJogador(DadosJogador dados) {
        String sql = """
                    UPDATE jogadores SET
                    nivel=?, experiencia=?, moedas=?,
                    mana=?, mana_max=?,
                    loc_mundo=?, loc_x=?, loc_y=?, loc_z=?, loc_yaw=?, loc_pitch=?,
                    guilda_id=?,
                    ultimo_login=CURRENT_TIMESTAMP
                    WHERE uuid=?
                """;

        try (Connection conn = gerenteBanco.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // Progresso
            ps.setInt(1, dados.getNivel());
            ps.setLong(2, dados.getExperiencia());
            ps.setLong(3, dados.getMoedas());

            // Mana
            ps.setDouble(4, dados.getMana());
            ps.setDouble(5, dados.getMaxMana());

            // Localização
            ps.setString(6, dados.getMundo());
            ps.setDouble(7, dados.getX());
            ps.setDouble(8, dados.getY());
            ps.setDouble(9, dados.getZ());
            ps.setFloat(10, dados.getYaw());
            ps.setFloat(11, dados.getPitch());

            // Guilda
            if (dados.getGuildaId() > 0) {
                ps.setInt(12, dados.getGuildaId());
            } else {
                // Se for 0, mandamos NULL explicitamente
                ps.setNull(12, java.sql.Types.INTEGER);
            }

            // Where
            ps.setString(13, dados.getUuid().toString());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao salvar jogador!", e);
            return false;
        }
    }
}