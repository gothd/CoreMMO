package br.com.ruasvivas.coreMMO.tasks;

import br.com.ruasvivas.coreMMO.CoreMMO;
import br.com.ruasvivas.coreMMO.model.DadosJogador;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegeneracaoTask extends BukkitRunnable {

    private final CoreMMO plugin;

    public RegeneracaoTask(CoreMMO plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player jogador : Bukkit.getOnlinePlayers()) {
            DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());
            if (dados == null) continue;

            // 1. Lógica: Baseada na Classe
            double regenBase = 5.0;
            double multiplicadorClasse = dados.getClasse().getRegeneracaoMana();

            double regeneracaoTotal = regenBase * multiplicadorClasse;

            if (dados.getMana() < dados.getMaxMana()) {
                dados.setMana(Math.min(dados.getMana() + regeneracaoTotal, dados.getMaxMana()));

                // 2. Visual: Atualiza a barra
                plugin.getGerenteDados().atualizarBarra(jogador, dados);
            }
        }
    }
}