package gui.profiling;

import gui.MainApplicationFrame;
import gui.windows.BasicWindow;
import gui.windows.GameWindow;
import gui.windows.LogWindow;
import gui.GameMechanics.Player;
import gui.GameMechanics.Achievement;
import gui.Visuals.GameVisualizer;
import log.Logger;
import log.LogWindowSource;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.List;

public class WindowStateManager {

    public static void positionWindow(JInternalFrame window, Rectangle screenBounds,
                                      int defaultX, int defaultY,
                                      int defaultWidth, int defaultHeight) {
        if (window == null) return;

        int x = Math.max(0, Math.min(defaultX, screenBounds.width - defaultWidth - 50));
        int y = Math.max(0, Math.min(defaultY, screenBounds.height - defaultHeight - 50));
        int width = Math.min(defaultWidth, screenBounds.width - x - 50);
        int height = Math.min(defaultHeight, screenBounds.height - y - 50);

        window.setBounds(x, y, width, height);
    }

    public static Map<String, Object> getWindowState(JInternalFrame window) throws PropertyVetoException {
        Map<String, Object> state = new HashMap<>();

        boolean wasIconified = window.isIcon();
        state.put("iconified", wasIconified);
        if (wasIconified) {
            window.setIcon(false);
        }

        boolean wasMaximized = window.isMaximum();
        state.put("maximized", wasMaximized);
        if (wasMaximized) {
            window.setMaximum(false);
        }

        state.put("closed", window.isClosed());
        Rectangle bounds = window.getBounds();
        state.put("x", bounds.x);
        state.put("y", bounds.y);
        state.put("width", bounds.width);
        state.put("height", bounds.height);
        state.put("visible", window.isVisible() && !window.isClosed());

        if (wasMaximized) {
            window.setMaximum(true);
        }
        if (wasIconified) {
            window.setIcon(true);
        }

        return state;
    }

    public static void restoreWindowState(MainApplicationFrame frame, Map<String, Object> state,
                                          Rectangle screenBounds, String windowId) {
        if (state == null) return;

        JInternalFrame window = frame.getInternalWindows().get(windowId);
        if (window == null) return;

        try {
            resetWindowState(window);

            int minSize = windowId.equals("logWindow") ? 300 : 400;
            int x = getIntValue(state.get("x"));
            int y = getIntValue(state.get("y"));
            int width = Math.max(minSize, getIntValue(state.get("width")));
            int height = Math.max(minSize, getIntValue(state.get("height")));

            if (x + width < 0) x = 0;
            else if (x > screenBounds.width) x = screenBounds.width - width;
            if (y + height < 0) y = 0;
            else if (y > screenBounds.height) y = screenBounds.height - height;

            window.setBounds(x, y, width, height);
            window.setVisible(Boolean.TRUE.equals(state.get("visible")));

            if (window.getParent() == null) {
                frame.getContentPane().add(window);
            }

            if (Boolean.TRUE.equals(state.get("maximized"))) {
                window.setMaximum(true);
            }

            if (Boolean.TRUE.equals(state.get("iconified"))) {
                window.setIcon(true);
            }

            if (window instanceof BasicWindow) {
                ((BasicWindow) window).setTranslatedTitle(frame.bundle);
            }
        } catch (Exception e) {
            Logger.error("Error restoring window state for " + windowId + ": " + e.getMessage());
        }
    }

    public static void applyWindowSpecialState(JInternalFrame window, Map<String, Object> state) {
        if (state == null || window == null) return;

        try {
            if (Boolean.TRUE.equals(state.get("maximized"))) {
                window.setMaximum(true);
            } else if (Boolean.TRUE.equals(state.get("iconified"))) {
                window.setIcon(true);
            }
        } catch (PropertyVetoException e) {
            Logger.error("Can't apply special state: " + e.getMessage());
        }
    }

    public static void resetWindowState(JInternalFrame window) throws PropertyVetoException {
        if (window.isMaximum()) window.setMaximum(false);
        if (window.isIcon()) window.setIcon(false);
    }

