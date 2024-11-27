package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.List;
import java.util.Random;

public class Enemy extends Character {
    private Random random;

    public Enemy(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite) {
        super(name, health, attackPower, positionX, positionY, sprite);
        random = new Random();
    }

    public String attackRandom(List<Player> players) {
        Player target = players.get(random.nextInt(players.size()));
        if (!target.isAlive()) {
            target = players.get(random.nextInt(players.size()));
        }
        System.out.println(name + " ataca a " + target.getName() + " causando " + attackPower + " de daño.");
        int attackDamage = new Random().nextInt(1,4);
        target.takeDamage(attackDamage);
        String string = "Enemy attacked " + target.getName() + " for " + attackDamage + " damage!";
        return string;
    }
}
