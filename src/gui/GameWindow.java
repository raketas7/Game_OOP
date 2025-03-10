package gui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.*;

public class GameWindow extends JInternalFrame {
    public GameWindow(ResourceBundle bundle) {
        super(bundle.getString("gameWindowTitle"), true, true, true, true);
        GameVisualizer m_visualizer = new GameVisualizer();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        WindowCloseHandler closeHandler = new WindowCloseHandler(bundle);
        addInternalFrameListener(closeHandler);
    }
}