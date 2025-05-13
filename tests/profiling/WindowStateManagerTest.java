package profiling;

import gui.MainApplicationFrame;
import gui.profiling.WindowStateManager;
import log.LogWindowSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WindowStateManagerTest {
    private JInternalFrame testWindow;
    private Rectangle screenBounds;
    private MainApplicationFrame mockFrame;
    private MockedStatic<JOptionPane> mockedOptionPane;

    @Mock
    private ResourceBundle bundle;

    @Mock
    private LogWindowSource logSource;

    @Before
    public void setUp() {
        testWindow = new JInternalFrame("Test Window", true, true, true, true);
        testWindow.setBounds(100, 100, 300, 400);
        screenBounds = new Rectangle(0, 0, 1024, 768);

        Map<String, JInternalFrame> internalWindows = new HashMap<>();
        internalWindows.put("testWindow", testWindow);

        mockedOptionPane = mockStatic(JOptionPane.class);
        mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                any(Component.class),
                any(String.class),
                any(String.class),
                anyInt()
        )).thenReturn(JOptionPane.YES_OPTION);

        mockedOptionPane.when(() -> JOptionPane.showInputDialog(
                any(Component.class),
                any(Object.class),
                any(String.class),
                anyInt()
        )).thenReturn("test");

        mockedOptionPane.when(() -> JOptionPane.showMessageDialog(
                any(Component.class),
                any(Object.class)
        )).thenAnswer(invocation -> null);

        mockFrame = mock(MainApplicationFrame.class);
        when(mockFrame.getInternalWindows()).thenReturn(internalWindows);
        when(mockFrame.getContentPane()).thenReturn(new JDesktopPane());
    }

    @After
    public void tearDown() {
        if (mockedOptionPane != null) {
            mockedOptionPane.close();
        }
    }

    @Test
    public void positionWindow_ShouldKeepWindowWithinScreenBounds() {
        WindowStateManager.positionWindow(testWindow, screenBounds, 1000, 700, 200, 200);
        assertTrue("Window X position is out of bounds",
                testWindow.getX() >= 0 && testWindow.getX() + testWindow.getWidth() <= screenBounds.width - 50);
        assertTrue("Window Y position is out of bounds",
                testWindow.getY() >= 0 && testWindow.getY() + testWindow.getHeight() <= screenBounds.height - 50);
    }

    @Test
    public void positionWindow_ShouldHandleNegativeCoordinates() {
        WindowStateManager.positionWindow(testWindow, screenBounds, -100, -100, 200, 200);
        assertEquals("Window X should be 0 for negative coordinates", 0, testWindow.getX());
        assertEquals("Window Y should be 0 for negative coordinates", 0, testWindow.getY());
    }

    @Test
    public void getWindowState_ShouldCaptureCorrectBounds() throws PropertyVetoException {
        Map<String, Object> state = WindowStateManager.getWindowState(testWindow);
        assertEquals(100, state.get("x"));
        assertEquals(100, state.get("y"));
        assertEquals(300, state.get("width"));
        assertEquals(400, state.get("height"));
        assertFalse((Boolean) state.get("maximized"));
        assertFalse((Boolean) state.get("iconified"));
    }

    @Test
    public void getWindowState_ShouldHandleMaximizedWindow() {
        try {
            testWindow.setMaximum(true);
            Map<String, Object> state = WindowStateManager.getWindowState(testWindow);
            assertTrue((Boolean) state.get("maximized"));
            assertEquals(100, state.get("x"));
            assertEquals(100, state.get("y"));
        } catch (PropertyVetoException e) {
            fail("Maximization was vetoed");
        }
    }

    @Test
    public void getWindowState_ShouldHandleIconifiedWindow() {
        try {
            testWindow.setIcon(true);
            Map<String, Object> state = WindowStateManager.getWindowState(testWindow);
            assertTrue((Boolean) state.get("iconified"));
            assertEquals(100, state.get("x"));
            assertEquals(100, state.get("y"));
        } catch (PropertyVetoException e) {
            fail("Iconification was vetoed");
        }
    }

    @Test
    public void restoreWindowState_ShouldApplyCorrectBounds() {
        Map<String, Object> state = new HashMap<>();
        state.put("x", 200);
        state.put("y", 200);
        state.put("width", 400);
        state.put("height", 500);
        state.put("maximized", false);
        state.put("iconified", false);
        state.put("visible", true);

        WindowStateManager.restoreWindowState(mockFrame, state, screenBounds, "testWindow");
        assertEquals(200, testWindow.getX());
        assertEquals(200, testWindow.getY());
        assertEquals(400, testWindow.getWidth());
        assertEquals(500, testWindow.getHeight());
        assertFalse(testWindow.isMaximum());
        assertFalse(testWindow.isIcon());
    }

    @Test
    public void restoreWindowState_ShouldHandleMaximizedState() {
        Map<String, Object> state = new HashMap<>();
        state.put("maximized", true);
        state.put("x", 100);
        state.put("y", 100);
        state.put("width", 300);
        state.put("height", 400);
        state.put("visible", true);

        WindowStateManager.restoreWindowState(mockFrame, state, screenBounds, "testWindow");
        assertTrue(testWindow.isMaximum());
    }

    @Test
    public void resetWindowState_ShouldResetSpecialStates() {
        try {
            testWindow.setMaximum(true);
            testWindow.setIcon(true);
            WindowStateManager.resetWindowState(testWindow);
            assertFalse(testWindow.isMaximum());
            assertFalse(testWindow.isIcon());
        } catch (PropertyVetoException e) {
            fail("State reset was vetoed");
        }
    }
}