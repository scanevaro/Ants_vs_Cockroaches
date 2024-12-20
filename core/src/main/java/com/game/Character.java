package com.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public abstract class Character {
    protected String name;
    protected int health;
    protected int maxHealth;
    protected int attackPower;
    protected float positionX;
    protected float positionY;
    protected boolean hasArmor;
    protected Sprite sprite;
    protected Texture healthBarBackground, healthBarForeground;

    public Character(String name, int health, int attackPower, float positionX, float positionY, Sprite sprite, boolean hasArmor) {
        this.name = name;
        this.health = health;
        this.maxHealth = health; // Guardar la salud máxima para la barra de salud
        this.attackPower = attackPower;
        this.positionX = positionX;
        this.positionY = positionY;
        this.sprite = sprite;
        this.hasArmor = hasArmor;
        this.sprite.setPosition(positionX, positionY);// Posicionar el sprite
        healthBarBackground = new Texture("health_bar_background.png");
        healthBarForeground = new Texture("health_bar_foreground.png");
    }

    public void takeDamage(int damage, int attackIndex) {
        damage = damage + attackIndex;

        if (hasArmor) health--;
        else health -= damage;

        if (health < 0) {
            health = 0;
        }

        if (attackIndex >= 2) {
            GameScreen.statusText = "Enemy armor broke!";
            hasArmor = false;
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public String getName() {
        return name;
    }

    public void draw(SpriteBatch batch, BitmapFont font) {
        // Dibujar el sprite
        sprite.draw(batch);

        // Dibujar el nombre del personaje
        font.draw(batch, name, positionX, positionY);

        // Dibujar la barra de salud
        float barWidth = sprite.getWidth();
        float barHeight = 5;
        float healthPercentage = (float) health / maxHealth;
        batch.draw(healthBarBackground, // Fondo de la barra de salud
            positionX, positionY - barHeight + 5, // Debajo del sprite
            barWidth, barHeight
        );
        batch.draw(healthBarForeground, // Barra de salud según el porcentaje
            positionX, positionY - barHeight + 5,
            barWidth * healthPercentage, barHeight
        );

        // Dibujar el valor numérico de la salud
        font.draw(batch, health + "/" + maxHealth, positionX, positionY - barHeight - 40);
    }

    public void healOne() {
        if (health != maxHealth)
            health++;
    }
}
