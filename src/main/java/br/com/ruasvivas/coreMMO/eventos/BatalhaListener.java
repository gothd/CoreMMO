package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class BatalhaListener implements Listener {

    private final CoreMMO plugin;

    public BatalhaListener(CoreMMO plugin) {
        this.plugin = plugin;
    }

    // Use MONITOR para apenas ler o resultado final
    // (após armadura/eventos de cancelamento)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void aoTomarDano(EntityDamageEvent evento) {
        if (evento.getEntity() instanceof Player jogador) {

            // AGENDAMENTO: Espera 1 tick (50ms) para o Bukkit aplicar o dano
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {

                // Prioridade Máxima
                // Remove qualquer bloqueio de mensagem (ex: "Recarregando")
                // e mostra a Vida atualizada imediatamente.
                plugin.getGerenteDados().forcarAtualizacao(jogador);

            }, 1L);
        }
    }
}