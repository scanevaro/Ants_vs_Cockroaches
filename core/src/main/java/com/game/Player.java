package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Character {
    private boolean defending;

    public Player(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite, boolean hasArmor) {
        super(name, health, attackPower, positionX, positionY, sprite, hasArmor);
        this.defending = false;
    }

    public void attack(Character target, Integer attackIndex) {
        target.takeDamage(attackPower, attackIndex);
    }

    public void defend(boolean defend) {
        defending = defend;
    }

    public void takeDamage(int damage) {
        if (defending) {
            damage /= 2;
            defending = false;
        }
        super.takeDamage(damage, 0);
    }

    public void waitAction() {
        healOne();
    }

    // Actualizar posición del sprite
    public void setPosition(float x, float y) {
        this.positionX = x;
        this.positionY = y;
        this.sprite.setPosition(x, y); // Asegúrate de mover el sprite también
    }

    public boolean isDefending() {
        return defending;
    }
}
