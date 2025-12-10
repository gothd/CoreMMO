package br.com.ruasvivas.coreMMO.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.*;

import java.util.ArrayList;

public class NPC {

    private final String id;
    private final String nomeVisivel;
    private final String titulo;
    private final EntityType tipo;
    private final Location localizacao;

    private LivingEntity entidadeCorpo;
    private TextDisplay entidadeTitulo;

    public NPC(String id, String nome, String titulo, EntityType tipo, Location loc) {
        this.id = id;
        this.nomeVisivel = nome;
        this.titulo = titulo;
        this.tipo = tipo;
        this.localizacao = loc;
    }

    public void spawnar() {
        if (localizacao.getWorld() == null) return;

        // 1. Cria o Corpo
        Entity entity = localizacao.getWorld().spawnEntity(localizacao, tipo);

        if (entity instanceof LivingEntity vivo) {
            vivo.setAI(false);         // Não anda
            vivo.setSilent(true);      // Não faz barulho
            vivo.setInvulnerable(true);// Proteção básica
            vivo.setRemoveWhenFarAway(false); // Persistência
            vivo.setCollidable(false); // Não empurra jogadores

            // Tags Essenciais para o Gerente encontrar e limpar depois
            vivo.addScoreboardTag("npc_interativo");
            vivo.addScoreboardTag("npc:" + id);

            // Se for um aldeão, removemos a capacidade de troca
            if (vivo instanceof Villager villager) {
                // Define a lista de receitas como vazia
                villager.setRecipes(new ArrayList<>());
            }

            this.entidadeCorpo = vivo;
        }

        // 2. Cria o Título (Holograma)
        Location locTitulo = localizacao.clone().add(0, tipo == EntityType.VILLAGER ? 2.3 : 2.5, 0);

        entidadeTitulo = localizacao.getWorld().spawn(locTitulo, TextDisplay.class, display -> {
                    display.text(Component.text(nomeVisivel).color(NamedTextColor.YELLOW)
                            .append(Component.newline())
                            .append(Component.text(titulo).color(NamedTextColor.GRAY))
                    );
                    display.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);

                    // IMPORTANTE: O título também precisa da tag para ser removido!
                    display.addScoreboardTag("npc_interativo");
                });
    }

    public void remover() {
        if (entidadeCorpo != null) entidadeCorpo.remove();
        if (entidadeTitulo != null) entidadeTitulo.remove();
    }
}