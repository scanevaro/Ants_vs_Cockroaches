package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Character {

    public Enemy(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite) {
        super(name, health, attackPower, positionX, positionY, sprite);
    }

    public String attackRandom(Array<Player> players) {
        Player target = players.get(MathUtils.random(0,2));
        if (!target.isAlive()) {
            target = players.get(MathUtils.random(0,2));
        }
        int attackDamage = MathUtils.random(attackPower,3);
        target.takeDamage(attackDamage);

        return "Enemy attacked " + target.getName() + " for " + attackDamage + " damage!";
    }
}
