package GamePlay;

import gui.Enemies.BasicEnemy;
import gui.Enemies.Enemy;
import gui.PlayerMechanics.Bullet;
import gui.PlayerMechanics.Player;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoundaryTests {

    @Test
    public void testPlayerAtZeroHealth() {
        Player player = new Player(100, 100);
        player.takeDamage(1000);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    @Test
    public void testPlayerHealthCannotBeNegative() {
        Player player = new Player(100, 100);
        player.takeDamage(2000);
        assertEquals(0, player.getHealth());
    }

    @Test
    public void testPlayerCannotRegenerateBeyondMaxHealth() {
        Player player = new Player(100, 100);
        player.regenerateHealth(100);
        assertEquals(1000, player.getHealth());
    }

    @Test
    public void testBulletDeactivationAfterCollision() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10) {
            @Override
            protected long getCurrentTime() {
                return 0;
            }
        };
        Enemy enemy = new BasicEnemy(100, 100);

        boolean collision = bullet.checkCollision(enemy);
        assertTrue(collision);
        bullet.deactivate();
        assertFalse(bullet.isActive());
    }

    @Test
    public void testMultipleLevelUps() {
        Player player = new Player(100, 100);
        player.addXp(400); // Достаточно для 3 уровней (100 + 200 + 300)
        assertEquals(3, player.getLevel());
        assertEquals(400 - 100 - 200, player.getXp());
    }
}