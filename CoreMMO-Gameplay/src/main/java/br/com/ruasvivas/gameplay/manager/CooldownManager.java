package br.com.ruasvivas.gameplay.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    // Jogador -> (Habilidade -> Timestamp de Fim)
    // ConcurrentHashMap para evitar problemas se o player usar skill e levar dano ao mesmo tempo
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void addCooldown(UUID uuid, String abilityName, int seconds) {
        long endTime = System.currentTimeMillis() + (seconds * 1000L);

        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(abilityName, endTime);
    }

    public boolean isOnCooldown(UUID uuid, String abilityName) {
        if (!cooldowns.containsKey(uuid)) return false;

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(abilityName)) return false;

        // Se AGORA for menor que o FIM, está em espera
        return System.currentTimeMillis() < playerCooldowns.get(abilityName);
    }

    public double getRemainingSeconds(UUID uuid, String abilityName) {
        if (!isOnCooldown(uuid, abilityName)) return 0.0;

        long end = cooldowns.get(uuid).get(abilityName);
        long left = end - System.currentTimeMillis();

        return Math.max(0, left / 1000.0); //
    }
}