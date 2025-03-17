import gui.GameVisualizer;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
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
        assertEquals(GameVisualizer.MAP_SIZE / 2.0, gameVisualizer.robotX, "Робот должен начинаться в центре карты по X");
        assertEquals(GameVisualizer.MAP_SIZE / 2.0, gameVisualizer.robotY, "Робот должен начинаться в центре карты по Y");
    }

    @Test
    public void testMoveUp() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_W);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotY < GameVisualizer.MAP_SIZE / 2.0, "Робот должен двигаться вверх");
    }

    @Test
    public void testMoveDown() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_S);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotY > GameVisualizer.MAP_SIZE / 2.0, "Робот должен двигаться вниз");
    }

    @Test
    public void testMoveLeft() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_A);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotX < GameVisualizer.MAP_SIZE / 2.0, "Робот должен двигаться влево");
    }

    @Test
    public void testMoveRight() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);
        gameVisualizer.moveRobotAndCamera();
        assertTrue(gameVisualizer.robotX > GameVisualizer.MAP_SIZE / 2.0, "Робот должен двигаться вправо");
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
        gameVisualizer.robotX = GameVisualizer.MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(GameVisualizer.MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING, gameVisualizer.robotX, "Робот не должен выходить за правую границу");
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
        gameVisualizer.robotY = GameVisualizer.MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING;
        gameVisualizer.activeKeys.add(KeyEvent.VK_S);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(GameVisualizer.MAP_SIZE - GameVisualizer.ROBOT_SIZE - GameVisualizer.BORDER_PADDING, gameVisualizer.robotY, "Робот не должен выходить за нижнюю границу");
    }

    @Test
    public void testCameraFollowsRobot() {
        gameVisualizer.activeKeys.add(KeyEvent.VK_D);
        gameVisualizer.moveRobotAndCamera();
        assertEquals(gameVisualizer.robotX - GameVisualizer.WINDOW_WIDTH / 2.0, gameVisualizer.offsetX, "Камера должна следовать за роботом");
    }
}
