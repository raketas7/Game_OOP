import gui.GameVisualizer;
import gui.PlayerMechanics.Player;
import org.junit.Before;
import org.junit.Test;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private Player player;
    private static final int MAP_SIZE = 1000;

    @Before
    public void setUp() {
        player = new Player(500, 500);
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
        // Движение по одной оси (нормализация = 1.0)
        assertEquals(1.0, Player.calculateNormalizedSpeed(5, 0), 0.0001,
                "Нормализация для движения по одной оси должна быть 1.0");

        // Диагональное движение (нормализация = sqrt(2)/2)
        double expectedDiagonalFactor = Math.sqrt(2) / 2; // ≈ 0.707
        assertEquals(expectedDiagonalFactor, Player.calculateNormalizedSpeed(5, 5), 0.0001,
                "Нормализация для диагонального движения должна быть sqrt(2)/2");
    }

    @Test
    public void testGetBounds() {
        Rectangle bounds = player.getBounds(100, 100);
        assertEquals(400, bounds.x);
        assertEquals(400, bounds.y);
        assertEquals(Player.SIZE, bounds.width);
        assertEquals(Player.SIZE, bounds.height);
    }
}