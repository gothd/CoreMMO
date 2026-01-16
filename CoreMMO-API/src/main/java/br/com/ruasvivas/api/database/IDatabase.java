package br.com.ruasvivas.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabase {
    /**
     * Obtém uma conexão ativa do Pool.
     * Deve ser fechada (try-with-resources) logo após o uso.
     */
    Connection getConnection() throws SQLException;

    /**
     * Encerra o Pool de conexões (usado ao desligar o servidor).
     */
    void close();
}