package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.common.model.Guild;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.manager.GuildManager;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener {

    private final CacheManager cacheManager;
    private final GuildManager guildManager;

    private final int LOCAL_RADIUS = 50;
    private final double RADIUS_SQUARED = LOCAL_RADIUS * LOCAL_RADIUS; // Otimização

    public ChatListener(CacheManager cacheManager, GuildManager guildManager) {
        this.cacheManager = cacheManager;
        this.guildManager = guildManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String originalText = PlainTextComponentSerializer.plainText().serialize(event.message());

        boolean isGlobal = false;
        boolean nobodyHeard = false;

        // 1. Roteamento de Canais
        if (originalText.startsWith("!")) {
            isGlobal = true;
            String finalText = originalText.substring(1).trim();
            if (finalText.isEmpty()) {
                event.setCancelled(true);
                return;
            }
            event.message(Component.text(finalText));
        } else {
            // Lógica Local: Remove quem está longe
            event.viewers().removeIf(viewer -> {
                if (!(viewer instanceof Player recipient)) return false;
                if (recipient.getUniqueId().equals(player.getUniqueId())) return false; // Você sempre se ouve
                if (!recipient.getWorld().equals(player.getWorld())) return true; // Mundos diferentes

                return recipient.getLocation().distanceSquared(player.getLocation()) > RADIUS_SQUARED;
            });

            // Verifica se alguém ouviu (exceto o próprio jogador e console)
            boolean heardByOthers = event.viewers().stream()
                    .filter(v -> v instanceof Player)
                    .anyMatch(v -> !((Player) v).getUniqueId().equals(player.getUniqueId()));

            if (!heardByOthers) nobodyHeard = true;
        }

        // Variáveis finais para o lambda do Renderer
        final boolean finalGlobal = isGlobal;
        final boolean finalNobody = nobodyHeard;

        event.renderer((source, sourceDisplayName, message, viewer) ->
                formatMessage(source, message, finalGlobal, finalNobody)
        );
    }

    private Component formatMessage(Player player, Component message, boolean global, boolean nobodyHeard) {
        User user = cacheManager.getUser(player); // Pega do Cache atualizado

        NamedTextColor tagColor;
        NamedTextColor msgColor;
        String channelTag;

        if (global) {
            channelTag = "[G]";
            tagColor = NamedTextColor.DARK_GREEN;
            msgColor = NamedTextColor.WHITE;
        } else if (nobodyHeard) {
            channelTag = "[?]";
            tagColor = NamedTextColor.DARK_GRAY;
            msgColor = NamedTextColor.DARK_GRAY;
        } else {
            channelTag = "[L]";
            tagColor = NamedTextColor.YELLOW;
            msgColor = NamedTextColor.GRAY;
        }

        Component prefix = Component.text(channelTag + " ").color(tagColor);
        if (nobodyHeard) prefix = prefix.decorate(TextDecoration.ITALIC);

        if (user != null) {
            // Adiciona Nível
            prefix = prefix.append(Component.text("[" + user.getLevel() + "] ").color(NamedTextColor.YELLOW));

            // Adiciona Guilda
            if (user.hasGuild()) {
                Guild guild = guildManager.getById(user.getGuildId());
                if (guild != null) {
                    prefix = prefix.append(Component.text(guild.getTag() + " ").color(NamedTextColor.AQUA));
                }
            }
        }

        Component body = message.color(msgColor);
        if (nobodyHeard) body = body.decorate(TextDecoration.ITALIC);

        return prefix
                .append(player.displayName().color(NamedTextColor.WHITE))
                .append(Component.text(": ").color(NamedTextColor.GRAY))
                .append(body);
    }
}