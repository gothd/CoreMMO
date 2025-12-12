package br.com.ruasvivas.coreMMO.model;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum ClasseRPG {

    NOVATO("Novato", NamedTextColor.GRAY, Material.LEATHER_HELMET,
            "Apenas um viajante."),

    GUERREIRO("Guerreiro", NamedTextColor.RED, Material.IRON_SWORD,
            "Mestre do combate corpo a corpo.",
            "Alta vida, dano físico massivo."),

    MAGO("Mago", NamedTextColor.BLUE, Material.BLAZE_ROD,
            "Dominador das artes arcanas.",
            "Dano em área, baixa defesa."),

    ARQUEIRO("Arqueiro", NamedTextColor.GREEN, Material.BOW,
            "Precisão letal à distância.",
            "Alta agilidade, dano crítico.");

    private final String nome;
    private final NamedTextColor cor;
    private final Material icone;
    private final String[] descricao;

    ClasseRPG(String nome, NamedTextColor cor, Material icone, String... descricao) {
        this.nome = nome;
        this.cor = cor;
        this.icone = icone;
        this.descricao = descricao;
    }

    // Getters padrão
    public String getNome() { return nome; }
    public NamedTextColor getCor() { return cor; }
    public Material getIcone() { return icone; }
    public String[] getDescricao() { return descricao; }
}