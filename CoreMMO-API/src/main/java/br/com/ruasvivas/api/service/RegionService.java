package br.com.ruasvivas.api.service;

import br.com.ruasvivas.common.model.region.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

public interface RegionService {

    /**
     * Retorna a região mais prioritária em uma localização.
     */
    Optional<Region> getRegionAt(Location location);

    /**
     * Verifica se um jogador pode construir/quebrar naquele bloco.
     */
    boolean canBuild(Player player, Location location);

    /**
     * Registra uma nova região em memória (e no banco no futuro).
     */
    void registerRegion(Region region);
}