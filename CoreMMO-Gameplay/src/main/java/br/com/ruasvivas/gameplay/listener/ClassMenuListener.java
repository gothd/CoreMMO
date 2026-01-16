package br.com.ruasvivas.gameplay.listener;

import br.com.ruasvivas.api.CoreRegistry;
import br.com.ruasvivas.api.dao.UserDAO;
import br.com.ruasvivas.common.model.RPGClass;
import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import br.com.ruasvivas.gameplay.ui.ClassSelectionMenu;
import br.com.ruasvivas.gameplay.ui.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ClassMenuListener implements Listener {

    private final JavaPlugin plugin;
    private final CacheManager cacheManager;

    public ClassMenuListener(JavaPlugin plugin, CacheManager cacheManager) {
        this.plugin = plugin;
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        // Validação pelo Título (Componente)
        if (!event.getView().title().equals(ClassSelectionMenu.TITLE)) return;

        event.setCancelled(true);

        // Proteção RawSlot: Impede interações com o inventário do próprio jogador
        // enquanto o menu está aberto (evita bugs de Shift+Click)
        if (event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Lógica de Identificação: Ícone -> Enum
        RPGClass selectedClass = null;
        for (RPGClass c : RPGClass.values()) {
            try {
                // Converte String (Common) -> Material (Bukkit) para comparar
                Material icon = Material.valueOf(c.getIconMaterial());
                if (icon == clickedItem.getType()) {
                    selectedClass = c;
                    break;
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (selectedClass != null) {
            processChoice(player, selectedClass);
        }
    }

    private void processChoice(Player player, RPGClass newClass) {
        User user = cacheManager.getUser(player); //
        if (user == null) return;

        // Regra: Só pode trocar se for Novato (Starter)
        if (user.getRpgClass() != RPGClass.NOVICE) {
            player.sendMessage(Component.text("Você já é um " + user.getRpgClass().getDisplayName() + "!")
                    .color(NamedTextColor.RED));
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Atualiza Cache
        user.setRpgClass(newClass);
        player.closeInventory();

        // Feedback
        player.sendMessage(Component.text("Destino selado! Você agora é um ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(newClass.getDisplayName()) // Nome em PT-BR
                        .color(NamedTextColor.GOLD))); // Ajustei para Gold para destaque

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // Atualiza o Placar imediatamente para refletir a mudança
        new ScoreboardManager().createScoreboard(player);

        // Persistência Async
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            CoreRegistry.getSafe(UserDAO.class).ifPresent(dao -> dao.saveUser(user));
        });
    }
}