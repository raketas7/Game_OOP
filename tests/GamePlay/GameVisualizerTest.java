package GamePlay;

import gui.Enemies.BasicEnemy;
import gui.Enemies.Enemy;
import gui.Enemies.FastEnemy;
import gui.GameMechanics.Bullet;
import gui.GameMechanics.Player;
import gui.GameMechanics.UpgradeType;
import gui.Visuals.GameVisualizer;
import gui.WaveManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static gui.Visuals.GameVisualizer.MAP_SIZE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GameVisualizerTest {
    private TestableGameVisualizer gameVisualizer;
    private Player player;
    private WaveManager waveManager;

    // Subclass to access protected methods and private fields
    private static class TestableGameVisualizer extends GameVisualizer {
        public TestableGameVisualizer(ResourceBundle bundle) {
            super(bundle);
        }

        public void exposedMovePlayer() {
            movePlayer();
        }

        public void exposedCheckCollisions() {
            checkCollisions();
        }

        public void exposedUpdateCamera() {
            updateCamera();
        }

        public void exposedUpdateWindowSize() {
            updateWindowSize();
        }

        public void exposedShoot() {
            shoot();
        }

        public void exposedUpdateBullets() {
            updateBullets();
        }

        public void exposedMoveEnemies() {
            moveEnemies();
        }

        public boolean isGameOver() {
            return gameOver;
        }

        public void setMousePosition(int x, int y) {
            mouseX = x;
            mouseY = y;
        }

        public List<Bullet> getBullets() {
            return bullets;
        }

        public List<UpgradeType> getOfferedUpgrades() {
            return offeredUpgrades;
        }

        public boolean isUpgradeSelectionMode() {
            return upgradeSelectionMode;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public GameState getGameState() {
            return gameState;
        }

        public JButton getStartButton() {
            return startButton;
        }

        public JButton getShopButton() {
            return shopButton;
        }

        public JPanel getShopPanel() {
            return shopPanel;
        }

        public int getCountdown() {
            return countdown;
        }

        public void resetGame() {
            getPlayer().reset();
            getEnemies().clear();
            getBullets().clear();
            getWaveManager().reset();
            gameOver = false;
            isPaused = false;
            countdown = 10;
            gameState = GameState.START_SCREEN;
            getStartButton().setBounds(getWindowWidth() / 2 - 100, getWindowHeight() / 2 + 50, 200, 50);
            getShopButton().setBounds(getWindowWidth() / 2 - 100, getWindowHeight() / 2 + 110, 200, 50);
            getStartButton().setVisible(true);
            getShopButton().setVisible(true);
            getShopPanel().setVisible(false);
        }
    }

    @Before
    public void setUp() {
        ResourceBundle mockBundle = Mockito.mock(ResourceBundle.class);
        when(mockBundle.getString(anyString())).thenReturn("test");
        gameVisualizer = new TestableGameVisualizer(mockBundle);
        player = gameVisualizer.getPlayer();
        waveManager = gameVisualizer.getWaveManager();
        gameVisualizer.setSize(800, 600);
        // Ensure game state is PLAYING for tests that require shooting
        gameVisualizer.getStartButton().doClick();
    }

    @Test
    public void testInitialPlayerPosition() {
        assertEquals("Player should start at map center X",
                MAP_SIZE / 2.0, player.getX(), 0.01);
        assertEquals("Player should start at map center Y",
                MAP_SIZE / 2.0, player.getY(), 0.01);
    }

    @Test
    public void testGameInitialState() {
        // Reset to START_SCREEN for this test
        gameVisualizer.resetGame();
        assertEquals("Initial game state should be START_SCREEN",
                TestableGameVisualizer.GameState.START_SCREEN, gameVisualizer.getGameState());
        assertTrue("Start button should be visible on start screen",
                gameVisualizer.getStartButton().isVisible());
    }

    @Test
    public void testPlayerMovement() {
        resetPlayerPosition();

        testMovement(KeyEvent.VK_W, () -> assertTrue(player.getY() < MAP_SIZE / 2.0), "up");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_S, () -> assertTrue(player.getY() > MAP_SIZE / 2.0), "down");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_A, () -> assertTrue(player.getX() < MAP_SIZE / 2.0), "left");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_D, () -> assertTrue(player.getX() > MAP_SIZE / 2.0), "right");
    }

    private void resetPlayerPosition() {
        player.move(MAP_SIZE / 2.0 - player.getX(), MAP_SIZE / 2.0 - player.getY(), MAP_SIZE);
    }

    private void testMovement(int keyCode, Runnable assertion, String direction) {
        gameVisualizer.getActiveKeys().add(keyCode);
        gameVisualizer.exposedMovePlayer();
        assertion.run();
        gameVisualizer.getActiveKeys().remove(keyCode);
    }

    @Test
    public void testDiagonalMovementSpeed() {
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_W);
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_D);
        gameVisualizer.exposedMovePlayer();

        double newX = player.getX();
        double newY = player.getY();

        assertTrue("Should move right", newX > MAP_SIZE / 2.0);
        assertTrue("Should move up", newY < MAP_SIZE / 2.0);
    }

    @Test
    public void testCameraFollowing() {
        double initialOffsetX = gameVisualizer.getCameraOffsetX();
        double initialOffsetY = gameVisualizer.getCameraOffsetY();

        player.move(100, 100, MAP_SIZE);
        gameVisualizer.exposedUpdateCamera();

        assertTrue("Camera should shift on X",
                gameVisualizer.getCameraOffsetX() > initialOffsetX);
        assertTrue("Camera should shift on Y",
                gameVisualizer.getCameraOffsetY() > initialOffsetY);
    }

    @Test
    public void testWindowResizing() {
        gameVisualizer.setSize(1024, 768);
        gameVisualizer.exposedUpdateWindowSize();
        gameVisualizer.exposedUpdateCamera();

        assertEquals(1024, gameVisualizer.getWindowWidth());
        assertEquals(768, gameVisualizer.getWindowHeight());
    }

    @Test
    public void testPlayerEnemyCollision() {
        waveManager.setEnemiesAlive(1);
        int initialHealth = player.getHealth();

        BasicEnemy enemy = new BasicEnemy(player.getX(), player.getY());
        gameVisualizer.getEnemies().add(enemy);
        gameVisualizer.exposedUpdateCamera();
        gameVisualizer.exposedCheckCollisions();

        assertEquals(0, gameVisualizer.getEnemies().size());
        assertEquals(0, waveManager.getEnemiesAlive());
        assertEquals(initialHealth - enemy.getDamage(), player.getHealth());
    }

    @Test
    public void testPlayerDeath() {
        BasicEnemy enemy = new BasicEnemy(player.getX(), player.getY());
        gameVisualizer.getEnemies().add(enemy);
        player.takeDamage(player.getMaxHealth());
        gameVisualizer.exposedCheckCollisions();

        assertTrue("Game should be over when player dies", gameVisualizer.isGameOver());
        assertEquals("Game state should be GAME_OVER", TestableGameVisualizer.GameState.GAME_OVER, gameVisualizer.getGameState());
    }

    @Test
    public void testWaveSpawning() {
        waveManager.startNextWave();
        assertEquals(1, waveManager.getCurrentWave());
        assertEquals(70, waveManager.getPointsAvailable());

        List<Enemy> enemies = waveManager.spawnEnemies(
                player.getX(), player.getY(), MAP_SIZE, Collections.emptyList());

        assertFalse(enemies.isEmpty());
        assertTrue(enemies.size() >= 3 && enemies.size() <= 7);
    }

    @Test
    public void testBulletCreationAndMovement() {
        gameVisualizer.setMousePosition(900, 900);
        gameVisualizer.exposedShoot();
        gameVisualizer.exposedUpdateBullets();

        assertEquals("Should create one bullet", 1, gameVisualizer.getBullets().size());
        Bullet bullet = gameVisualizer.getBullets().get(0);
        assertTrue("Bullet should be active", bullet.isActive());
    }

    @Test
    public void testBulletEnemyCollision() {
        FastEnemy enemy = new FastEnemy(player.getX() + 50, player.getY() + 50);
        gameVisualizer.getEnemies().add(enemy);
        waveManager.setEnemiesAlive(1);

        gameVisualizer.setMousePosition((int) enemy.getX(), (int) enemy.getY());
        gameVisualizer.exposedShoot();

        for (int i = 0; i < 10; i++) {
            gameVisualizer.exposedUpdateBullets();
        }

        assertTrue("Enemy should disappear after hit",
                gameVisualizer.getEnemies().isEmpty());
        assertTrue("Bullet should disappear after collision",
                gameVisualizer.getBullets().isEmpty());
    }

    @Test
    public void testEnemyMovementTowardsPlayer() {
        BasicEnemy enemy = new BasicEnemy(player.getX() + 200, player.getY() + 200);
        gameVisualizer.getEnemies().add(enemy);

        double initialDistance = Math.sqrt(
                Math.pow(enemy.getX() - player.getX(), 2) +
                        Math.pow(enemy.getY() - player.getY(), 2));

        gameVisualizer.exposedMoveEnemies();

        double newDistance = Math.sqrt(
                Math.pow(enemy.getX() - player.getX(), 2) +
                        Math.pow(enemy.getY() - player.getY(), 2));

        assertTrue("Enemy should move closer to player", newDistance < initialDistance);
    }

    @Test
    public void testLevelUpAndUpgrades() {
        FastEnemy enemy = new FastEnemy(player.getX() + 50, player.getY() + 50);
        gameVisualizer.getEnemies().add(enemy);
        waveManager.setEnemiesAlive(1);

        int bulletDamage = 10;
        enemy.takeDamage(enemy.getHealth() - bulletDamage);
        Bullet bullet = new Bullet(player.getX(), player.getY(),
                enemy.getX() + Player.SIZE / 2,
                enemy.getY() + Player.SIZE / 2,
                bulletDamage);
        gameVisualizer.getBullets().add(bullet);

        int initialLevel = player.getLevel();
        int xpNeeded = player.getXpToNextLevel() - enemy.getXpReward();
        if (xpNeeded > 0) {
            player.addXp(xpNeeded);
        }

        for (int i = 0; i < 5; i++) {
            gameVisualizer.exposedUpdateBullets();
        }

        assertEquals("Player should level up after adding XP", initialLevel + 1, player.getLevel());
        assertTrue("Game should be paused after level-up", gameVisualizer.isPaused());
        assertTrue("Upgrade selection mode should be active", gameVisualizer.isUpgradeSelectionMode());
        assertEquals("Should offer up to 3 upgrade options", Math.min(3, UpgradeType.values().length), gameVisualizer.getOfferedUpgrades().size());
        assertTrue("Bullets list should be empty after collision", gameVisualizer.getBullets().isEmpty());
    }

    @Test
    public void testGameReset() {
        player.takeDamage(player.getMaxHealth());
        gameVisualizer.resetGame();

        assertFalse("Game should not be over after reset", gameVisualizer.isGameOver());
        assertEquals("Game state should be START_SCREEN", TestableGameVisualizer.GameState.START_SCREEN,
                gameVisualizer.getGameState());
        assertEquals("Player health should reset to 200", 200, player.getHealth());
        assertTrue("Player should be alive", player.isAlive());
        assertTrue("Enemies list should be empty", gameVisualizer.getEnemies().isEmpty());
        assertTrue("Bullets list should be empty", gameVisualizer.getBullets().isEmpty());
    }
}