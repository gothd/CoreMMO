package br.com.ruasvivas.coreMMO.cache;

import br.com.ruasvivas.coreMMO.model.DadosJogador;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class GerenteDados {

    // O nosso "Cofre" na memória RAM (Cache)
    private final Map<UUID, DadosJogador> cache = new HashMap<>();
    // Mapa que guarda o Timestamp (ms) de até quando a barra está "ocupada"
    private final Map<UUID, Long> bloqueioActionBar = new HashMap<>();

    // Guarda o jogador no cache (Login)
    public void adicionarJogador(DadosJogador dados) {
        cache.put(dados.getUuid(), dados);
    }

    // Remove o jogador do cache (Logout)
    public void removerJogador(UUID uuid) {
        cache.remove(uuid);
    }

    // Busca rápida para usar em comandos e eventos
    public DadosJogador getDados(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Nível 1 (Baixa Prioridade): Atualização de Rotina (Task/Regeneração).
     * Só exibe se não houver um aviso importante na tela.
     */
    public void atualizarBarra(Player player) {
        // Verifica se existe um bloqueio ativo
        if (bloqueioActionBar.containsKey(player.getUniqueId())) {
            long fimBloqueio = bloqueioActionBar.get(player.getUniqueId());
            if (System.currentTimeMillis() < fimBloqueio) {
                return; // O aviso tem prioridade, não faz nada.
            }
        }

        enviarStatus(player);
    }

    /**
     * Nível 2 (Média Prioridade): Avisos (Cooldown, Sem Mana).
     * Exibe a mensagem e bloqueia a atualização de rotina por 2 segundos.
     */
    public void enviarAviso(Player player, Component mensagem) {
        player.sendActionBar(mensagem);
        // Define o bloqueio para Daqui a 2000ms (2 segundos)
        bloqueioActionBar.put(player.getUniqueId(), System.currentTimeMillis() + 2000);
    }

    /**
     * Nível 3 (Alta Prioridade): Dano ou Cura Crítica.
     * Remove qualquer bloqueio e força a exibição imediata do status atualizado.
     */
    public void forcarAtualizacao(Player player) {
        bloqueioActionBar.remove(player.getUniqueId());
        enviarStatus(player);
    }

    // Método privado auxiliar para montar o texto
    private void enviarStatus(Player player) {
        DadosJogador dados = getDados(player.getUniqueId());
        if (dados == null) return;

        // 1. Obtendo Vida Máxima de forma segura (API 1.21)
        double vidaMax = 20.0;
        var atributo = player.getAttribute(Attribute.MAX_HEALTH);
        if (atributo != null) vidaMax = atributo.getValue();

        // 2. Formatando números (sem casas decimais)
        String textoVida = String.format("%.0f/%.0f", player.getHealth(), vidaMax);
        String textoMana = String.format("%.0f/%.0f", dados.getMana(), dados.getMaxMana());

        // 3. Enviando a Action Bar
        Component barra = Component.text()
                .append(Component.text("❤ " + textoVida).color(NamedTextColor.RED))
                .append(Component.text("   "))
                .append(Component.text("💧 " + textoMana).color(NamedTextColor.AQUA))
                .build();
        player.sendActionBar(barra);
    }
}