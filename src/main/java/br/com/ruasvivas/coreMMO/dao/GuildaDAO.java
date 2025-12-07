package br.com.ruasvivas.coreMMO.dao;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.banco.GerenteBanco;
import br.com.ruasvivas.coreMMO.model.Guilda;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class GuildaDAO {

    private final CoreMMO plugin;
    private final GerenteBanco gerenteBanco;

    public GuildaDAO(CoreMMO plugin, GerenteBanco gerenteBanco) {
        this.plugin = plugin;
        this.gerenteBanco = gerenteBanco;
    }

    // CRIAR: Retorna o ID gerado pelo banco
    public void criar(Guilda guilda) {
        String sql = "INSERT INTO guildas (nome, tag, lider_uuid) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = gerenteBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guilda.getNome());
            ps.setString(2, guilda.getTag());
            ps.setString(3, guilda.getLiderUuid().toString());
            ps.executeUpdate();

            // Recupera o ID (1, 2, 3...) que o banco criou
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    guilda.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar guilda!", e);
        }
    }

    // CARREGAR TUDO: Para inicializar o cache ao ligar o servidor
    public List<Guilda> carregarTodas() {
        List<Guilda> lista = new ArrayList<>();
        String sql = "SELECT * FROM guildas";

        try (Connection conn = gerenteBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Guilda guilda = new Guilda(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("tag"),
                        UUID.fromString(rs.getString("lider_uuid"))
                );
                lista.add(guilda);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao carregar todas as guildas!", e);
        }
        return lista;
    }

    // ESTATÍSTICAS: Calcula o KDR somando os membros
    public void carregarEstatisticas(Guilda guilda) {
        String sql = "SELECT COUNT(*) as membros, " +
                "SUM(kills_pve + kills_pvp) as kills, " +
                "SUM(mortes) as mortes " +
                "FROM jogadores WHERE guilda_id = ?";

        try (Connection conn = gerenteBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guilda.getId());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                guilda.setQuantidadeMembros(rs.getInt("membros"));
                guilda.setTotalKills(rs.getInt("kills"));
                guilda.setTotalMortes(rs.getInt("mortes"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao calcular KDR da guilda!", e);
        }
    }

    public void deletar(int id) {
        // Graças ao ON DELETE SET NULL, isso atualiza os jogadores auto
        String sql = "DELETE FROM guildas WHERE id = ?";
        try (Connection conn = gerenteBanco.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao deletar guilda!", e);
        }
    }
}