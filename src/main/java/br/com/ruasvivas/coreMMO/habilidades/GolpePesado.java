package br.com.ruasvivas.coreMMO.habilidades;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class GolpePesado implements Habilidade {
    @Override
    public String getNome() {
        return "Golpe Pesado";
    }

    @Override
    public int getCooldownSegundos() {
        return 8;
    }

    @Override
    public double getCustoMana() {
        return 10.0;
    }

    @Override
    public boolean usar(Player player) {
        player.getWorld().spawnParticle(Particle.CRIT, player.getLocation(), 20, 1, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1f, 0.5f);

        boolean acertou = false;
        // Dano em área de 4 blocos
        for (Entity e : player.getNearbyEntities(4, 2, 4)) {
            if (e instanceof LivingEntity vitima && e != player) {
                vitima.damage(8.0, player);
                // Empurrão
                Vector dir = vitima.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                vitima.setVelocity(dir.multiply(1.2).setY(0.4));
                acertou = true;
            }
        }

        if (acertou) {
            player.sendMessage(Component.text("⚔️ SMASH!").color(NamedTextColor.GOLD));
        } else {
            player.sendMessage(Component.text("Você golpeou o ar...").color(NamedTextColor.GRAY));
        }
        return true;
    }
}