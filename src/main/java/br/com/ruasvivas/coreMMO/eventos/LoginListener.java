package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoginListener implements Listener {

    private final CoreMMO plugin;

    public LoginListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoEntrar(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Marca como carregando
        plugin.getGerenteDados().setCarregando(player.getUniqueId(), true);

        // 2. Aplica efeitos de "Limbo" (Infinitos até carregar)
        // BLINDNESS: Esconde o mundo carregando/glitchado
        // SLOW: Impede movimento (Stun)
        // RESISTANCE: Garante invulnerabilidade extra
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 99999, 5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 99999, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 99999, 255));

        player.sendActionBar(Component.text("Carregando personagem...").color(NamedTextColor.YELLOW));
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent event) {
        // Limpeza de memória
        plugin.getGerenteDados().setCarregando(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void aoSofrerDano(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Se estiver carregando, cancela qualquer dano (Queda, Mobs, Lava)
            if (plugin.getGerenteDados().estaCarregando(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    // Opcional: Bloquear movimentação brusca se o PotionEffect falhar
    @EventHandler
    public void aoMover(PlayerMoveEvent event) {
        if (plugin.getGerenteDados().estaCarregando(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }
}