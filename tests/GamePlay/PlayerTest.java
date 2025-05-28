package GamePlay;

import gui.Visuals.GameVisualizer;
import gui.GameMechanics.Bullet;
import gui.GameMechanics.Player;
import gui.GameMechanics.UpgradeType;
import gui.GameMechanics.Achievement;
import org.junit.Before;
import org.junit.Test;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Player player;
    private static final int MAP_SIZE = 1000;

    @Before
    public void setUp() {
        player = new Player(500.0, 500.0, new ArrayList<Achievement>());
    }

    @Test
    public void testMove() {
        player.move(10, 0, MAP_SIZE);
        assertEquals(510, player.getX());
        assertEquals(500, player.getY());

        // Диагональное движение с нормализацией
        player.move(10, 10, MAP_SIZE);
        assertTrue(player.getX() > 510);
        assertTrue(player.getY() > 500);

        // Границы карты
        player.move(-1000, -1000, MAP_SIZE);
        assertEquals(GameVisualizer.BORDER_PADDING, player.getX());
        assertEquals(GameVisualizer.BORDER_PADDING, player.getY());
    }

    @Test
    public void testCalculateNormalizedSpeed() {
        assertEquals(1.0, Player.calculateNormalizedSpeed(5, 0), 0.0001);
        double expectedDiagonalFactor = Math.sqrt(2) / 2;
        assertEquals(expectedDiagonalFactor, Player.calculateNormalizedSpeed(5, 5), 0.0001);
    }

    @Test
    public void testGetBounds() {
        Rectangle bounds = player.getBounds(100, 100);
        assertEquals(400, bounds.x);
        assertEquals(400, bounds.y);
        assertEquals(Player.SIZE, bounds.width);
        assertEquals(Player.SIZE, bounds.height);
    }

    @Test
    public void testPlayerInitialization() {
        assertEquals(500, player.getX());
        assertEquals(500, player.getY());
        assertEquals(200, player.getHealth());
        assertEquals(200, player.getMaxHealth());
        assertEquals(1, player.getLevel());
        assertEquals(0, player.getXp());
        assertEquals(100, player.getXpToNextLevel());
    }

    @Test
    public void testPlayerShooting() {
        List<Bullet> bullets = player.shoot(600, 600);
        assertEquals(1, bullets.size());
        Bullet bullet = bullets.get(0);
        assertEquals(500 + Player.SIZE/2.0, bullet.getX());
        assertEquals(500 + Player.SIZE/2.0, bullet.getY());
    }

    @Test
    public void testPlayerTakingDamage() {
        player.takeDamage(100);
        assertEquals(100, player.getHealth());
    }

    @Test
    public void testPlayerDeath() {
        player.takeDamage(1000);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    @Test
    public void testPlayerRegeneration() {
        player.takeDamage(100);
        player.regenerateHealth(50);
        assertEquals(150, player.getHealth());
    }

    @Test
    public void testXpGainAndLevelUp() {
        player.addXp(100);
        assertEquals(2, player.getLevel());
        assertEquals(0, player.getXp());
        assertEquals(200, player.getXpToNextLevel());
    }

    @Test
    public void testUpgradeSystem() {
        int initialDamage = player.getBulletDamage();
        player.applyUpgrade(UpgradeType.DAMAGE);
        assertEquals(initialDamage + 5, player.getBulletDamage());
    }

    @Test
    public void testUpgradeOptions() {
        List<UpgradeType> upgrades = player.getUpgradeOptions();
        assertEquals(3, upgrades.size());
    }
}