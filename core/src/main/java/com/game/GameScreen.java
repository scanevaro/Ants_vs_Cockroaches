package com.game;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private List<Player> players;
    private Enemy enemy;

    private int state; // 0: Selección de acciones, 1: Ejecución de acciones, 2: Turno del enemigo, 3: Fin del juego

    private Sprite warriorTexture, mageTexture, archerTexture, cockroachTexture;
    private int currentPlayerIndex;
    private List<Runnable> actionsQueue; // Lista de acciones que se ejecutarán
    private int attackIndex;
    private String statusText;
    private int killStreak;

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();

        // Cargar texturas
        warriorTexture = new Sprite(new Texture("warrior.png"));
        mageTexture = new Sprite(new Texture("mage.png"));
        archerTexture = new Sprite(new Texture("archer.png"));
        cockroachTexture = new Sprite(new Texture("cockroach.png"));

        // Crear un equipo de 3 jugadores
        players = new ArrayList<>();

        players.add(new Player("Warrior Ant", new Random().nextInt(10,16), 1, 300, 300, warriorTexture));
        players.add(new Player("Archer Ant", new Random().nextInt(8,14), 1, 200, 300, archerTexture));
        players.add(new Player("Mage Ant", new Random().nextInt(6,12), 1, 100, 300, mageTexture));

        // Crear enemigo
        enemy = new Enemy("Baby Cockroach", new Random().nextInt(10,16), 1, 500, 300, cockroachTexture);

        state = 0; // Inicia con la selección de acciones de los jugadores
        currentPlayerIndex = 0;
        actionsQueue = new ArrayList<>();

        attackIndex = 0;
        killStreak = 0;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        // Dibujar jugadores y sus barras de vida en sus nuevas posiciones
        for (Player player : players) {
            player.draw(batch, font);
        }

        // Dibujar al enemigo
        enemy.draw(batch, font);

        // How to play
        font.setColor(Color.ORANGE);
        font.draw(batch, "How to play", 50, 215);
        font.draw(batch, "Actions 'Attack' and 'Defend' move ants forward on their turn, 'Wait' heals 1.", 50, 200);
        font.draw(batch, "Consecutive attacks add 1 damage", 50, 185);
        font.setColor(Color.WHITE);

        // Status Text
        if(statusText != null)
            font.draw(batch, statusText, 50, 130);

        // Enemy kill streak
        font.draw(batch, "Current kill streak = " + killStreak, 50, 160);

        // Manejo de turnos y acciones
        handleTurn();

        batch.end();
    }

    private void handleTurn() {
        // Lógica del turno
        if (state == 0) { // Selección de acciones
            handlePlayerActionSelection();
        } else if (state == 1) { // Ejecución de acciones
            if (actionsQueue.isEmpty()) {
                if (!enemy.isAlive()){
                    state = 3;
                    killStreak++;
                    if (players.getLast().isAlive()){ //heal 1 hp to last ant
                        players.getLast().healOne();
                    }
                    return;
                }
                state = 2; // Pasar al turno del enemigo
            }
        } else if (state == 2) { // Turno del enemigo
            enemyTurn();
        } else if (state == 3) { // Fin del juego
            handleEndGame();
        }
    }


    private void handlePlayerActionSelection() {
        Player currentPlayer = players.get(currentPlayerIndex);

        font.draw(batch, currentPlayer.getName() + " turn", 50, 100);
        font.draw(batch, "Press 'A' to atack, 'D' to defend, 'W' to wait", 50, 70);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A)) {
            actionsQueue.add(() -> {
                currentPlayer.attack(enemy, attackIndex);
                attackIndex++;
                movePlayerToFront(currentPlayer);
            });
            nextPlayerAction();
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) {
            actionsQueue.add(() -> {
                currentPlayer.defend();
                attackIndex = 0;
                movePlayerToFront(currentPlayer);
            });
            nextPlayerAction();
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W)) {
            actionsQueue.add(() -> {
                currentPlayer.waitAction();
                attackIndex = 0;
            });
            nextPlayerAction();
        }
    }

    private void movePlayerToFront(Player player) {
        // Mover el jugador actual al frente de la lista
        players.remove(player);
        players.add(0, player); // Colocar al jugador en la primera posición
        recalculatePlayerPositions(); // Recalcular posiciones de todos los jugadores
    }

    private void recalculatePlayerPositions() {
        // Define posiciones base para los jugadores
        float baseX = 300; // Posición inicial en X
        float baseY = 300; // Posición base para los sprites

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.setPosition(baseX, baseY); // Reubica sprites
            baseX = baseX - 100;
        }
    }


    private void nextPlayerAction() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0; // Reiniciar el índice
            executeActions(); // Ejecutar las acciones seleccionadas
            attackIndex = 0; //Attack Combos
        }
    }

    private void executeActions() {
        state = 1; // Cambiar al estado de ejecución
        float delay = 0;

        for (Runnable action : actionsQueue) {
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    action.run();
                }
            }, delay);
            delay += 1; // Agregar 1 segundo entre acciones
        }

        // Limpiar la cola de acciones después de ejecutarlas
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                actionsQueue.clear();
            }
        }, delay);
    }

    private void enemyTurn() {
        font.draw(batch, "Enemy turn", 50, 100);

        statusText = enemy.attackRandom(players);
        if (players.stream().noneMatch(Player::isAlive)) {
            state = 3; // Todos los jugadores están muertos
        } else {
            state = 0; // Volver al turno del jugador
        }
    }

    private void handleEndGame() {
        if (players.stream().anyMatch(Player::isAlive)) {
            font.draw(batch, "You won!", 50, 50);
            statusText = "Last ant healed 1!";
        } else {
            font.draw(batch, "You've been defeated, noob...", 50, 50);
        }
        font.draw(batch, "Press 'R' for next fight...", 50, 20);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            restart();
        }
    }

    private void restart() {
        // new enemy
        enemy = new Enemy("Baby Cockroach", new Random().nextInt(10,16), 1, 500, 300, cockroachTexture);
        state = 0;
        statusText = null;
        currentPlayerIndex = 0;
        actionsQueue.clear();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        warriorTexture.getTexture().dispose();
        mageTexture.getTexture().dispose();
        archerTexture.getTexture().dispose();
        cockroachTexture.getTexture().dispose();
        cockroachTexture.getTexture().dispose();
    }
}
