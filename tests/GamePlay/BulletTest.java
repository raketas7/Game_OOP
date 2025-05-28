package GamePlay;

import gui.Enemies.Enemy;
import gui.Enemies.BasicEnemy;
import gui.GameMechanics.Bullet;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BulletTest {

    @Test
    public void testBulletCreation() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10);
        assertTrue(bullet.isActive(), "Bullet should be active upon creation");
        assertEquals(100, bullet.getX(), "Bullet X position should be 100");
        assertEquals(100, bullet.getY(), "Bullet Y position should be 100");
        assertEquals(10, bullet.getDamage(), "Bullet damage should be 10");
    }

    @Test
    public void testBulletMovement() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10);
        double initialX = bullet.getX();
        double initialY = bullet.getY();

        bullet.update();

        assertNotEquals(initialX, bullet.getX(), "Bullet X position should change after update");
        assertNotEquals(initialY, bullet.getY(), "Bullet Y position should change after update");
    }

    @Test
    public void testBulletLifetime() {
        long startTime = 1000L; // Arbitrary start time
        final long[] currentTime = {startTime}; // Mutable time for lambda
        Bullet bullet = new Bullet(100, 100, 200, 200, 10, () -> currentTime[0]);

        // Simulate passage of time beyond LIFETIME (5000 ms)
        for (int i = 0; i < 300; i++) {
            bullet.update();
            currentTime[0] += 20; // Advance time by 20ms per frame
        }
        assertFalse(bullet.isActive(), "Bullet should be inactive after exceeding lifetime");
    }

    @Test
    public void testCollisionDetection() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10);
        Enemy enemy = new BasicEnemy(110, 110);

        assertTrue(bullet.checkCollision(enemy), "Bullet should collide with enemy at close position");
    }

    @Test
    public void testNoCollision() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10);
        Enemy enemy = new BasicEnemy(300, 300);

        assertFalse(bullet.checkCollision(enemy), "Bullet should not collide with enemy far away");
    }
}