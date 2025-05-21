package GamePlay;

import gui.Enemies.Enemy;
import gui.Enemies.BasicEnemy;
import gui.PlayerMechanics.Bullet;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BulletTest {

    @Test
    public void testBulletCreation() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return 0;
            }
        };
        assertTrue(bullet.isActive());
        assertEquals(100, bullet.getX());
        assertEquals(100, bullet.getY());
        assertEquals(10, bullet.getDamage());
    }

    @Test
    public void testBulletMovement() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return 0;
            }
        };
        double initialX = bullet.getX();
        double initialY = bullet.getY();

        bullet.update();

        assertNotEquals(initialX, bullet.getX());
        assertNotEquals(initialY, bullet.getY());
    }

    @Test
    public void testBulletLifetime() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return LIFETIME + 1;
            }
        };

        bullet.update();
        assertTrue(bullet.isActive());
    }

    @Test
    public void testCollisionDetection() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return 0;
            }
        };
        Enemy enemy = new BasicEnemy(110, 110);

        assertTrue(bullet.checkCollision(enemy));
    }

    @Test
    public void testNoCollision() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return 0;
            }
        };
        Enemy enemy = new BasicEnemy(300, 300);

        assertFalse(bullet.checkCollision(enemy));
    }
}