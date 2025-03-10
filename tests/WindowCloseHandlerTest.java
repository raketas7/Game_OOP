import gui.WindowCloseHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import javax.swing.event.InternalFrameEvent;

import javax.swing.*;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

// проверка вызова окна подтверждения при закрытии и корректности реакции
public class WindowCloseHandlerTest {

    private JInternalFrame mockFrame;
    private WindowCloseHandler closeHandler;

    @Before
    public void setUp() {
        ResourceBundle mockBundle = Mockito.mock(ResourceBundle.class);
        mockFrame = Mockito.mock(JInternalFrame.class);

        when(mockBundle.getString("confirmExit")).thenReturn("Вы уверены, что хотите закрыть это окно?");
        when(mockBundle.getString("confirmClose")).thenReturn("Подтверждение закрытия");

        closeHandler = new WindowCloseHandler(mockBundle);
    }

    // тесты закрытия и не закрытия окна
    @Test
    public void testInternalFrameClosing_YesOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.YES_OPTION);

            closeHandler.internalFrameClosing(new InternalFrameEvent(mockFrame, InternalFrameEvent.INTERNAL_FRAME_CLOSING));

            verify(mockFrame, times(1)).dispose();
        }
    }

    @Test
    public void testInternalFrameClosing_NoOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.NO_OPTION);

            closeHandler.internalFrameClosing(new InternalFrameEvent(mockFrame, InternalFrameEvent.INTERNAL_FRAME_CLOSING));

            verify(mockFrame, never()).dispose();
        }
    }

    // тесты корректности возвращаемых значений метода confirmClose
    @Test
    public void testConfirmClose_YesOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.YES_OPTION);

            boolean result = closeHandler.confirmClose(mockFrame);

            assertTrue(result);
        }
    }

    @Test
    public void testConfirmClose_NoOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showConfirmDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION)
            )).thenReturn(JOptionPane.NO_OPTION);

            boolean result = closeHandler.confirmClose(mockFrame);

            assertFalse(result);
        }
    }
}