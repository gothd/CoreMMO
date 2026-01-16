package br.com.ruasvivas.gameplay.skill;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.skill.Skill;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class PrecisionShotSkill implements Skill {

    private final Random random = new Random();

    @Override
    public String getName() {
        return "Precision Shot"; // Nome interno/sistema
    }

    @Override
    public double getManaCost() {
        return 15.0;
    }

    @Override
    public int getCooldownSeconds() {
        return 6;
    }

    @Override
    public boolean cast(Player player) {
        // Efeitos Visuais (Som de Besta é mais "seco" e mecânico)
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1f, 1f); //

        // RayTrace (Mecânica de Sniper - HitScan)
        // Traça uma linha de 50 blocos na direção do olhar instantaneamente
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                50,
                0.5, // Hitbox um pouco mais generosa
                e -> e != player && e instanceof LivingEntity
        );

        // Desenha o rastro da flecha (Tracer Bullet)
        Location targetLoc = (result != null) ?
                result.getHitPosition().toLocation(player.getWorld()) :
                player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(50));

        spawnTracerEffect(player.getEyeLocation(), targetLoc);

        if (result == null || !(result.getHitEntity() instanceof LivingEntity target)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, 0.5f, 0.5f); // Som de "errar/bater na parede"
            player.sendMessage(Component.text("Errou!").color(NamedTextColor.GRAY));
            return true; // Gasta mana
        }

        // Cálculo de Dano (Depende do Nível do User)
        // É preciso acessar o CacheManager para saber o nível do atirador
        double finalDamage = calculateDamage(player);

        // Lógica de Headshot (Geometria)
        // Se acertou perto da altura dos olhos (Topo da hitbox)
        double hitY = result.getHitPosition().getY();
        double eyeY = target.getEyeLocation().getY();
        boolean isHeadshot = hitY >= (eyeY - 0.3); //

        boolean isCrit = isHeadshot; // Headshot garante crítico

        // Sorte (20% base se não for HS)
        if (!isHeadshot && random.nextInt(100) < 20) {
            isCrit = true;
        }

        // Aplica Multiplicadores
        if (isCrit) {
            finalDamage *= 2.0;
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);

            if (isHeadshot) {
                player.sendMessage(Component.text("HEADSHOT! " + (int) finalDamage + " de dano!")
                        .color(NamedTextColor.GOLD));
            } else {
                player.sendMessage(Component.text("Crítico! " + (int) finalDamage + " de dano!")
                        .color(NamedTextColor.YELLOW));
            }
        } else {
            player.sendMessage(Component.text("Acertou! " + (int) finalDamage + " de dano.")
                    .color(NamedTextColor.GREEN));
        }

        // Aplica dano e knockback leve
        target.damage(finalDamage, player);
        target.setVelocity(player.getLocation().getDirection().multiply(0.5));

        return true;
    }

    private double calculateDamage(Player player) {
        // Recupera o User do cache global
        CacheManager cache = CoreRegistry.get(CacheManager.class);

        User user = cache.getUser(player);
        if (user != null) {
            double baseDamage = 6.0;
            double levelBonus = user.getLevel() * 0.5;
            return baseDamage + levelBonus;
        }
        return 6.0; // Fallback
    }

    private void spawnTracerEffect(Location start, Location end) {
        Vector vector = end.toVector().subtract(start.toVector());
        double distance = vector.length();
        vector.normalize().multiply(0.5); // Espaçamento de 0.5 blocos

        Location current = start.clone();
        for (double i = 0; i < distance; i += 0.5) {
            current.add(vector);
            // Partícula CRIT é boa para rastro de sniper
            current.getWorld().spawnParticle(Particle.CRIT, current, 1, 0, 0, 0, 0); //
        }
    }
}