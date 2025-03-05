package gui;

import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

public class GameWindow extends JInternalFrame {

    public GameWindow() {
        super("Игровое поле", true, true, true, true);
        GameVisualizer m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        addInternalFrameListener(new InternalFrameListener() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                confirmAndClose();
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
        });
    }

    private void confirmAndClose() {
        int option = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите закрыть это окно?",
                "Подтверждение закрытия",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            dispose(); // Закрываем окно, если пользователь подтвердил
        }
        // Если NO_OPTION, окно не закрывается
    }
}