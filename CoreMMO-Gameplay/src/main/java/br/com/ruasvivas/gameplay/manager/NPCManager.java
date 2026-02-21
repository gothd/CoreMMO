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
                    plugin.getLogger().warning("Tipo inválido para NPC '" + id + "'.");
                    continue;
                }


                // Leitura da Localização
                // SAFE LOADING: Ignora se não houver mundo definido
                String worldName = section.getString(path + "location.world");
                if (worldName == null || worldName.equalsIgnoreCase("none") || worldName.isEmpty()) {
                    plugin.getLogger().info("NPC '" + id + "' aguardando local. Use /npc set " + id + " no jogo.");
                    continue; // Pula este NPC e não tenta spawnar
                }

                double x = section.getDouble(path + "location.x");
                double y = section.getDouble(path + "location.y");
                double z = section.getDouble(path + "location.z");
                float yaw = (float) section.getDouble(path + "location.yaw");
                float pitch = (float) section.getDouble(path + "location.pitch");

                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

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

    // GHOST VALIDATOR: Checa se a entidade física bate com a memória atual
    public boolean isValidEntity(Entity entity) {
        String npcId = null;
        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("npc:")) npcId = tag.substring(4);
            else if (tag.startsWith("npc_title:")) npcId = tag.substring(10);
        }

        if (npcId == null) return false; // Tem a tag base, mas perdeu o ID? Lixo.

        NPC active = activeNpcs.get(npcId);
        if (active == null) return false; // NPC deletado do config

        // Verifica se ele está no local correto.
        // Hologramas ficam mais altos, então dá uma margem de ~4 blocos (16 de dist quadrada)
        return active.getLocation().getWorld().equals(entity.getWorld()) &&
                active.getLocation().distanceSquared(entity.getLocation()) <= 16.0;
    }

    // IN-GAME SETUP: Salva nova localização e recarrega
    public void setNpcLocation(String id, Location loc) {
        // Formata os números para evitar 3.1415926535... no config
        plugin.getConfig().set("npcs." + id + ".location.world", loc.getWorld().getName());
        plugin.getConfig().set("npcs." + id + ".location.x", Math.round(loc.getX() * 100.0) / 100.0);
        plugin.getConfig().set("npcs." + id + ".location.y", Math.round(loc.getY() * 100.0) / 100.0);
        plugin.getConfig().set("npcs." + id + ".location.z", Math.round(loc.getZ() * 100.0) / 100.0);
        plugin.getConfig().set("npcs." + id + ".location.yaw", Math.round(loc.getYaw() * 10.0) / 10.0);
        plugin.getConfig().set("npcs." + id + ".location.pitch", Math.round(loc.getPitch() * 10.0) / 10.0);

        plugin.saveConfig();
        loadNPCs(); // Recarrega tudo e faz o spawn no novo local
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
        if (!loc.getChunk().isLoaded()) loc.getChunk().load();

        // Limpa lixo residual exatamente no local do spawn novo
        for (Entity e : loc.getNearbyEntities(3, 5, 3)) {
            if (e.getScoreboardTags().contains(NPC_TAG)) e.remove();
        }

        NPC npc = new NPC(id, name, title, type, loc);
        npc.spawn();
        activeNpcs.put(id, npc);
    }
}