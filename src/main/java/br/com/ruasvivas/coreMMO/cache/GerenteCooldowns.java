package br.com.ruasvivas.coreMMO.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GerenteCooldowns {

    // O Mapa Aninhado: Jogador -> (Habilidade -> Tempo Final)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Adiciona um tempo de espera (em segundos)
    public void adicionarCooldown(UUID uuid, String habilidade, int segundos) {
        // Calcula o tempo futuro: Agora + Segundos * 1000
        long fim = System.currentTimeMillis() + (segundos * 1000L);

        // Se o jogador não tem mapa ainda, cria um e adiciona o tempo
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(habilidade, fim);
    }

    // Verifica se ainda está esperando
    public boolean emCooldown(UUID uuid, String habilidade) {
        if (!cooldowns.containsKey(uuid)) return false;

        Map<String, Long> tempos = cooldowns.get(uuid);
        if (!tempos.containsKey(habilidade)) return false;

        // Retorna TRUE se AGORA for menor que o FIM
        return System.currentTimeMillis() < tempos.get(habilidade);
    }

    // Utilitário para mostrar "Faltam X segundos"
    public double getSegundosRestantes(UUID uuid, String habilidade) {
        if (!emCooldown(uuid, habilidade)) return 0.0;

        long fim = cooldowns.get(uuid).get(habilidade);
        long falta = fim - System.currentTimeMillis();

        // Converte ms para segundos com precisão decimal
        return falta / 1000.0;
    }
}