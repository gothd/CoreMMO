package br.com.ruasvivas.coreMMO.habilidades;

import org.bukkit.entity.Player;

public interface Habilidade {
    String getNome();

    int getCooldownSegundos();

    double getCustoMana();

    boolean usar(Player player);
}