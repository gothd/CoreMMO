package br.com.ruasvivas.coreMMO.npcs;

import br.com.ruasvivas.coreMMO.CoreMMO;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class GerenteNPC {

    private final CoreMMO plugin;
    private final Map<String, NPC> npcsAtivos = new HashMap<>();

    public GerenteNPC(CoreMMO plugin) {
        this.plugin = plugin;
    }

    public void carregarNPCs() {
        World mundo = Bukkit.getWorld("world");
        if (mundo != null) {
            // Criação dos NPCs fixos
            criar("ferreiro", "Borg", "<Ferreiro>", EntityType.IRON_GOLEM,
                    new Location(mundo, 1, 176, 192)); // Ajuste X,Y,Z

            criar("guia", "Merlin", "<Guia>", EntityType.VILLAGER,
                    new Location(mundo, -8, 181, 208)); // Ajuste X,Y,Z
        }
    }

    public void desligarTudo() {
        // Remove os NPCs da memória RAM ao desligar o plugin
        npcsAtivos.values().forEach(NPC::remover);
        npcsAtivos.clear();
    }

    private void criar(String id, String nome, String titulo, EntityType tipo, Location loc) {
        // 1. Garante que a área está carregada para encontrar duplicatas
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load();
        }

        // 2. Remove NPCs (e Títulos) antigos neste local exato
        for (Entity e : loc.getNearbyEntities(2, 4, 2)) {
            if (e.getScoreboardTags().contains("npc_interativo")) {
                e.remove();
            }
        }

        // 3. Spawna o novo
        NPC npc = new NPC(id, nome, titulo, tipo, loc);
        npc.spawnar();
        npcsAtivos.put(id, npc);
    }
}