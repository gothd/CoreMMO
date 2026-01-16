package br.com.ruasvivas.api.skill;

import org.bukkit.entity.Player;

/**
 * Contrato para criação de habilidades.
 * Addons externos podem implementar isso para criar magias novas.
 */
public interface Skill {

    /**
     * O nome único da habilidade (ex: "Bola de Fogo").
     * Usado para registros e cooldowns.
     */
    String getName();

    /**
     * Custo de Mana para conjurar.
     */
    double getManaCost();

    /**
     * Tempo de recarga em segundos.
     */
    int getCooldownSeconds();

    /**
     * Executa a lógica da habilidade.
     * @param player O jogador que está conjurando.
     * @return true se a habilidade foi executada com sucesso (deve descontar mana).
     * false se falhou (ex: alvo inválido, bloqueado).
     */
    boolean cast(Player player);
}