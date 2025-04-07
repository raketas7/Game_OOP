package gui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.*;

public class GameWindow extends JInternalFrame implements TranslatableWindow {

    public GameWindow(ResourceBundle bundle) {
        super("", true, true, true, true);
        GameVisualizer m_visualizer = new GameVisualizer(bundle);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);

        // Set the window to maximum size
        setMaximizable(true);
        try {
            setMaximum(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

        WindowCloseHandler closeHandler = new WindowCloseHandler(bundle);
        addInternalFrameListener(closeHandler);

        setTranslatedTitle(bundle);
    }

    @Override
    public void setTranslatedTitle(ResourceBundle bundle) {
        setTitle(bundle.getString("gameWindowTitle"));
    }
}
