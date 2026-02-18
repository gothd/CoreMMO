package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.service.CacheService;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.common.util.GameConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager implements CacheService {

    // Mapa thread-safe para guardar os usuários online
    private final Map<UUID, User> userCache = new ConcurrentHashMap<>();

    // Controle de bloqueio da Action Bar (Nível 2 e 3 de prioridade)
    private final Map<UUID, Long> actionBarLock = new ConcurrentHashMap<>();

    // Controle de carregamento assíncrono
    private final Set<UUID> loadingUsers = ConcurrentHashMap.newKeySet();

    // --- Gestão de Cache ---

    public void cacheUser(User user) {
        userCache.put(user.getUuid(), user);
        setLoading(user.getUuid(), false); // Terminou de carregar
    }

    public void removeUser(UUID uuid) {
        userCache.remove(uuid);
        actionBarLock.remove(uuid);
        loadingUsers.remove(uuid);
    }

    @Override
    public User getUser(UUID uuid) {
        return userCache.get(uuid);
    }

    // Método auxiliar para pegar via Player do Bukkit
    @Override
    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public void setLoading(UUID uuid, boolean isLoading) {
        if (isLoading) loadingUsers.add(uuid);
        else loadingUsers.remove(uuid);
    }

    public boolean isLoading(UUID uuid) {
        return loadingUsers.contains(uuid);
    }

    // --- Sistema de Action Bar (Visual) ---

    /**
     * Nível 1: Atualização de Rotina.
     * Ignora se houver um aviso importante na tela.
     */
    public void updateActionBar(Player player) {
        if (isLocked(player.getUniqueId())) return;
        sendUserStatus(player);
    }

    /**
     * Nível 2: Avisos Importantes (Cooldown, Mana insuficiente).
     * Bloqueia a barra por 2 segundos.
     */
    public void sendWarning(Player player, Component message) {
        player.sendActionBar(message);
        // Bloqueia atualizações de rotina por 2000ms
        actionBarLock.put(player.getUniqueId(), System.currentTimeMillis() + 2000);
    }

    /**
     * Envia um aviso com tempo de duração personalizado.
     * Útil para cooldowns curtos (ex: 0.5s) para não travar a tela por muito tempo.
     */
    public void sendTimedWarning(Player player, Component message, long millis) {
        player.sendActionBar(message);
        // Define o bloqueio exatamente pelo tempo solicitado
        actionBarLock.put(player.getUniqueId(), System.currentTimeMillis() + millis);
    }

    /**
     * Nível 3: Crítico (Dano/Cura).
     * Força a atualização imediata.
     */
    public void forceUpdate(Player player) {
        actionBarLock.remove(player.getUniqueId());
        sendUserStatus(player);
    }

    private boolean isLocked(UUID uuid) {
        if (actionBarLock.containsKey(uuid)) {
            long end = actionBarLock.get(uuid);
            // Se o tempo atual for menor que o bloqueio, ainda está bloqueado
            return System.currentTimeMillis() < end;
        }
        return false;
    }

    /**
     * Remove qualquer bloqueio ativo da Action Bar.
     * Permite que o próximo update (Regen ou Skill) desenhe livremente.
     */
    public void unlockActionBar(Player player) {
        actionBarLock.remove(player.getUniqueId());
    }

    private void sendUserStatus(Player player) {
        User user = getUser(player.getUniqueId());
        if (user == null) return;

        // Recupera vida máxima (Paper API)
        double maxHealth = GameConstants.BASE_PLAYER_HEALTH;
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) maxHealth = attr.getValue();

        // Formatação visual
        String hpText = String.format("%.0f/%.0f", player.getHealth(), maxHealth);
        String manaText = String.format("%.0f/%.0f", user.getMana(), user.getMaxMana()); //

        Component bar = Component.text()
                .append(Component.text("❤ " + hpText).color(NamedTextColor.RED))
                .append(Component.text("   "))
                .append(Component.text("💧 " + manaText).color(NamedTextColor.AQUA))
                .build();

        player.sendActionBar(bar);
    }
}