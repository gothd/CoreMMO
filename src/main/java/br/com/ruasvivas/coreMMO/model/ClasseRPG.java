package br.com.ruasvivas.coreMMO.model;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum ClasseRPG {

    // Novato: Regenera muito (2.0), mas não tem skill ativa
    NOVATO("Novato", NamedTextColor.GRAY, Material.LEATHER_HELMET,
            null, 10, 2.0, // Pai: null, MaxLv: 10, Regen: 2.0
            "O início da jornada.", "Regeneração acelerada."),

    // As classes avançadas evoluem de Novato, vão até o Lv 100,
    // mas regeneram normal (1.0)
    GUERREIRO("Guerreiro", NamedTextColor.RED, Material.IRON_SWORD,
            "NOVATO", 100, 1.0,
            "Mestre do combate.", "Dano físico massivo."),

    MAGO("Mago", NamedTextColor.BLUE, Material.BLAZE_ROD,
            "NOVATO", 100, 1.0,
            "Dominador arcano.", "Dano mágico em área."),

    ARQUEIRO("Arqueiro", NamedTextColor.GREEN, Material.BOW,
            "NOVATO", 100, 1.0,
            "Precisão letal.", "Agilidade e Crítico.");

    private final String nome;
    private final NamedTextColor cor;
    private final Material icone;

    // Novos Campos de Mecânica
    private final String classePai; // Nome da classe anterior (Árvore)
    private final int nivelMaximo;  // Cap de nível
    private final double regeneracaoMana; // Bônus passivo

    private final String[] descricao;

    ClasseRPG(String nome, NamedTextColor cor, Material icone,
              String classePai, int nivelMaximo, double regeneracaoMana,
              String... descricao) {
        this.nome = nome;
        this.cor = cor;
        this.icone = icone;
        this.classePai = classePai;
        this.nivelMaximo = nivelMaximo;
        this.regeneracaoMana = regeneracaoMana;
        this.descricao = descricao;
    }

    // Getters
    public String getNome() { return nome; }
    public NamedTextColor getCor() { return cor; }
    public Material getIcone() { return icone; }
    public String getClassePai() { return classePai; } // Para checar evolução
    public int getNivelMaximo() { return nivelMaximo; }
    public double getRegeneracaoMana() { return regeneracaoMana; }
    public String[] getDescricao() { return descricao; }
}