import gui.LogWindow;
import log.LogWindowSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class LogWindowTest {

    private LogWindow logWindow;

    @Before
    public void setUp() {
        LogWindowSource mockLogSource = Mockito.mock(LogWindowSource.class);
        logWindow = new LogWindow(mockLogSource);
    }

    @Test
    public void testConfirmAndClose() throws Exception {
        Method method = logWindow.getClass().getDeclaredMethod("confirmAndClose");
        method.setAccessible(true);
        logWindow.setVisible(true);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    logWindow,
                    "Вы уверены, что хотите закрыть это окно?",
                    "Подтверждение закрытия",
                    JOptionPane.YES_NO_OPTION
            )).thenReturn(JOptionPane.YES_OPTION);

            method.invoke(logWindow);

            assertFalse(logWindow.isVisible(), "Окно должно быть закрыто.");
        }
    }

    @Test
    public void testConfirmAndClose_NoOption() throws Exception {
        Method method = logWindow.getClass().getDeclaredMethod("confirmAndClose");
        method.setAccessible(true);
        logWindow.setVisible(true);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    logWindow,
                    "Вы уверены, что хотите закрыть это окно?",
                    "Подтверждение закрытия",
                    JOptionPane.YES_NO_OPTION
            )).thenReturn(JOptionPane.NO_OPTION);

            method.invoke(logWindow);

            assertTrue(logWindow.isVisible(), "Окно не должно быть закрыто.");
        }
    }
}