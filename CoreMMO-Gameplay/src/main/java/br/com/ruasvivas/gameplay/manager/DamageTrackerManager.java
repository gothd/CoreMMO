package br.com.ruasvivas.gameplay.manager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DamageTrackerManager {

    // Classe interna para guardar Dano + Timestamp
    private static class DamageSession {
        double totalDamage;
        long lastHitTimestamp;

        DamageSession(double damage) {
            this.totalDamage = damage;
            this.lastHitTimestamp = System.currentTimeMillis();
        }

        void addDamage(double damage) {
            this.totalDamage += damage;
            this.lastHitTimestamp = System.currentTimeMillis(); // Atualiza o relógio
        }
    }

    // Map<MobUUID, Map<PlayerUUID, Session>>
    private final Map<UUID, Map<UUID, DamageSession>> tracker = new ConcurrentHashMap<>();

    public void registerDamage(LivingEntity mob, Player damager, double damage) {
        tracker.computeIfAbsent(mob.getUniqueId(), k -> new ConcurrentHashMap<>())
                .compute(damager.getUniqueId(), (k, session) -> {
                    if (session == null) {
                        return new DamageSession(damage);
                    } else {
                        session.addDamage(damage);
                        return session;
                    }
                });
    }

    /**
     * Limpa jogadores que não bateram no mob nos últimos X segundos.
     * Deve ser chamado antes de calcular o loot.
     */
    public void removeExpiredAttackers(LivingEntity mob, int expirationSeconds) {
        Map<UUID, DamageSession> mobSessions = tracker.get(mob.getUniqueId());
        if (mobSessions == null) return;

        long expirationMillis = expirationSeconds * 1000L;
        long now = System.currentTimeMillis();

        // Remove quem está inativo há muito tempo
        mobSessions.entrySet().removeIf(entry -> (now - entry.getValue().lastHitTimestamp) > expirationMillis);

        // Se não sobrou ninguém, remove o mob do tracker pra economizar memória
        if (mobSessions.isEmpty()) {
            tracker.remove(mob.getUniqueId());
        }
    }

    /**
     * Retorna o mapa simples (UUID -> Dano) APENAS dos jogadores válidos.
     */
    public Map<UUID, Double> getDamageMap(LivingEntity mob) {
        Map<UUID, DamageSession> sessionMap = tracker.get(mob.getUniqueId());
        if (sessionMap == null) return new HashMap<>();

        // Converte Session -> Double para manter compatibilidade com o MobListener
        return sessionMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().totalDamage));
    }

    public void clearMob(LivingEntity mob) {
        tracker.remove(mob.getUniqueId());
    }

    /**
     * Retorna o jogador que causou mais dano.
     */
    public UUID getTopDamager(LivingEntity mob) {
        Map<UUID, Double> map = getDamageMap(mob);
        if (map.isEmpty()) return null;

        return map.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}