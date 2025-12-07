package br.com.ruasvivas.coreMMO.cache;

import br.com.ruasvivas.coreMMO.model.Guilda;

import java.util.HashMap;
import java.util.Map;

public class GerenteGuilda {

    // Mapa 1: Busca rápida por ID
    private final Map<Integer, Guilda> porId = new HashMap<>();

    // Mapa 2: Busca rápida por Texto (Nome ou Tag)
    private final Map<String, Guilda> porNome = new HashMap<>();

    public void registrarGuilda(Guilda guilda) {
        porId.put(guilda.getId(), guilda);
        porNome.put(guilda.getNome().toLowerCase(), guilda);
        porNome.put(guilda.getTag().toLowerCase(), guilda);
    }

    public void removerGuilda(Guilda guilda) {
        porId.remove(guilda.getId());
        porNome.remove(guilda.getNome().toLowerCase());
        porNome.remove(guilda.getTag().toLowerCase());
    }

    public Guilda getPorId(int id) {
        return porId.get(id);
    }

    public Guilda getPorNome(String texto) {
        return porNome.get(texto.toLowerCase());
    }
}