package com.game;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Player extends Character {
    private boolean defending;

    public Player(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite) {
        super(name, health, attackPower, positionX, positionY, sprite);
        this.defending = false;
    }

    public void attack(Character target, Integer attackIndex) {
        System.out.println(name + " ataca a " + target.getName() + " causando " + attackPower + " de daño.");
        target.takeDamage(attackPower + attackIndex);
    }

    public void defend() {
        defending = true;
        System.out.println(name + " se defiende. Recibirá menos daño en el próximo ataque.");
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
        System.out.println(name + " decide esperar y no hace nada este turno");
        healOne();
    }

    // Actualizar posición del sprite
    public void setPosition(float x, float y) {
        this.positionX = x;
        this.positionY = y;
        this.sprite.setPosition(x, y); // Asegúrate de mover el sprite también
    }
}
