package br.com.ruasvivas.gameplay.task;

import br.com.ruasvivas.common.model.User;
import br.com.ruasvivas.gameplay.manager.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RegenTask extends BukkitRunnable {

    private final CacheManager cacheManager;

    public RegenTask(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                User user = cacheManager.getUser(player);
                if (user == null) continue;

                // Lógica Matemática (Regeneração)
                if (user.getMana() < user.getMaxMana()) {
                    double regenAmount = 5.0; // Base

                    // Aplica bônus da Classe (Lê do Enum no Common)
                    if (user.getRpgClass() != null) {
                        regenAmount *= user.getRpgClass().getManaRegen();
                    }

                    // Setter inteligente (Já faz o clamp min/max dentro do User)
                    user.setMana(user.getMana() + regenAmount);
                }

                // Lógica Visual (Interface)
                // Atualiza a barra sempre, para mantê-la fixa na tela
                cacheManager.updateActionBar(player);

            } catch (Exception e) {
                // Silencia erros individuais para não parar o loop dos outros jogadores
            }
        }
    }
}