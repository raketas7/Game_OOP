package GamePlay;

import gui.Enemies.BasicEnemy;
import gui.Enemies.Enemy;
import gui.Enemies.FastEnemy;
import gui.GameVisualizer;
import gui.PlayerMechanics.Bullet;
import gui.PlayerMechanics.Player;
import gui.PlayerMechanics.UpgradeType;
import gui.WaveManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.ResourceBundle;

import static gui.GameVisualizer.MAP_SIZE;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class GameVisualizerTest {
    private TestableGameVisualizer gameVisualizer;
    private Player player;
    private WaveManager waveManager;

    // Class to access protected methods
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

        public void setMousePosition(double x, double y) {
            mouseX = x;
            mouseY = y;
        }

        public java.util.List<Bullet> getBullets() {
            return bullets;
        }

        public java.util.List<UpgradeType> getOfferedUpgrades() {
            return offeredUpgrades;
        }

        public boolean isUpgradeSelectionMode() {
            return upgradeSelectionMode;
        }

        public boolean isPaused() {
            return isPaused;
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
    }

    @Test
    public void testInitialPlayerPosition() {
        assertEquals("Игрок должен начинаться в центре карты по X",
                MAP_SIZE / 2.0, player.getX(), 0.01);
        assertEquals("Игрок должен начинаться в центре карте по Y",
                MAP_SIZE / 2.0, player.getY(), 0.01);
    }

    @Test
    public void testGameInitialState() {
        assertEquals("Начальное состояние игры должно быть START_SCREEN",
                GameVisualizer.GameState.START_SCREEN, gameVisualizer.getGameState());
        assertTrue("На стартовом экране должна быть видна кнопка",
                gameVisualizer.getStartButton().isVisible());
    }

    @Test
    public void testPlayerMovement() {
        // Сброс позиции игрока перед каждым тестом движения
        resetPlayerPosition();

        // Тестирование движения во всех направлениях
        testMovement(KeyEvent.VK_W, () -> assertTrue(player.getY() < MAP_SIZE / 2.0), "вверх");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_S, () -> assertTrue(player.getY() > MAP_SIZE / 2.0), "вниз");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_A, () -> assertTrue(player.getX() < MAP_SIZE / 2.0), "влево");
        resetPlayerPosition();

        testMovement(KeyEvent.VK_D, () -> assertTrue(player.getX() > MAP_SIZE / 2.0), "вправо");
    }

    private void resetPlayerPosition() {
        // Жёстко устанавливаем позицию игрока в центр
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

        assertTrue("Должно быть движение вправо", newX > MAP_SIZE / 2.0);
        assertTrue("Должно быть движение вверх", newY < MAP_SIZE / 2.0);
    }

    @Test
    public void testCameraFollowing() {
        double initialOffsetX = gameVisualizer.getCameraOffsetX();
        double initialOffsetY = gameVisualizer.getCameraOffsetY();

        player.move(100, 100, MAP_SIZE);
        gameVisualizer.exposedUpdateCamera();

        assertTrue("Камера должна сместиться по X",
                gameVisualizer.getCameraOffsetX() > initialOffsetX);
        assertTrue("Камера должна сместиться по Y",
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
        player.takeDamage(player.getMaxHealth());
        gameVisualizer.exposedCheckCollisions();

        assertTrue(gameVisualizer.isGameOver());
        assertEquals(GameVisualizer.GameState.GAME_OVER, gameVisualizer.getGameState());
    }

    @Test
    public void testWaveSpawning() {
        waveManager.startNextWave();
        assertEquals(1, waveManager.getCurrentWave());
        assertEquals(70, waveManager.getPointsAvailable());

        java.util.List<Enemy> enemies = waveManager.spawnEnemies(
                player.getX(), player.getY(), MAP_SIZE, Collections.emptyList());

        assertFalse(enemies.isEmpty());
        assertTrue(enemies.size() >= 3 && enemies.size() <= 7);
    }

    @Test
    public void testBulletCreationAndMovement() {
        gameVisualizer.setMousePosition(900, 900);
        gameVisualizer.exposedShoot();
        gameVisualizer.exposedUpdateBullets();

        assertEquals(1, gameVisualizer.getBullets().size());
        Bullet bullet = gameVisualizer.getBullets().get(0);
        assertTrue(bullet.isActive());
    }

    @Test
    public void testBulletEnemyCollision() {
        FastEnemy enemy = new FastEnemy(player.getX() + 50, player.getY() + 50); // Уменьшил расстояние
        gameVisualizer.getEnemies().add(enemy);
        waveManager.setEnemiesAlive(1);

        gameVisualizer.setMousePosition(enemy.getX(), enemy.getY());
        gameVisualizer.exposedShoot();

        // Несколько обновлений, чтобы пуля точно достигла врага
        for (int i = 0; i < 10; i++) {
            gameVisualizer.exposedUpdateBullets();
        }

        assertTrue("Враг должен исчезнуть после попадания",
                gameVisualizer.getEnemies().isEmpty());
        assertTrue("Пуля должна исчезнуть после столкновения",
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

        assertTrue(newDistance < initialDistance);
    }

    @Test
    public void testLevelUpAndUpgrades() {
        player.addXp(player.getXpToNextLevel());
        gameVisualizer.exposedUpdateBullets();
        gameVisualizer.exposedUpdateBullets();

        assertTrue(gameVisualizer.isPaused());
        assertTrue(gameVisualizer.isUpgradeSelectionMode());
        assertEquals(3, gameVisualizer.getOfferedUpgrades().size());
    }

    @Test
    public void testGameReset() {
        player.takeDamage(player.getMaxHealth());
        gameVisualizer.resetGame();

        assertFalse(gameVisualizer.isGameOver());
        assertEquals(GameVisualizer.GameState.START_SCREEN, gameVisualizer.getGameState());
        assertEquals(1000, player.getHealth());
        assertTrue(player.isAlive());
        assertTrue(gameVisualizer.getEnemies().isEmpty());
        assertTrue(gameVisualizer.getBullets().isEmpty());
    }
}
