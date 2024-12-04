package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Character {

    public Enemy(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite, boolean hasArmor) {
        super(name, health, attackPower, positionX, positionY, sprite, hasArmor);
    }

    public String attackRandom(Array<Player> players) {
        Player target = players.get(MathUtils.random(0, 2));
        if (!target.isAlive()) {
            target = players.get(MathUtils.random(0, 2));
        }
        int attackDamage = MathUtils.random(attackPower, attackPower++);

        boolean defending = false;
        if (target.isDefending()) defending = true;
        target.takeDamage(attackDamage);

        if (defending && attackDamage > 1) {
            return target.getName() + " defended " + attackDamage + " damage!";
        }
        return "Enemy attacked " + target.getName() + " for " + attackDamage + " damage!";
    }
}
