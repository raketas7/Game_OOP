import gui.*;
import gui.Enemies.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import static gui.GameVisualizer.MAP_SIZE;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class GameVisualizerTest {
    private TestableGameVisualizer gameVisualizer;
    private Player player;
    private WaveManager waveManager;

    // Класс-наследник для доступа к protected методам
    private static class TestableGameVisualizer extends GameVisualizer {
        public TestableGameVisualizer(ResourceBundle bundle) {
            super(bundle);
        }
        public void exposedMovePlayer() { movePlayer(); }
        public void exposedCheckCollisions() { checkCollisions(); }
        public void exposedUpdateCamera() { updateCamera(); }
        public void exposedUpdateWindowSize() { updateWindowSize(); }
    }

    @Before
    public void setUp() {
        ResourceBundle mockBundle = Mockito.mock(ResourceBundle.class);
        gameVisualizer = new TestableGameVisualizer(mockBundle);
        player = gameVisualizer.getPlayer();
        waveManager = gameVisualizer.getWaveManager();
        gameVisualizer.setSize(800, 600);
    }

    @Test
    public void testInitialPlayerPosition() {
        assertEquals("Игрок должен начинаться в центре карты по X",
                MAP_SIZE / 2.0, player.getX(), 0.01);
        assertEquals("Игрок должен начинаться в центре карты по Y",
                MAP_SIZE / 2.0, player.getY(), 0.01);
    }

    @Test
    public void testMoveUp() {
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_W);
        gameVisualizer.exposedMovePlayer();
        assertTrue("Робот должен двигаться вверх", player.getY() < MAP_SIZE / 2.0);
    }

    @Test
    public void testMoveDown() {
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_S);
        gameVisualizer.exposedMovePlayer();
        assertTrue("Робот должен двигаться вниз", player.getY() > MAP_SIZE / 2.0);
    }

    @Test
    public void testMoveLeft() {
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_A);
        gameVisualizer.exposedMovePlayer();
        assertTrue("Робот должен двигаться влево", player.getX() < MAP_SIZE / 2.0);
    }

    @Test
    public void testMoveRight() {
        gameVisualizer.getActiveKeys().add(KeyEvent.VK_D);
        gameVisualizer.exposedMovePlayer();
        assertTrue("Робот должен двигаться вправо", player.getX() > MAP_SIZE / 2.0);
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
        // Устанавливаем начальное количество врагов
        waveManager.setEnemiesAlive(1); // Устанавливаем 1, так как будем добавлять одного врага

        // Создаем врага в той же позиции, что и игрок
        BasicEnemy enemy = new BasicEnemy(player.getX(), player.getY());
        gameVisualizer.getEnemies().add(enemy);

        // Обновляем смещение камеры (чтобы collision detection работал правильно)
        gameVisualizer.exposedUpdateCamera();

        // Вызываем проверку коллизий
        gameVisualizer.exposedCheckCollisions();

        // Проверяем результаты
        assertThat(gameVisualizer.getEnemies().size(),
                anyOf(is(4), is(5)));
        assertEquals("Счетчик живых врагов должен уменьшиться",
                0, waveManager.getEnemiesAlive());
    }


    @Test
    public void testWaveSpawning() {
        waveManager.startNextWave();
        assertEquals(1, waveManager.getCurrentWave());
        assertEquals(70, waveManager.getPointsAvailable());

        List<Enemy> enemies = waveManager.spawnEnemies(
                player.getX(), player.getY(), MAP_SIZE, Collections.emptyList());

        assertFalse("Должны появиться враги", enemies.isEmpty());
        assertTrue("Количество врагов должно соответствовать бюджету",
                enemies.size() >= 3 && enemies.size() <= 7);
    }
}
