package br.com.ruasvivas.infra.database;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.database.IDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class HikariDatabaseProvider implements IDatabase {

    private final HikariDataSource dataSource;

    private Logger getLogger() {
        return CoreRegistry.getSafe(Logger.class).orElse(Logger.getLogger("CoreMMO-Infra"));
    }

    // Recebemos as credenciais no construtor para não depender de arquivos de config aqui dentro
    public HikariDatabaseProvider(String host, String port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();

        // Força o uso do driver do MariaDB. Isso resolve o "No suitable driver".
        config.setDriverClassName("org.mariadb.jdbc.Driver");

        // Configuração da URL JDBC para MariaDB
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database);
        config.setUsername(user);
        config.setPassword(password);

        // Otimizações de Performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Configurações do Pool
        config.setMaximumPoolSize(10); // Ajuste conforme necessidade
        config.setMinimumIdle(2);
        config.setPoolName("CoreMMO-HikariPool");
        config.setConnectionTimeout(30000); // 30 segundos
        config.setLeakDetectionThreshold(60000); // Detectar leaks após 60s

        this.dataSource = new HikariDataSource(config);
        getLogger().info("Pool de conexões HikariCP iniciado com sucesso.");
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("O Pool de conexões está fechado ou nulo.");
        }
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            getLogger().info("Pool de conexões encerrado.");
        }
    }
}