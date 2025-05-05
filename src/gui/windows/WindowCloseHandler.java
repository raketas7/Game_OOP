package gui.windows;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.util.ResourceBundle;

public class WindowCloseHandler implements InternalFrameListener {
    private ResourceBundle bundle;

    public WindowCloseHandler(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void updateBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame frame = (JInternalFrame) e.getSource();
        if (confirmAction(frame,
                bundle.getString("confirmCloseWindow"),
                bundle.getString("confirmCloseTitle"))) {
            frame.dispose();
        }
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {}

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {}

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {}

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {}

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {}

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {}

    public boolean confirmClose(Component parentComponent) {
        return confirmAction(parentComponent,
                bundle.getString("confirmCloseWindow"),
                bundle.getString("confirmCloseTitle"));
    }

    public boolean confirmExit(Component parentComponent) {
        return confirmAction(parentComponent,
                bundle.getString("confirmCloseWindow"),
                bundle.getString("confirmCloseTitle"));
    }

    private boolean confirmAction(Component parentComponent, String message, String title) {
        Object[] options = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText")
        };

        int option = JOptionPane.showOptionDialog(
                parentComponent,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        return option == JOptionPane.YES_OPTION;
    }
}