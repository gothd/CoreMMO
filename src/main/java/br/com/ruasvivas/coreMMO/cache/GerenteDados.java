package br.com.ruasvivas.coreMMO.cache;

import br.com.ruasvivas.coreMMO.model.DadosJogador;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
}