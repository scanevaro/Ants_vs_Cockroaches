package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Character {
    private boolean defending;

    public Player(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite) {
        super(name, health, attackPower, positionX, positionY, sprite);
        this.defending = false;
    }

    public void attack(Character target, Integer attackIndex) {
        target.takeDamage(attackPower + attackIndex);
    }

    public void defend(boolean defend) {
        defending = defend;
    }

    @Override
    public void takeDamage(int damage) {
        if (defending) {
            damage /= 2;
            defending = false;
        }
        super.takeDamage(damage);
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
