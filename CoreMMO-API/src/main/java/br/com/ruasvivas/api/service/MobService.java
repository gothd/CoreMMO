package br.com.ruasvivas.api.service;

import org.bukkit.entity.LivingEntity;

public interface MobService {

    /**
     * Retorna o nível RPG do monstro.
     * @param entity A entidade viva.
     * @return O nível (padrão 1 se não tiver nível definido).
     */
    int getMobLevel(LivingEntity entity);
}