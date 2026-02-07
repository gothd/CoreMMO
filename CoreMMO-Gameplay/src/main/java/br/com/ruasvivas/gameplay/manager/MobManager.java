package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.service.MobService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class MobManager implements MobService {

    private final JavaPlugin plugin;
    private final Random random = new Random();
    public final NamespacedKey LEVEL_KEY;

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Cria uma chave única: "coremmo:level"
        this.LEVEL_KEY = new NamespacedKey(plugin, "level");
    }

    @Override
    public int getMobLevel(LivingEntity entity) {
        if (entity == null) return 1;

        // Verificação de Segurança (Null Check)
        // Se não tiver a chave, retorna 1 direto
        if (!entity.getPersistentDataContainer().has(LEVEL_KEY, PersistentDataType.INTEGER)) {
            return 1;
        }

        // Pega como Integer (Objeto) primeiro
        Integer level = entity.getPersistentDataContainer().get(LEVEL_KEY, PersistentDataType.INTEGER);

        // Retorna o valor ou 1 se for nulo (Safe Unboxing)
        return level != null ? level : 1;
    }

    public void setupMob(LivingEntity entity) {
        if (!(entity instanceof Monster)) return; // Só afeta monstros hostis

        // Lógica de Nível: Aleatório entre 1 e 10 (Por enquanto)
        // TODO: Calcular baseado na distância do Spawn (x, z)
        int level = random.nextInt(10) + 1;

        // PersistentDataContainer salva o 'int' dentro do arquivo do mob no disco.
        entity.getPersistentDataContainer().set(LEVEL_KEY, PersistentDataType.INTEGER, level);

        // Ajusta Atributos (Escalamento)
        double healthMultiplier = 1.0 + (level * 0.2); // +20% vida por nível
        double damageMultiplier = 1.0 + (level * 0.05); // +5% dano por nível

        // Vida
        var attrHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        if (attrHealth != null) {
            double base = attrHealth.getBaseValue(); // Pega o base original do mob
            // Reseta para o base antes de multiplicar (evita acumular se rodar 2x)
            // (TODO: guardar o atributo base original no NBT também)
            attrHealth.setBaseValue(base * healthMultiplier);
            entity.setHealth(attrHealth.getValue()); // Cura total
        }

        // Dano
        var attrDamage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attrDamage != null) {
            attrDamage.setBaseValue(attrDamage.getBaseValue() * damageMultiplier);
        }

        // Nome Visual
        updateName(entity, level);
    }

    private void updateName(LivingEntity entity, int level) {
        // Cores baseadas na dificuldade
        NamedTextColor color = level >= 8 ? NamedTextColor.RED :
                (level >= 5 ? NamedTextColor.GOLD : NamedTextColor.YELLOW);

        Component name = Component.text("[Lv." + level + "] ", color)
                .append(Component.text(entity.getType().name()).color(NamedTextColor.WHITE));

        entity.customName(name);
        entity.setCustomNameVisible(false);
    }
}