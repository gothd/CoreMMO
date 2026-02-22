package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.service.RegionService;
import br.com.ruasvivas.common.model.region.Region;
import br.com.ruasvivas.common.model.region.RegionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RegionManager implements RegionService {

    // Mapa com todas as regiões carregadas em memória (ID -> Region)
    private final Map<String, Region> regions = new ConcurrentHashMap<>();

    @Override
    public void registerRegion(Region region) {
        regions.put(region.getId().toLowerCase(), region);
    }

    public void removeRegion(String id) {
        regions.remove(id.toLowerCase());
    }

    /**
     * Encontra todas as regiões que contém a coordenada e retorna a de MAIOR prioridade.
     */
    @Override
    public Optional<Region> getRegionAt(Location location) {
        if (location.getWorld() == null) return Optional.empty();

        String world = location.getWorld().getName();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return regions.values().stream()
                .filter(r -> r.contains(world, x, y, z))
                .max(Comparator.comparingInt(Region::getPriority)); // Pega a maior prioridade!
    }

    /**
     * Validação global de quebra/construção de blocos.
     */
    @Override
    public boolean canBuild(Player player, Location location) {
        // Admins ignoram proteções
        if (player.hasPermission("coremmo.admin")) return true;

        Optional<Region> optRegion = getRegionAt(location);

        // Se não tiver região (Wilderness), pode construir livremente
        if (optRegion.isEmpty()) return true;

        Region region = optRegion.get();

        // Se for Safe Zone ou Dungeon, ninguém constrói/quebra (exceto admin)
        if (region.getType() == RegionType.SAFE_ZONE || region.getType() == RegionType.DUNGEON) {
            return false;
        }

        // TODO: Quando implementar terrenos de Guilda, checar o ID da guilda aqui
        if (region.getType() == RegionType.GUILD_CLAIM) {
            // return playerGuildId == region.getOwnerGuildId();
            return false; // Temporariamente bloqueado
        }

        return region.getType().isCanBuildDefault();
    }
}