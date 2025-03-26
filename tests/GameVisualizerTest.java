import gui.GameVisualizer;
import org.junit.Before;
import org.junit.Test;

import static gui.GameVisualizer.MAP_SIZE;
import static org.junit.Assert.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import static org.junit.jupiter.api.Assertions.*;

public class GameVisualizerTest {
    private GameVisualizer gameVisualizer;

    @Before
    public void setUp() {
        gameVisualizer = new GameVisualizer();
    }

    @Test
    public void testInitialRobotPosition() {
        assertEquals(MAP_SIZE / 2.0, gameVisualizer.robotX, "Робот должен начинаться в центре карты по X");
        assertEquals(MAP_SIZE / 2.0, gameVisualizer.robotY, "Робот должен начинаться в центре карты по Y");
    }

    @Test
    public void testMoveUp() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_W);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotY < MAP_SIZE / 2.0, "Робот должен двигаться вверх");
    }

    @Test
    public void testMoveDown() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_S);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotY > MAP_SIZE / 2.0, "Робот должен двигаться вниз");
    }

    @Test
    public void testMoveLeft() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_A);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotX < MAP_SIZE / 2.0, "Робот должен двигаться влево");
    }

    @Test
    public void testMoveRight() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotX > MAP_SIZE / 2.0, "Робот должен двигаться вправо");
    }

    @Test
    public void testBorderCollisionLeft() {
        gameVisualizer.robotX = GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_A);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(GameVisualizer.BORDER_PADDING, gameVisualizer.robotX, "Робот не должен выходить за левую границу");
    }

    @Test
    public void testBorderCollisionRight() {
        gameVisualizer.robotX = MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING, gameVisualizer.robotX, "Робот не должен выходить за правую границу");
    }

    @Test
    public void testBorderCollisionTop() {
        gameVisualizer.robotY = GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_W);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(GameVisualizer.BORDER_PADDING, gameVisualizer.robotY, "Робот не должен выходить за верхнюю границу");
    }

    @Test
    public void testBorderCollisionBottom() {
        gameVisualizer.robotY = MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_S);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING, gameVisualizer.robotY, "Робот не должен выходить за нижнюю границу");
    }

    @Test
    public void testCameraFollowsRobot() {
        // Create a fresh instance with known dimensions
        GameVisualizer gameVisualizer = new GameVisualizer() {
            @Override
            public Dimension getSize() {
                return new Dimension(800, 600); // Set a fixed size for testing
            }
        };

        // Clear any existing keys and add only the D key
        gameVisualizer.activeKeys.clear();
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);

        // Set initial robot position to center
        gameVisualizer.robotX = MAP_SIZE / 2.0;  // 1600.0

        // Execute the movement
        gameVisualizer.moveRobotAndCamera();

        // Calculate expected offset: robotX - WIDTH/2
        double expectedOffset = gameVisualizer.robotX - 800 / 2.0;  // 1605.0 - 400 = 1205.0

        // Use delta of 0.1 to account for floating-point precision
        assertEquals(expectedOffset, gameVisualizer.offsetX, 0.1,
                "Камера должна следовать за роботом");
    }
}
