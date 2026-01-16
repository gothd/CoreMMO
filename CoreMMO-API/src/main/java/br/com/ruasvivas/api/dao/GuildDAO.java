package br.com.ruasvivas.api.dao;

import br.com.ruasvivas.common.model.Guild;
import java.util.List;

public interface GuildDAO {

    // Cria e define o ID gerado no objeto
    void createGuild(Guild guild);

    // Carrega para o cache inicial
    List<Guild> fetchAll();

    // Atualiza KDR e contagem de membros
    void updateStatistics(Guild guild);

    // Remove do banco
    void deleteGuild(int id);
}