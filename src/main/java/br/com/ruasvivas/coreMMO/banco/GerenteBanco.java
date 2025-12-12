package br.com.ruasvivas.coreMMO.banco;

import br.com.ruasvivas.coreMMO.CoreMMO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class GerenteBanco {

    private final CoreMMO plugin;
    private HikariDataSource dataSource;

    public GerenteBanco(CoreMMO plugin) {
        this.plugin = plugin;
    }

    public void abrirConexao() {
        // 1. Lendo configuração
        String host = plugin.getConfig().getString("database.host");
        int port = plugin.getConfig().getInt("database.port");
        String db = plugin.getConfig().getString("database.name");
        String user = plugin.getConfig().getString("database.user");
        String pass = plugin.getConfig().getString("database.password");
        int poolSize = plugin.getConfig().getInt("database.pool-size");

        // 2. Configurando o Hikari
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + db);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaximumPoolSize(poolSize);
        config.setPoolName("CoreMMO-Pool");

        // CORREÇÃO: Forçamos o driver para evitar erros de "No suitable driver"
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        // 3. Iniciando a piscina
        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Conexão com Banco de Dados iniciada! 🐬");

        // 4. Cria a tabela se não existir (Automação)
        inicializarTabela();

        // 5. Atualiza a tabela (Self-Healing)
        atualizarTabela();
    }

    public void fecharConexao() {
        if (dataSource != null) dataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void inicializarTabela() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS jogadores (
                        uuid VARCHAR(36) NOT NULL,
                        username VARCHAR(16) NOT NULL,
                        nivel INT DEFAULT 1,
                        experiencia BIGINT DEFAULT 0,
                        moedas BIGINT DEFAULT 0,
                        loc_mundo VARCHAR(50),
                        loc_x DOUBLE, loc_y DOUBLE, loc_z DOUBLE,
                        loc_yaw FLOAT, loc_pitch FLOAT,
                        ultimo_login DATETIME DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (uuid)
                    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
                """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // Log profissional em vez de printStackTrace
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar tabela 'jogadores'!", e);
        }
    }

    private void atualizarTabela() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // 1. Criar Tabela de Guildas (A Entidade Pai)
            stmt.executeUpdate("""
                        CREATE TABLE IF NOT EXISTS guildas (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            nome VARCHAR(32) NOT NULL UNIQUE,
                            tag VARCHAR(6) NOT NULL UNIQUE,
                            descricao TEXT,
                            lider_uuid VARCHAR(36) NOT NULL,
                            nivel INT DEFAULT 1,
                            experiencia BIGINT DEFAULT 0,
                            membros_max INT DEFAULT 10,
                            data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
                            INDEX (lider_uuid)
                        ) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
                    """);

            // 2. Colunas nos Jogadores (A Entidade Filho)
            String[] updates = {
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS mana DOUBLE DEFAULT 100",
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS mana_max DOUBLE DEFAULT 100",
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS " +
                            "guilda_id INT",
                    // Estatísticas para KDR (Kill/Death Ratio)
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS " +
                            "kills_pve INT DEFAULT 0",
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS " +
                            "kills_pvp INT DEFAULT 0",
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS " +
                            "mortes INT DEFAULT 0",
                    // Classe
                    "ALTER TABLE jogadores ADD COLUMN IF NOT EXISTS classe VARCHAR(16) DEFAULT 'NOVATO'"
            };

            for (String sql : updates) {
                stmt.executeUpdate(sql);
            }
            // 3. O Casamento (Vínculo das Tabelas)
            // Só tentamos criar a regra se ela ainda não existir
            if (!existeConstraint(conn, "fk_jogador_guilda")) {
                stmt.executeUpdate(
                        "ALTER TABLE jogadores ADD CONSTRAINT fk_jogador_guilda " +
                                "FOREIGN KEY (guilda_id) REFERENCES guildas(id) " +
                                "ON DELETE SET NULL"
                );
                plugin.getLogger().info("Sistema de Guildas vinculado!");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro na migração!", e);
        }
    }

    // Método auxiliar para verificar se uma regra já existe no banco
    private boolean existeConstraint(Connection conn, String nome) {
        String sql = "SELECT * FROM information_schema.TABLE_CONSTRAINTS " +
                "WHERE CONSTRAINT_SCHEMA = DATABASE() " +
                "AND CONSTRAINT_NAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }
}