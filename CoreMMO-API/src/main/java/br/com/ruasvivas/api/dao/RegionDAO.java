package br.com.ruasvivas.api.dao;

import br.com.ruasvivas.common.model.region.Region;
import java.util.List;

public interface RegionDAO {

    /**
     * Salva ou atualiza uma região no banco de dados.
     */
    void saveRegion(Region region);

    /**
     * Deleta uma região do banco de dados pelo ID.
     */
    void deleteRegion(String id);

    /**
     * Carrega todas as regiões do banco de dados (para o onEnable).
     */
    List<Region> loadAllRegions();
}