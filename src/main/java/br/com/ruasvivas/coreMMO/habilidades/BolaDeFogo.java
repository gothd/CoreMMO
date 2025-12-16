package br.com.ruasvivas.coreMMO.habilidades;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

public class BolaDeFogo implements Habilidade {
    @Override
    public String getNome() {
        return "Bola de Fogo";
    }

    @Override
    public int getCooldownSegundos() {
        return 5;
    }

    @Override
    public double getCustoMana() {
        return 20.0;
    }

    @Override
    public boolean usar(Player player) {
        player.launchProjectile(Fireball.class);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        player.sendMessage(Component.text("🔥 Incinerar!").color(NamedTextColor.RED));
        return true;
    }
}