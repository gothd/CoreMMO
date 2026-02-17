package br.com.ruasvivas.infra.database;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.database.IDatabase;
import br.com.ruasvivas.api.database.ITableManager;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MariaDBTableManager implements ITableManager {

    @Override
    public void createTables() {
        // Recupera serviços do Registry
        // Se o Logger não estiver registrado, usamos um Logger global genérico para não quebrar
        Logger logger = CoreRegistry.getSafe(Logger.class).orElse(Logger.getLogger("CoreMMO-Infra"));

        // Recupera o banco de dados (Obrigatório)
        IDatabase db;
        try {
            db = CoreRegistry.get(IDatabase.class);
        } catch (IllegalStateException e) {
            logger.severe("[Infra] Erro: Banco de dados não registrado antes da criação de tabelas.");
            return;
        }

        // Definição dos SQLs
        // Tabela de Guildas (Pai)
        String sqlGuilds = """
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
                """;

        // Tabela de Jogadores (Filho)
        String sqlUsers = """
                    CREATE TABLE IF NOT EXISTS jogadores (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                        username VARCHAR(16) NOT NULL,
                        classe VARCHAR(16) DEFAULT 'NOVATO',
                        nivel INT DEFAULT 1,
                        experiencia BIGINT DEFAULT 0,
                        moedas BIGINT DEFAULT 0,
                        mana DOUBLE DEFAULT 100,
                        mana_max DOUBLE DEFAULT 100,
                
                        guilda_id INT,
                
                        kills_pve INT DEFAULT 0,
                        kills_pvp INT DEFAULT 0,
                        mortes INT DEFAULT 0,
                
                        loc_mundo VARCHAR(50),
                        loc_x DOUBLE, loc_y DOUBLE, loc_z DOUBLE,
                        loc_yaw FLOAT, loc_pitch FLOAT,
                
                        ultimo_login DATETIME DEFAULT CURRENT_TIMESTAMP,
                
                        CONSTRAINT fk_jogador_guilda FOREIGN KEY (guilda_id)\s
                        REFERENCES guildas(id) ON DELETE SET NULL
                    ) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
                """;

        // Tabela de Permissões (1:N com Jogadores)
        String sqlPerms = """
            CREATE TABLE IF NOT EXISTS jogadores_permissoes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                uuid VARCHAR(36) NOT NULL,
                permissao VARCHAR(64) NOT NULL,
                data_adicao DATETIME DEFAULT CURRENT_TIMESTAMP,
                INDEX (uuid),
                UNIQUE KEY unique_perm (uuid, permissao),
                CONSTRAINT fk_perm_jogador FOREIGN KEY (uuid)\s
                REFERENCES jogadores(uuid) ON DELETE CASCADE
            ) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
       \s""";

        // Execução
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {

            // Adiciona as queries ao lote (batch) para execução otimizada
            stmt.addBatch(sqlGuilds);
            stmt.addBatch(sqlUsers);
            stmt.addBatch(sqlPerms);

            stmt.executeBatch();

            logger.info("Tabelas de banco de dados verificadas/sincronizadas.");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "FALHA CRÍTICA ao criar tabelas do banco de dados!", e);
        }
    }
}