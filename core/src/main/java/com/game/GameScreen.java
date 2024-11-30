package com.game;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private BitmapFont font;
    private Array<Player> players;
    private Enemy enemy;

    private int state; // 0: Selección de acciones, 1: Ejecución de acciones, 2: Turno del enemigo, 3: Fin del juego

    private Sprite warriorTexture, cockroachFlippedTexture, mageTexture, archerTexture, cockroachTexture, grassBackground;
    private int currentPlayerIndex;
    private Array<Runnable> actionsQueue; // Lista de acciones que se ejecutarán
    private int attackIndex, enemyPositionX, enemyPositionY, frontAntPositionX, frontAntPositionY, midAntPositionX, midAntPositionY, backAntPositionX, backAntPositionY;
    private String statusText;
    private int killStreak;
    private int enemyAttackPower;
    private Array<Music> musics;
    private Music musicPlaying;
    private Sound babyCockroachDeath, punch1, punch2, punch3, loose;

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(3.5f);

        // Cargar texturas
        warriorTexture = new Sprite(new Texture("warrior.png"));
        mageTexture = new Sprite(new Texture("mage.png"));
        archerTexture = new Sprite(new Texture("archer.png"));
        cockroachTexture = new Sprite(new Texture("cockroach.png"));
        cockroachFlippedTexture = new Sprite(new Texture("cockroach.png"));
        cockroachFlippedTexture.flip(true, true);
        grassBackground = new Sprite(new Texture("grass.png"));

        grassBackground.setPosition(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        grassBackground.setCenter(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        grassBackground.setScale(2f);
        grassBackground.setAlpha(90);

        frontAntPositionX = (int) (mageTexture.getWidth() + archerTexture.getWidth() + 175);
        frontAntPositionY = 400;
        midAntPositionX = (int) (mageTexture.getWidth() + 100);
        midAntPositionY = 400;
        backAntPositionX = 100;
        backAntPositionY = 400;
        enemyPositionX = (int) (Gdx.graphics.getWidth() - cockroachTexture.getWidth());
        enemyPositionY = 400;
        enemyAttackPower = 1;

        // Crear un equipo de 3 jugadores
        players = new Array<>();

        players.add(new Player("Warrior Ant", MathUtils.random(10, 15), 1, frontAntPositionX, frontAntPositionY, warriorTexture));
        players.add(new Player("Archer Ant", MathUtils.random(8, 13), 1, midAntPositionX, midAntPositionY, archerTexture));
        players.add(new Player("Mage Ant", MathUtils.random(6, 11), 1, backAntPositionX, backAntPositionY, mageTexture));

        // Crear enemigo
        enemy = new Enemy("Baby Cockroach", MathUtils.random(5, 10), enemyAttackPower, enemyPositionX, enemyPositionY, cockroachTexture);

        state = 0; // Inicia con la selección de acciones de los jugadores
        currentPlayerIndex = 0;
        actionsQueue = new Array<>();

        attackIndex = 0;
        killStreak = 0;

        startMusic();
        setSFX();

        cockroachFlippedTexture.setPosition(enemyPositionX, enemyPositionY - 300);
    }

    private void setSFX() {
        babyCockroachDeath = Gdx.audio.newSound(Gdx.files.internal("babyCockroachDeath.mp3"));
        punch1 = Gdx.audio.newSound(Gdx.files.internal("punch1.mp3"));
        punch2 = Gdx.audio.newSound(Gdx.files.internal("punch2.mp3"));
        punch3 = Gdx.audio.newSound(Gdx.files.internal("punch3.mp3"));
        loose = Gdx.audio.newSound(Gdx.files.internal("loose.mp3"));
    }

    private void startMusic() {
        musics = new Array<>();
        musics.add(Gdx.audio.newMusic(Gdx.files.internal("kenTheme.mp3")));
        musics.add(Gdx.audio.newMusic(Gdx.files.internal("guileTheme.mp3")));
        musics.add(Gdx.audio.newMusic(Gdx.files.internal("ryuTheme.mp3")));

        musicPlaying = musics.random();
        musicPlaying.setLooping(true);
        musicPlaying.setVolume(0.2f);
        musicPlaying.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        grassBackground.draw(batch);

        // Dibujar jugadores y sus barras de vida en sus nuevas posiciones
        for (Player player : players) {
            player.draw(batch, font);
        }

        // Dibujar al enemigo
        if (enemy.isAlive()) enemy.draw(batch, font);
        else cockroachFlippedTexture.draw(batch);

        // How to play
        font.setColor(Color.ORANGE);
        font.getData().setScale(2f);
        font.draw(batch, "How to play", 50, 150);
        font.draw(batch, "Actions 'Attack' and 'Defend' move ants forward on their turn, 'Wait' heals 1.", 50, 100);
        font.draw(batch, "Consecutive attacks add 1 damage. A different action resets the combo", 50, 50);
        font.getData().setScale(3.5f);
        font.setColor(Color.WHITE);

        // Status Text
        if (statusText != null) {
            font.getData().setScale(2.6f);
            font.draw(batch, "Status:", 1250, 250);
            font.draw(batch, statusText, 1250, 215);
            font.getData().setScale(3.5f);
        }

        // Enemy kill streak
        font.draw(batch, "Current kill streak = " + killStreak, 1475, 50);

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
                if (!enemy.isAlive()) {
                    state = 3;
                    babyCockroachDeath.play();
                    killStreak++;
                    if (players.peek().isAlive()) { //heal 1 hp to last ant
                        players.peek().healOne();
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
        if (!currentPlayer.isAlive()) {
            nextPlayerAction();
            return;
        }

        font.draw(batch, currentPlayer.getName() + " turn", 50, 275);
        font.draw(batch, "Press 'A' to atack, 'D' to defend, 'W' to wait", 50, 225);

        for (int i = 0; i < players.size; i++) {
            players.get(i).defend(false);
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.A)) {
            actionsQueue.add(() -> {
                currentPlayer.attack(enemy, attackIndex);
                if (attackIndex == 0) {
                    punch1.play();
                } else if (attackIndex == 1) {
                    punch3.play();
                } else if (attackIndex == 2) {
                    punch2.play();
                }
                attackIndex++;
                movePlayerToFront(currentPlayer);
            });
            nextPlayerAction();
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.D)) {
            actionsQueue.add(() -> {
                currentPlayer.defend(true);
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
        players.removeIndex(players.indexOf(player, true));
        players.insert(0, player); // Colocar al jugador en la primera posición
        recalculatePlayerPositions(); // Recalcular posiciones de todos los jugadores
    }

    private void recalculatePlayerPositions() {
        // Define posiciones base para los jugadores
        float baseX = frontAntPositionX; // Posición inicial en X
        float baseY = frontAntPositionY; // Posición base para los sprites

        for (int i = 0; i < players.size; i++) {
            Player player = players.get(i);
            player.setPosition(baseX, baseY); // Reubica sprites
            baseX = baseX - (frontAntPositionX - midAntPositionX);
            if (baseX < 100) baseX = 100;
        }
    }


    private void nextPlayerAction() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.size) {
            currentPlayerIndex = 0; // Reiniciar el índice
            statusText = "Performing actions...";
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
        if (areAllPlayersDead()) {
            state = 3; // Todos los jugadores están muertos
        } else {
            state = 0; // Volver al turno del jugador
        }
    }

    private boolean areAllPlayersDead() {
        for (Player player : players) {
            if (player.isAlive()) {
                return false; // Si hay al menos un jugador vivo, retorna falso
            }
        }
        return true; // Todos los jugadores están muertos
    }

    private void handleEndGame() {
        if (!areAllPlayersDead()) {
            font.draw(batch, "You won!", 50, 275);
            statusText = "Last ant healed 1!";
        } else {
            font.draw(batch, "You've been defeated, noob...", 50, 275);
        }
        font.draw(batch, "Press 'R' for next fight...", 50, 225);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            restart();
        }
    }

    private void restart() {
        // new enemy
        enemyAttackPower++;
        enemy = new Enemy("Baby Cockroach", MathUtils.random(enemy.maxHealth, enemy.maxHealth + 3), enemyAttackPower, enemyPositionX, enemyPositionY, cockroachTexture);
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
        for (int i = 0; musics.size > i; i++) musics.get(i).dispose();
        punch2.dispose();
        punch1.dispose();
        punch3.dispose();
    }
}
