package br.com.ruasvivas.api.service;

import br.com.ruasvivas.common.model.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface CacheService {
    /**
     * Retorna o usuário do cache (RAM).
     * Retorna null se o jogador estiver offline ou os dados ainda não carregaram.
     */
    @Nullable
    User getUser(UUID uuid);
    @Nullable
    User getUser(Player player);

    // TODO: adicionar:
    // boolean isLoaded(UUID uuid);
    // void invalidate(UUID uuid);
}