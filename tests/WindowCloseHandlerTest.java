import gui.windows.WindowCloseHandler;
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

        when(mockBundle.getString("confirmCloseWindow")).thenReturn("Вы уверены, что хотите закрыть это окно?");
        when(mockBundle.getString("confirmCloseTitle")).thenReturn("Подтверждение закрытия");
        when(mockBundle.getString("yesButtonText")).thenReturn("Да");
        when(mockBundle.getString("noButtonText")).thenReturn("Нет");

        closeHandler = new WindowCloseHandler(mockBundle);
    }

    @Test
    public void testInternalFrameClosing_YesOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION),
                    eq(JOptionPane.QUESTION_MESSAGE),
                    isNull(),
                    any(),
                    eq("Нет")
            )).thenReturn(JOptionPane.YES_OPTION);

            closeHandler.internalFrameClosing(new InternalFrameEvent(mockFrame, InternalFrameEvent.INTERNAL_FRAME_CLOSING));

            verify(mockFrame, times(1)).dispose();
        }
    }

    @Test
    public void testInternalFrameClosing_NoOption() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION),
                    eq(JOptionPane.QUESTION_MESSAGE),
                    isNull(),
                    any(),
                    eq("Нет")
            )).thenReturn(JOptionPane.NO_OPTION);

            closeHandler.internalFrameClosing(new InternalFrameEvent(mockFrame, InternalFrameEvent.INTERNAL_FRAME_CLOSING));

            verify(mockFrame, never()).dispose();
        }
    }

    @Test
    public void testConfirmExit() {
        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(
                    eq(mockFrame),
                    eq("Вы уверены, что хотите закрыть это окно?"),
                    eq("Подтверждение закрытия"),
                    eq(JOptionPane.YES_NO_OPTION),
                    eq(JOptionPane.QUESTION_MESSAGE),
                    isNull(),
                    any(),
                    eq("Нет")
            )).thenReturn(JOptionPane.YES_OPTION);

            boolean result = closeHandler.confirmExit(mockFrame);

            assertTrue(result);
        }
    }

    @Test
    public void testUpdateBundle() {
        ResourceBundle newBundle = Mockito.mock(ResourceBundle.class);
        when(newBundle.getString("confirmCloseWindow")).thenReturn("New message");
        when(newBundle.getString("confirmCloseTitle")).thenReturn("New title");
        when(newBundle.getString("yesButtonText")).thenReturn("Yes");
        when(newBundle.getString("noButtonText")).thenReturn("No");

        closeHandler.updateBundle(newBundle);

        try (MockedStatic<JOptionPane> mockedOptionPane = Mockito.mockStatic(JOptionPane.class)) {
            mockedOptionPane.when(() -> JOptionPane.showOptionDialog(
                    eq(mockFrame),
                    eq("New message"),
                    eq("New title"),
                    eq(JOptionPane.YES_NO_OPTION),
                    eq(JOptionPane.QUESTION_MESSAGE),
                    isNull(),
                    any(),
                    eq("No")
            )).thenReturn(JOptionPane.YES_OPTION);

            boolean result = closeHandler.confirmClose(mockFrame);

            assertTrue(result);
        }
    }
}