    public static void applyWindowStates(MainApplicationFrame frame, Map<String, Object> states) {
        if (states == null) return;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();

        // Restore player coins and enemies killed if present
        JInternalFrame gameWindow = frame.getInternalWindows().get("gameWindow");
        if (gameWindow instanceof GameWindow) {
            Player player = ((GameWindow) gameWindow).getPlayer();
            if (states.containsKey("playerCoins")) {
                int coins = ((Number) states.get("playerCoins")).intValue();
                player.addCoins(coins - player.getCoins());
                player.saveCoins();
            }
            if (states.containsKey("enemiesKilled")) {
                int enemiesKilled = ((Number) states.get("enemiesKilled")).intValue();
                player.setEnemiesKilled(enemiesKilled);
            }
            // Restore achievement states
            GameVisualizer visualizer = ((GameWindow) gameWindow).getGameVisualizer();
            List<Achievement> achievements = visualizer.getAchievements();
            for (int i = 0; i < achievements.size(); i++) {
                String key = "achievement_" + i + "_unlocked";
                if (states.containsKey(key)) {
                    boolean isUnlocked = (Boolean) states.get(key);
                    achievements.get(i).reset(); // Reset first
                    if (isUnlocked) {
                        achievements.get(i).updateStatus(player.getEnemiesKilled());
                    }
                }
            }
            visualizer.updateAchievementsPanel();
        }

        for (String windowId : states.keySet()) {
            if (windowId.equals("language") || windowId.equals("playerCoins") || windowId.equals("enemiesKilled") || windowId.startsWith("achievement_")) continue;
            if (!frame.getInternalWindows().containsKey(windowId) || frame.getInternalWindows().get(windowId).isClosed()) {
                JInternalFrame window = createWindow(windowId, frame.logSource, frame.bundle);
                if (window != null) {
                    frame.addWindow(windowId, window);
                }
            }
        }

        for (String windowId : frame.getInternalWindows().keySet()) {
            Map<String, Object> state = (Map<String, Object>) states.get(windowId);
            if (state != null) {
                restoreWindowState(frame, state, screenBounds, windowId);
            }
        }

        SwingUtilities.invokeLater(() -> applySpecialStates(frame, states));
    }

    public static Map<String, Object> getWindowStateWithFocus(JInternalFrame window, boolean isFocused)
            throws PropertyVetoException {
        Map<String, Object> state = getWindowState(window);
        state.put("focused", isFocused && window.isSelected());
        return state;
    }

    public static Map<String, Object> saveCurrentWindowStates(MainApplicationFrame frame) {
        Map<String, Object> states = new HashMap<>();
        try {
            for (Map.Entry<String, JInternalFrame> entry : frame.getInternalWindows().entrySet()) {
                states.put(entry.getKey(), getWindowStateWithFocus(entry.getValue(), entry.getKey().equals("logWindow")));
            }
            // Add player coins and enemies killed to the state map
            JInternalFrame gameWindow = frame.getInternalWindows().get("gameWindow");
            if (gameWindow instanceof GameWindow) {
                Player player = ((GameWindow) gameWindow).getPlayer();
                states.put("playerCoins", player.getCoins());
                states.put("enemiesKilled", player.getEnemiesKilled());
                // Save achievement states
                GameVisualizer visualizer = ((GameWindow) gameWindow).getGameVisualizer();
                List<Achievement> achievements = visualizer.getAchievements();
                for (int i = 0; i < achievements.size(); i++) {
                    states.put("achievement_" + i + "_unlocked", achievements.get(i).isUnlocked());
                }
            }
        } catch (Exception e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
        return states;
    }

    public static LogWindow createLogWindow(LogWindowSource logSource, ResourceBundle bundle) {
        LogWindow window = new LogWindow(logSource, bundle);
        window.setClosable(true);
        window.setResizable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        return window;
    }

    public static GameWindow createGameWindow(ResourceBundle bundle) {
        GameWindow window = new GameWindow(bundle);
        window.setClosable(true);
        window.setResizable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        return window;
    }

    public static JInternalFrame createWindow(String windowId, LogWindowSource logSource, ResourceBundle bundle) {
        return switch (windowId) {
            case "logWindow" -> createLogWindow(logSource, bundle);
            case "gameWindow" -> createGameWindow(bundle);
            default -> null;
        };
    }

    public static void applySpecialStates(MainApplicationFrame frame, Map<String, Object> states) {
        for (String windowId : frame.getInternalWindows().keySet()) {
            Map<String, Object> state = (Map<String, Object>) states.get(windowId);
            if (state != null) {
                applyWindowSpecialState(frame.getInternalWindows().get(windowId), state);
            }
        }
    }

    private static int getIntValue(Object value) {
        return ((Number) value).intValue();
    }
}