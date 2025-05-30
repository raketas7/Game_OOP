package GamePlay;

import gui.GameMechanics.Achievement;
import org.junit.Before;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AchievementTest {
    private Achievement achievement;

    @Before
    public void setUp() {
        achievement = new Achievement("Test Achievement", "Test Description", 10, 5);
    }

    @Test
    public void testInitialState() {
        assertFalse(achievement.isUnlocked());
        assertEquals("Test Achievement", achievement.getName());
        assertEquals("Test Description", achievement.getDescription());
        assertEquals(10, achievement.getTargetKills());
        assertEquals(5, achievement.getDamageBonus());
    }

    @Test
    public void testUpdateStatus_NotUnlocked() {
        achievement.updateStatus(5);
        assertFalse(achievement.isUnlocked());
    }

    @Test
    public void testUpdateStatus_Unlocked() {
        achievement.updateStatus(10);
        assertTrue(achievement.isUnlocked());
    }

    @Test
    public void testUpdateStatus_ExceedTarget() {
        achievement.updateStatus(15);
        assertTrue(achievement.isUnlocked());
    }

    @Test
    public void testReset() {
        achievement.updateStatus(10);
        assertTrue(achievement.isUnlocked());
        achievement.reset();
        assertFalse(achievement.isUnlocked());
    }

    @Test
    public void testDamageBonus() {
        assertEquals(5, achievement.getDamageBonus());
    }
}