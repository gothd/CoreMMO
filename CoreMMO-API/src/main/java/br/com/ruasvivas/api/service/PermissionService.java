package br.com.ruasvivas.api.service;

import java.util.UUID;

public interface PermissionService {

    /**
     * Adiciona uma permissão permanentemente a um jogador (guarda na base de dados e aplica online).
     *
     * @param uuid       UUID do jogador.
     * @param permission Node da permissão (ex: "coremmo.admin").
     */
    void addPermission(UUID uuid, String permission);

    /**
     * Remove uma permissão permanentemente de um jogador.
     *
     * @param uuid       UUID do jogador.
     * @param permission Node da permissão a ser removida.
     */
    void removePermission(UUID uuid, String permission);

}