package br.com.ruasvivas.coreMMO.habilidades;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
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

public class TiroPreciso implements Habilidade {

    private final Random random = new Random();

    @Override
    public String getNome() {
        return "Tiro Preciso";
    }

    @Override
    public int getCooldownSegundos() {
        return 6;
    }

    @Override
    public double getCustoMana() {
        return 15.0;
    }

    @Override
    public boolean usar(Player player) {
        // 1. Efeitos Visuais (Som de disparo de arco)
        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1f, 2f);

        // 2. RayTrace: Traça uma linha de 50 blocos na direção do olhar
        RayTraceResult resultado = player.getWorld().rayTraceEntities(player.getEyeLocation(), // Origem
                player.getEyeLocation().getDirection(), // Direção
                50,                                     // Alcance
                0.5,                                    // Tamanho da Hitbox extra
                e -> e != player && e instanceof LivingEntity // Ignora a si mesmo
        );

        // 3. Desenha o rastro da flecha (Partículas)
        tracarLinha(player.getEyeLocation(), resultado != null ?
                resultado.getHitPosition().toLocation(player.getWorld()) :
                player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(50)));

        if (resultado == null || !(resultado.getHitEntity() instanceof LivingEntity alvo)) {
            player.sendMessage(Component.text("Errou!").color(NamedTextColor.GRAY));
            return true; // Gasta mana mesmo errando
        }

        // 4. Cálculo de Dano Baseado no Nível
        DadosJogador dados = CoreMMO.getPlugin(CoreMMO.class).getGerenteDados().getDados(player.getUniqueId());
        double danoBase = 6.0;
        double bonusNivel = dados.getNivel() * 0.5; // Ex: Nível 10 = +5 Dano
        double danoFinal = danoBase + bonusNivel;

        // 5. Lógica de Headshot (Geometria)
        // Se o ponto de impacto (Y) for maior que a altura dos olhos do inimigo - 0.3
        double alturaImpacto = resultado.getHitPosition().getY();
        double alturaOlhosAlvo = alvo.getEyeLocation().getY();

        boolean isHeadshot = alturaImpacto >= (alturaOlhosAlvo - 0.3);
        boolean isCritico = isHeadshot; // Se for na cabeça, é 100% crit

        // Se não for Headshot, tenta a sorte (20%)
        if (!isHeadshot) {
            if (random.nextInt(100) < 20) {
                isCritico = true;
            }
        }

        // Aplica Multiplicador de Crítico (2x)
        if (isCritico) {
            danoFinal *= 2.0;
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);

            if (isHeadshot) {
                player.sendMessage(Component.text("HEADSHOT! " + (int) danoFinal + " de dano!")
                        .color(NamedTextColor.GOLD));
            } else {
                player.sendMessage(Component.text("Crítico! " + (int) danoFinal + " de dano!")
                        .color(NamedTextColor.YELLOW));
            }
        } else {
            player.sendMessage(Component.text("Acertou! " + (int) danoFinal + " de dano.")
                    .color(NamedTextColor.GREEN));
        }

        // Aplica o dano e empurrão leve
        alvo.damage(danoFinal, player);
        alvo.setVelocity(player.getLocation().getDirection().multiply(0.5));

        return true;
    }

    // Método auxiliar para desenhar o rastro
    private void tracarLinha(Location origem, Location destino) {
        Vector vetor = destino.toVector().subtract(origem.toVector());
        double distancia = vetor.length();
        vetor.normalize().multiply(0.5); // Espaçamento de 0.5 blocos

        for (double i = 0; i < distancia; i += 0.5) {
            origem.add(vetor);
            origem.getWorld().spawnParticle(Particle.CRIT, origem, 1, 0, 0, 0, 0);
        }
    }
}