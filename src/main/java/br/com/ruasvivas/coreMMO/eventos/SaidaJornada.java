package br.com.ruasvivas.coreMMO.eventos;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SaidaJornada implements Listener {

    private final CoreMMO plugin;

    public SaidaJornada(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void aoSair(PlayerQuitEvent evento) {
        Player jogador = evento.getPlayer();
        UUID uuid = jogador.getUniqueId();

        // 1. Pega do Cache (RAM)
        DadosJogador dados = plugin.getGerenteDados().getDados(uuid);

        // 2. Limpa o Cache imediatamente (libera memória)
        plugin.getGerenteDados().removerJogador(uuid);

        if (dados != null) {
            // 3. Atualiza localização final antes de salvar
            dados.setLocalizacao(jogador.getLocation());

            // 4. Salva no Banco (Assíncrono)
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean sucesso = plugin.getJogadorDAO().salvarJogador(dados);

                if (sucesso) {
                    plugin.getLogger().info("Dados salvos: " + jogador.getName());
                } else {
                    plugin.getLogger().warning("Erro ao salvar: " + jogador.getName());
                }
            });
        }
    }
}