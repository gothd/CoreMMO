package br.com.ruasvivas.gameplay.npc;

import br.com.ruasvivas.gameplay.manager.NPCManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.ArrayList;

public class NPC {

    private final String id;
    private final String visibleName;
    private final String title;
    private final EntityType type;
    private final Location location;

    private LivingEntity bodyEntity;
    private TextDisplay titleEntity;

    public NPC(String id, String visibleName, String title, EntityType type, Location location) {
        this.id = id;
        this.visibleName = visibleName;
        this.title = title;
        this.type = type;
        this.location = location;
    }

    public void spawn() {
        if (location.getWorld() == null) return;
        // Garante chunk carregado para spawnar
        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        // CORREÇÃO DE CORES
        // Transforma "&eNome" em Componente Real
        Component nameComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(visibleName);
        Component titleComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(title);

        // Spawn do Corpo Físico
        Entity entity = location.getWorld().spawnEntity(location, type);

        if (entity instanceof LivingEntity living) {
            living.setAI(false);
            living.setSilent(true);
            living.setInvulnerable(true);
            living.setRemoveWhenFarAway(false);
            living.setCollidable(false);

            // Tags essenciais para a interação
            living.addScoreboardTag(NPCManager.NPC_TAG);
            living.addScoreboardTag("npc:" + id);

            // Nome Customizado (para aparecer quando aponta)
            living.customName(nameComponent);
            living.setCustomNameVisible(true);

            if (living instanceof Villager villager) {
                villager.setRecipes(new ArrayList<>());
            }

            this.bodyEntity = living;
        }

        // Spawn do Holograma (Título)
        // Ajuste de altura dependendo do mob
        double heightOffset = (type == EntityType.VILLAGER) ? 2.05 : 2.85;
        Location titleLoc = location.clone().add(0, heightOffset, 0);

        titleEntity = (TextDisplay) location.getWorld().spawnEntity(titleLoc, EntityType.TEXT_DISPLAY);
        titleEntity.text(titleComponent);
        titleEntity.setBillboard(Display.Billboard.CENTER);
        titleEntity.addScoreboardTag(NPCManager.NPC_TAG);
        titleEntity.addScoreboardTag("npc_title:" + id);
    }

    public void remove() {
        // Carregar o chunk antes de remover
        // Sem isso, a entidade não é encontrada e vira um "fantasma" no arquivo de região.
        if (location.getWorld() != null && !location.getChunk().isLoaded()) {
            location.getChunk().load();
        }
        if (bodyEntity != null) {
            bodyEntity.remove();
            bodyEntity = null;
        }
        if (titleEntity != null) {
            titleEntity.remove();
            titleEntity = null;
        }
    }

    public Location getLocation() {
        return location;
    }
}