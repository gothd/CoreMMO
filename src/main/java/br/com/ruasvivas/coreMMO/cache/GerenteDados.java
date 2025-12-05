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

    public void atualizarBarra(Player jogador, DadosJogador dados) {
        // 1. Obtendo Vida Máxima de forma segura (API 1.21)
        double vidaMax = 20.0;
        var atributo = jogador.getAttribute(Attribute.MAX_HEALTH);
        if (atributo != null) vidaMax = atributo.getValue();

        // 2. Formatando números (sem casas decimais)
        String textoVida = String.format("%.0f/%.0f", jogador.getHealth(), vidaMax);
        String textoMana = String.format("%.0f/%.0f", dados.getMana(), dados.getMaxMana());

        // 3. Enviando a Action Bar
        jogador.sendActionBar(
                Component.text("❤ " + textoVida).color(NamedTextColor.RED)
                        .append(Component.text("   "))
                        .append(Component.text("💧 " + textoMana).color(NamedTextColor.AQUA))
        );
    }
}