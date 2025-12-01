package br.com.ruasvivas.coreMMO.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// Implementa DUAS coisas: Comando e Evento
public class MenuClasses implements CommandExecutor, Listener {

    // O título do menu serve como nossa "senha" para identificar a janela
    private final Component TITULO_MENU = Component.text("Escolha sua Classe")
            .color(NamedTextColor.DARK_PURPLE);

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (sender instanceof Player player) {

            // 1. Criando o Baú (9 espaços = 1 linha)
            Inventory menu = Bukkit.createInventory(null, 9, TITULO_MENU);

            // 2. Criando o Botão (Espada de Ferro)
            ItemStack icone = new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = icone.getItemMeta();

            meta.displayName(Component.text("Guerreiro").color(NamedTextColor.RED));
            meta.lore(List.of(Component.text("Clique para selecionar.").color(NamedTextColor.GRAY)));

            // ItemFlag: Esconde aquele texto feio "+6 Attack Damage"
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            icone.setItemMeta(meta);

            // 3. Colocando no Slot 0 (Primeiro da esquerda)
            menu.setItem(0, icone);

            player.openInventory(menu);
            return true;
        }
        return false;
    }

    // --- A LÓGICA DE SEGURANÇA ---

    @EventHandler
    public void aoClicar(InventoryClickEvent evento) {
        // 1. É o nosso menu?
        if (!evento.getView().title().equals(TITULO_MENU)) {
            return;
        }

        // 2. REGRA DE OURO: Cancela qualquer tentativa de pegar itens
        evento.setCancelled(true);

        // 3. Verifica se clicou num item válido
        if (evento.getCurrentItem() == null) return;

        // 4. Verifica se o clique foi na PARTE DE CIMA (Menu)
        // O inventário de cima tem slots 0-8. O do jogador começa no 9.
        if (evento.getRawSlot() < 9) {

            // Foi o Guerreiro?
            if (evento.getCurrentItem().getType() == Material.IRON_SWORD) {
                Player jogador = (Player) evento.getWhoClicked();
                jogador.sendMessage(Component.text("Você virou um Guerreiro!").color(NamedTextColor.GREEN));
                jogador.closeInventory();
            }
        }
    }
}