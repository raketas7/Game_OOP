import gui.GameVisualizer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameVisualizerTest {

    private TestableGameVisualizer gameVisualizer;

    @Before
    public void setUp() {
        gameVisualizer = new TestableGameVisualizer();
        gameVisualizer.setSize(400, 400);
    }

    @Test
    public void testRobotTeleportation() {
        // за левую
        testTeleportation(-1, 100, 400, 100);

        // за правую
        testTeleportation(401, 100, 0, 100);

        // за верхнюю
        testTeleportation(100, -1, 100, 400);

        // за нижнюю
        testTeleportation(100, 401, 100, 0);
    }

    private void testTeleportation(double startX, double startY, double expectedX, double expectedY) {
        gameVisualizer.setRobotPosition(startX, startY);
        gameVisualizer.moveRobot(0, 0, 0);
        assertEquals(expectedX, gameVisualizer.getRobotPositionX(), 0.001);
        assertEquals(expectedY, gameVisualizer.getRobotPositionY(), 0.001);
    }

    private static class TestableGameVisualizer extends GameVisualizer {
        @Override
        protected void moveRobot(double velocity, double angularVelocity, double duration) {
            super.moveRobot(velocity, angularVelocity, duration);
        }
    }
}
