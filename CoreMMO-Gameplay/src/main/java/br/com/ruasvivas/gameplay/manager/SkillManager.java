package br.com.ruasvivas.gameplay.manager;

import br.com.ruasvivas.api.skill.Skill;
import br.com.ruasvivas.common.model.RPGClass;
import br.com.ruasvivas.gameplay.skill.FireballSkill;
import br.com.ruasvivas.gameplay.skill.HeavyStrikeSkill;
import br.com.ruasvivas.gameplay.skill.PrecisionShotSkill;

import java.util.HashMap;
import java.util.Map;

public class SkillManager {

    private final Map<RPGClass, Skill> skills = new HashMap<>();

    public SkillManager() {
        registerDefaults();
    }

    private void registerDefaults() {
        // Registra as skills nativas do Core
        registerSkill(RPGClass.MAGE, new FireballSkill());
        registerSkill(RPGClass.WARRIOR, new HeavyStrikeSkill());
        registerSkill(RPGClass.ARCHER, new PrecisionShotSkill());
    }

    // Método público para permitir que ADDONS registrem skills novas!
    public void registerSkill(RPGClass rpgClass, Skill skill) {
        skills.put(rpgClass, skill);
    }

    public Skill getSkill(RPGClass rpgClass) {
        return skills.get(rpgClass);
    }
}