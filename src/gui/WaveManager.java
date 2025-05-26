package gui;

import gui.Enemies.*;
import gui.PlayerMechanics.Player;

import java.util.*;
import java.awt.Rectangle;

public class WaveManager {
    private int currentWave = 0;
    private int pointsAvailable;
    private int enemiesAlive = 0;
    private long lastSpawnTime;
    private static final long WAVE_COOLDOWN = 15000;

    public int getCurrentWave() { return currentWave; }
    public int getPointsAvailable() { return pointsAvailable; }
    public int getEnemiesAlive() { return enemiesAlive; }
    public static long getWaveCooldown() { return WAVE_COOLDOWN; }
    public void setEnemiesAlive(int i) { this.enemiesAlive = i; }

    private static class EnemyCost {
        Class<? extends Enemy> enemyClass;
        int cost;

        EnemyCost(Class<? extends Enemy> enemyClass, int cost) {
            this.enemyClass = enemyClass;
            this.cost = cost;
        }
    }

    private final List<EnemyCost> enemyTypes = new ArrayList<>();
    private final Random random = new Random();

    public WaveManager() {
        enemyTypes.add(new EnemyCost(BasicEnemy.class, 10));
        enemyTypes.add(new EnemyCost(FastEnemy.class, 15));
        enemyTypes.add(new EnemyCost(TankEnemy.class, 20));
    }

    public void startNextWave() {
        currentWave++;
        pointsAvailable = 50 + currentWave * 20;
        lastSpawnTime = System.currentTimeMillis();
    }

    public boolean shouldSpawnWave() {
        return enemiesAlive == 0 ||
                System.currentTimeMillis() - lastSpawnTime > WAVE_COOLDOWN;
    }

    public void enemyDied() {
        enemiesAlive--;
    }

    public List<Enemy> spawnEnemies(double playerX, double playerY, int mapSize, List<Enemy> existingEnemies) {
        List<Enemy> newEnemies = new ArrayList<>();

        while (pointsAvailable > 0) {
            EnemyCost randomEnemyType = getRandomAffordableEnemy();
            if (randomEnemyType == null) break;

            double[] position = findFreePosition(playerX, playerY, mapSize, existingEnemies, newEnemies);
            if (position == null) break;

            pointsAvailable -= randomEnemyType.cost;
            enemiesAlive++;

            try {
                Enemy enemy = randomEnemyType.enemyClass
                        .getConstructor(double.class, double.class)
                        .newInstance(position[0], position[1]);
                newEnemies.add(enemy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return newEnemies;
    }

    private double[] findFreePosition(double playerX, double playerY, int mapSize,
                                      List<Enemy> existingEnemies, List<Enemy> newEnemies) {
        int attempts = 0;
        while (attempts < 100) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = GameVisualizer.ENEMY_SPAWN_RADIUS + random.nextDouble() * 300;
            double spawnX = playerX + Math.cos(angle) * distance;
            double spawnY = playerY + Math.sin(angle) * distance;

            spawnX = Math.max(GameVisualizer.BORDER_PADDING,
                    Math.min(spawnX, mapSize - Player.SIZE - GameVisualizer.BORDER_PADDING));
            spawnY = Math.max(GameVisualizer.BORDER_PADDING,
                    Math.min(spawnY, mapSize - Player.SIZE - GameVisualizer.BORDER_PADDING));

            if (isPositionFree(spawnX, spawnY, existingEnemies, newEnemies)) {
                return new double[]{spawnX, spawnY};
            }
            attempts++;
        }
        return null;
    }

    private boolean isPositionFree(double x, double y, List<Enemy> existingEnemies, List<Enemy> newEnemies) {
        Rectangle newEnemyBounds = new Rectangle(
                (int)(x - (double) Player.SIZE / 2),
                (int)(y - (double) Player.SIZE / 2),
                Player.SIZE,
                Player.SIZE
        );

        for (Enemy enemy : existingEnemies) {
            if (newEnemyBounds.intersects(enemy.getCollisionBounds())) {
                return false;
            }
        }

        for (Enemy enemy : newEnemies) {
            if (newEnemyBounds.intersects(enemy.getCollisionBounds())) {
                return false;
            }
        }

        return true;
    }

    private EnemyCost getRandomAffordableEnemy() {
        List<EnemyCost> affordableEnemies = new ArrayList<>();
        for (EnemyCost ec : enemyTypes) {
            if (ec.cost <= pointsAvailable) {
                affordableEnemies.add(ec);
            }
        }
        return affordableEnemies.isEmpty() ? null :
                affordableEnemies.get(random.nextInt(affordableEnemies.size()));
    }

    public void reset() {
        currentWave = 0;
        pointsAvailable = 0;
        enemiesAlive = 0;
        lastSpawnTime = 0;
    }
}