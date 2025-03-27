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
        GameVisualizer visualizer = new GameVisualizer();

        visualizer.robotX = GameVisualizer.MAP_SIZE / 2.0;
        visualizer.robotY = GameVisualizer.MAP_SIZE / 2.0;
        visualizer.updateCameraOffset();

        double initialOffsetX = visualizer.offsetX;
        double initialOffsetY = visualizer.offsetY;

        // Двигаем робота вправо и вниз
        visualizer.robotX += 100;
        visualizer.robotY += 100;
        visualizer.updateCameraOffset();

        // Проверяем, что смещение камеры изменилось
        assertTrue(visualizer.offsetX > initialOffsetX, "Камера должна сместиться вправо");
        assertTrue(visualizer.offsetY > initialOffsetY, "Камера должна сместиться вниз");

        // Проверяем, что камера не выходит за границы карты
        assertTrue(visualizer.offsetX >= 0 && visualizer.offsetX <= GameVisualizer.MAP_SIZE - visualizer.getWindowWidth(),
                "Камера должна оставаться в пределах карты по X");
        assertTrue(visualizer.offsetY >= 0 && visualizer.offsetY <= GameVisualizer.MAP_SIZE - visualizer.getWindowHeight(),
                "Камера должна оставаться в пределах карты по Y");
    }

}
