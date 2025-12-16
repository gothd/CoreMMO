package br.com.ruasvivas.coreMMO.habilidades;

import br.com.ruasvivas.coreMMO.model.ClasseRPG;

import java.util.HashMap;
import java.util.Map;

public class GerenteHabilidades {

    private final Map<ClasseRPG, Habilidade> mapaHabilidades = new HashMap<>();

    public GerenteHabilidades() {
        registrar();
    }

    private void registrar() {
        mapaHabilidades.put(ClasseRPG.MAGO, new BolaDeFogo());
        mapaHabilidades.put(ClasseRPG.GUERREIRO, new GolpePesado());
        mapaHabilidades.put(ClasseRPG.ARQUEIRO, new TiroPreciso());
        // Novato: Não tem ativa.
    }

    public Habilidade getHabilidade(ClasseRPG classe) {
        return mapaHabilidades.get(classe);
    }
}