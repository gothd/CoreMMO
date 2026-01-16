package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.common.model.Guild;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildManager {

    // Mapa 1: Busca rápida por ID
    private final Map<Integer, Guild> byId = new ConcurrentHashMap<>();

    // Mapa 2: Busca rápida por Texto (Nome ou Tag) para comandos
    private final Map<String, Guild> byName = new ConcurrentHashMap<>();

    public void registerGuild(Guild guild) {
        byId.put(guild.getId(), guild);
        byName.put(guild.getName().toLowerCase(), guild);
        byName.put(guild.getTag().toLowerCase(), guild); //
    }

    public void unregisterGuild(Guild guild) {
        byId.remove(guild.getId());
        byName.remove(guild.getName().toLowerCase());
        byName.remove(guild.getTag().toLowerCase());
    }

    public Guild getById(int id) {
        return byId.get(id);
    }

    public Guild getByNameOrTag(String text) {
        return byName.get(text.toLowerCase());
    }

    public java.util.Collection<Guild> getAll() {
        return byId.values();
    }
}