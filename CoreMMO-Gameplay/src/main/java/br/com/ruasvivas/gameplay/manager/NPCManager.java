package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.gameplay.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class NPCManager {

    public static final String NPC_TAG = "npc_interactive";

    private final JavaPlugin plugin;
    private final Map<String, NPC> activeNpcs = new HashMap<>();

    // Armazena diálogos em memória: Map<NPC_ID, ListaDeFalas>
    private final Map<String, List<String>> npcDialogues = new HashMap<>();

    public NPCManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadNPCs() {
        // Limpa anteriores da memória se for um reload
        shutdown();
        npcDialogues.clear();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("npcs");
        if (section == null) {
            plugin.getLogger().warning("Nenhuma seção 'npcs' encontrada no config.yml.");
            return;
        }

        int count = 0;
        for (String id : section.getKeys(false)) {
            try {
                // Leitura dos dados básicos
                String path = id + ".";
                String name = section.getString(path + "name", "NPC");
                String title = section.getString(path + "title", "");
                String typeName = section.getString(path + "type", "VILLAGER");

                // Carrega diálogos
                List<String> dialogues = section.getStringList(path + "dialogue");
                npcDialogues.put(id, dialogues);

                // Conversão segura de Enum
                EntityType type;
                try {
                    type = EntityType.valueOf(typeName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Tipo de entidade inválido para NPC '" + id + "': " + typeName);
                    continue;
                }

                // Leitura da Localização
                String worldName = section.getString(path + "location.world", "world");
                double x = section.getDouble(path + "location.x");
                double y = section.getDouble(path + "location.y");
                double z = section.getDouble(path + "location.z");
                float yaw = (float) section.getDouble(path + "location.yaw");
                float pitch = (float) section.getDouble(path + "location.pitch");

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("Mundo '" + worldName + "' não carregado para o NPC " + id);
                    continue;
                }

                Location loc = new Location(world, x, y, z, yaw, pitch);

                // Criação Efetiva
                create(id, name, title, type, loc); //
                count++;

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Erro ao carregar NPC: " + id, e);
            }
        }
        plugin.getLogger().info(count + " NPCs carregados via configuração.");
    }

    public List<String> getDialogue(String npcId) {
        return npcDialogues.getOrDefault(npcId, Collections.emptyList());
    }

    public void shutdown() {
        // Remove as entidades fisicamente do mundo
        activeNpcs.values().forEach(NPC::remove);
        activeNpcs.clear();
    }

    private void create(String id, String name, String title, EntityType type, Location loc) {
        if (!loc.getChunk().isLoaded()) {
            loc.getChunk().load();
        }

        // Remove NPCs (e Títulos) antigos neste local exato
        for (Entity e : loc.getNearbyEntities(2, 4, 2)) {
            if (e.getScoreboardTags().contains(NPC_TAG)) {
                e.remove();
            }
        }

        NPC npc = new NPC(id, name, title, type, loc);
        npc.spawn();
        activeNpcs.put(id, npc);
    }
}