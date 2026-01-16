package br.com.ruasvivas.gameplay.skill;

import br.com.ruasvivas.api.skill.Skill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

public class FireballSkill implements Skill {
    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public double getManaCost() {
        return 20.0;
    }

    @Override
    public int getCooldownSeconds() {
        return 5;
    }

    @Override
    public boolean cast(Player player) {
        player.launchProjectile(Fireball.class);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1f, 1f);
        player.sendMessage(Component.text("🔥 Incinerar!").color(NamedTextColor.RED));
        return true;
    }
}
