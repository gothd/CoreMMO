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
            // Bloco try-catch para evitar que um erro em um jogador mate a task para todos
            try {
                // Pega os dados da RAM (Rápido)
                DadosJogador dados = plugin.getGerenteDados().getDados(jogador.getUniqueId());
                if (dados == null) continue;

                // 1. Lógica de Regeneração (Matemática)
                // Só regenera se não estiver cheio
                if (dados.getMana() < dados.getMaxMana()) {
                    double regen = 5.0;

                    // Aplica bônus de classe se existir
                    if (dados.getClasse() != null) {
                        regen *= dados.getClasse().getRegeneracaoMana();
                    }

                    dados.setMana(Math.min(dados.getMana() + regen, dados.getMaxMana()));
                }

                // 2. Lógica Visual (Interface)
                // A atualização DEVE ocorrer sempre, mesmo se a mana estiver cheia,
                // para manter a barra fixa na tela.

                // Nível 1: Tenta atualizar, mas o GerenteDados bloqueará
                // se houver uma mensagem de erro importante sendo exibida.
                plugin.getGerenteDados().atualizarBarra(jogador);

            } catch (Exception e) {
                // Log discreto para não spammar o console, mas avisar do problema
                plugin.getLogger().warning("Erro na task de regeneração para " + jogador.getName() + ": " + e.getMessage());
            }
        }
    }
}