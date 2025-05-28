package GamePlay;

import gui.Enemies.BasicEnemy;
import gui.Enemies.Enemy;
import gui.GameMechanics.Bullet;
import gui.GameMechanics.Player;
import gui.GameMechanics.Achievement;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class BoundaryTests {

    @Test
    public void testPlayerAtZeroHealth() {
        Player player = new Player(100.0, 100.0, new ArrayList<Achievement>());
        player.takeDamage(1000);
        assertEquals(0, player.getHealth());
        assertFalse(player.isAlive());
    }

    @Test
    public void testPlayerHealthCannotBeNegative() {
        Player player = new Player(100.0, 100.0, new ArrayList<Achievement>());
        player.takeDamage(2000);
        assertEquals(0, player.getHealth());
    }

    @Test
    public void testPlayerCannotRegenerateBeyondMaxHealth() {
        Player player = new Player(100.0, 100.0, new ArrayList<Achievement>());
        player.regenerateHealth(100);
        assertEquals(200, player.getHealth()); // maxHealth is 200
    }

    @Test
    public void testBulletDeactivationAfterCollision() {
        Bullet bullet = new Bullet(100, 100, 200, 200, 10);
        Enemy enemy = new BasicEnemy(100, 100);

        boolean collision = bullet.checkCollision(enemy);
        assertTrue(collision, "Bullet should collide with enemy at same position");
        bullet.deactivate();
        assertFalse(bullet.isActive(), "Bullet should be deactivated after collision");
    }

    @Test
    public void testMultipleLevelUps() {
        Player player = new Player(100.0, 100.0, new ArrayList<Achievement>());
        player.addXp(400); // Enough for 3 levels (100 + 200 + 300)
        assertEquals(3, player.getLevel());
        assertEquals(400 - 100 - 200, player.getXp());
    }
}