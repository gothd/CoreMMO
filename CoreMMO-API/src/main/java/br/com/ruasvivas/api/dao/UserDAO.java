package br.com.ruasvivas.api.dao;

import br.com.ruasvivas.common.model.User;

import java.util.UUID;

public interface UserDAO {

    // Cria o registro básico no banco
    void createUser(UUID uuid, String username);

    // Carrega todos os dados (Stats, Loc, Guilda)
    User loadUser(UUID uuid);

    // Salva o estado atual
    boolean saveUser(User user);

    void addPermission(UUID uuid, String permission);

    void removePermission(UUID uuid, String permission);
}