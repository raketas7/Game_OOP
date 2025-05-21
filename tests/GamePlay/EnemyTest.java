package GamePlay;

import gui.Enemies.BasicEnemy;
import gui.Enemies.Enemy;
import gui.Enemies.FastEnemy;
import gui.Enemies.TankEnemy;
import gui.PlayerMechanics.Player;
import org.junit.Before;
import org.junit.Test;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EnemyTest {
    private BasicEnemy basicEnemy;
    private FastEnemy fastEnemy;
    private TankEnemy tankEnemy;

    @Before
    public void setUp() {
        basicEnemy = new BasicEnemy(100, 100);
        fastEnemy = new FastEnemy(200, 200);
        tankEnemy = new TankEnemy(300, 300);
    }

    @Test
    public void testEnemyMovement() {
        double initialX = basicEnemy.getX();
        double initialY = basicEnemy.getY();
        basicEnemy.move(150, 150, List.of());
        assertTrue(basicEnemy.getX() > initialX);
        assertTrue(basicEnemy.getY() > initialY);
    }

    @Test
    public void testEnemyCollisionAvoidance() {
        BasicEnemy enemy1 = new BasicEnemy(100, 100);
        BasicEnemy enemy2 = new BasicEnemy(105, 105);
        List<Enemy> twoEnemies = new ArrayList<>();
        twoEnemies.add(enemy1);
        twoEnemies.add(enemy2);

        double initialX1 = enemy1.getX();
        double initialY1 = enemy1.getY();
        enemy1.move(200, 200, twoEnemies);
        assertNotEquals(initialX1, enemy1.getX());
        assertNotEquals(initialY1, enemy1.getY());
    }

    @Test
    public void testGetCollisionBounds() {
        Rectangle bounds = basicEnemy.getCollisionBounds();
        assertEquals(85, bounds.x);
        assertEquals(85, bounds.y);
        assertEquals(30, bounds.width);
        assertEquals(30, bounds.height);
    }

    @Test
    public void testEnemyTypes() {
        assertEquals(30, basicEnemy.getSize());
        assertEquals(25, fastEnemy.getSize());
        assertEquals(40, tankEnemy.getSize());

        assertEquals(Color.BLACK, basicEnemy.getColor());
        assertEquals(Color.BLUE, fastEnemy.getColor());
        assertEquals(Color.GREEN, tankEnemy.getColor());
    }

    @Test
    public void testDraw() {
        Graphics2D g2d = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        basicEnemy.draw(g2d, 0, 0);
        fastEnemy.draw(g2d, 0, 0);
        tankEnemy.draw(g2d, 0, 0);
        g2d.dispose();
    }

    @Test
    public void testEnemyInitialization() {
        assertEquals(100, basicEnemy.getX());
        assertEquals(100, basicEnemy.getY());
        assertTrue(basicEnemy.isAlive());
    }

    @Test
    public void testEnemyTakingDamage() {
        basicEnemy.takeDamage(10);
        assertEquals(10, basicEnemy.getHealth());
    }

    @Test
    public void testEnemyDeath() {
        basicEnemy.takeDamage(30);
        assertFalse(basicEnemy.isAlive());
    }

    @Test
    public void testPlayerEnemyCollision() {
        Player player = new Player(100, 100);
        Rectangle playerBounds = new Rectangle((int)player.getX(), (int)player.getY(), Player.SIZE, Player.SIZE);
        Rectangle enemyBounds = basicEnemy.getBounds(0, 0);
        assertTrue(playerBounds.intersects(enemyBounds));
    }
}