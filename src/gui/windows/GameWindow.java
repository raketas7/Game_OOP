package gui.windows;

import gui.GameMechanics.Player;
import gui.Visuals.GameVisualizer;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javax.swing.JPanel;

public class GameWindow extends BasicWindow {
    private final GameVisualizer visualizer;

    public GameWindow(ResourceBundle bundle) {
        super(true, true, true, true);
        this.visualizer = new GameVisualizer(bundle);
        initializeUI(bundle);
        addInternalFrameListener(new WindowCloseHandler(bundle));
    }

    @Override
    protected String getTitleKey() {
        return "gameWindowItem";
    }

    private void initializeUI(ResourceBundle bundle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        setupDefaultCloseOperation();
        setTranslatedTitle(bundle);
        pack();
    }

    public Player getPlayer() {
        return visualizer.getPlayer();
    }

    public GameVisualizer getGameVisualizer() {
        return visualizer;
    }
}