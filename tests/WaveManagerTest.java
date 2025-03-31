import gui.Enemies.*;
import gui.GameVisualizer;
import gui.Player;
import gui.WaveManager;
import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class WaveManagerTest {
    private WaveManager waveManager;

    @Before
    public void setUp() {
        waveManager = new WaveManager();
    }

    @Test
    public void testStartNextWave() {
        assertEquals(0, waveManager.getCurrentWave());
        waveManager.startNextWave();
        assertEquals(1, waveManager.getCurrentWave());
        assertEquals(70, waveManager.getPointsAvailable()); // 50 + 1*20
    }

    @Test
    public void testShouldSpawnWave() {
        // Нет врагов
        assertTrue(waveManager.shouldSpawnWave());
        waveManager.startNextWave();

        // Есть живые враги, но кд не прошел
        waveManager.setEnemiesAlive(1);
        assertFalse(waveManager.shouldSpawnWave());

        // Прошло время кд, даже если есть живые враги
        waveManager.startNextWave();
        try {
            Thread.sleep(WaveManager.getWaveCooldown() + 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(waveManager.shouldSpawnWave());

        // Нет врагов, и кд прошел
        waveManager.setEnemiesAlive(0);
        assertTrue(waveManager.shouldSpawnWave());
    }

    @Test
    public void testEnemyDied() {
        waveManager.setEnemiesAlive(3);
        waveManager.enemyDied();
        assertEquals(2, waveManager.getEnemiesAlive());
    }

    @Test
    public void testSpawnEnemies() {
        waveManager.startNextWave();
        List<Enemy> enemies = waveManager.spawnEnemies(100, 100, 1000, List.of());

        assertFalse(enemies.isEmpty());
        assertTrue(waveManager.getPointsAvailable() < 70);

        // Проверяем типы созданных врагов
        for (Enemy enemy : enemies) {
            assertTrue(enemy instanceof BasicEnemy ||
                    enemy instanceof FastEnemy ||
                    enemy instanceof TankEnemy);
        }
    }

    @Test
    public void testFindFreePosition() {
        waveManager.startNextWave();

        // Создаем врага в центре, чтобы spawnEnemies искал свободную позицию вокруг
        List<Enemy> existingEnemies = List.of(new BasicEnemy(100, 100));

        List<Enemy> newEnemies = waveManager.spawnEnemies(100, 100, 1000, existingEnemies);

        // Проверяем, что новые враги появились и их позиции не пересекаются
        assertFalse(newEnemies.isEmpty());
        for (Enemy enemy : newEnemies) {
            assertTrue(enemy.getX() >= GameVisualizer.BORDER_PADDING);
            assertTrue(enemy.getY() >= GameVisualizer.BORDER_PADDING);
            assertTrue(enemy.getX() <= 1000 - Player.SIZE - GameVisualizer.BORDER_PADDING);
            assertTrue(enemy.getY() <= 1000 - Player.SIZE - GameVisualizer.BORDER_PADDING);

            // Проверяем, что нет пересечений с существующими врагами
            for (Enemy existing : existingEnemies) {
                assertFalse(enemy.getCollisionBounds().intersects(existing.getCollisionBounds()));
            }
        }
    }

    @Test
    public void testEnemyCost() {
        waveManager.startNextWave();

        List<Enemy> enemies = waveManager.spawnEnemies(100, 100, 1000, List.of());

        // Проверяем, что потрачены pointsAvailable
        assertTrue(waveManager.getPointsAvailable() < 70);

        // Проверяем, что созданы враги соответствующих типов
        for (Enemy enemy : enemies) {
            assertTrue(enemy instanceof BasicEnemy ||
                    enemy instanceof FastEnemy ||
                    enemy instanceof TankEnemy);
        }
    }
}