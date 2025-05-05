package gui;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.JPanel;

public class GameWindow extends BasicWindow {
    private final GameVisualizer visualizer;

    public GameWindow(ResourceBundle bundle) {
        super(true, true, true, true);
        this.visualizer = new GameVisualizer(bundle);
        initializeUI(bundle);
    }

    @Override
    protected String getTitleKey() {
        return "gameWindowItem";
    }

    private void initializeUI(ResourceBundle bundle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        setupDefaultCloseOperation(bundle);
        setTranslatedTitle(bundle);
    }
}