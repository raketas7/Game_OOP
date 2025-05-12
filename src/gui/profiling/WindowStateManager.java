package gui.profiling;

import gui.MainApplicationFrame;
import gui.windows.BasicWindow;
import gui.windows.GameWindow;
import gui.windows.LogWindow;
import log.Logger;
import log.LogWindowSource;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class WindowStateManager {

    public static Map<String, Object> getWindowState(JInternalFrame window) throws PropertyVetoException {
        Map<String, Object> state = new HashMap<>();
        boolean wasMaximized = window.isMaximum();
        boolean wasIconified = window.isIcon();

        state.put("iconified", wasIconified);
        state.put("maximized", wasMaximized);
        state.put("closed", window.isClosed());

        if (wasIconified) window.setIcon(false);
        if (wasMaximized) window.setMaximum(false);

        Rectangle bounds = window.getBounds();
        state.put("x", bounds.x);
        state.put("y", bounds.y);
        state.put("width", bounds.width);
        state.put("height", bounds.height);
        state.put("visible", window.isVisible() && !window.isClosed());

        if (wasMaximized) window.setMaximum(true);
        if (wasIconified) window.setIcon(true);

        return state;
    }

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
            int width = getIntValue(state.get("width"));
            int height = getIntValue(state.get("height"));

            // Корректировка только если окно полностью невидимо
            if (x + width < 0) {
                x = 0; // Сдвигаем окно, чтобы хотя бы часть стала видимой
            } else if (x > screenBounds.width) {
                x = screenBounds.width - width; // Сдвигаем, если полностью за правой границей
            }
            if (y + height < 0) {
                y = 0; // Сдвигаем вверх, если полностью за верхней границей
            } else if (y > screenBounds.height) {
                y = screenBounds.height - height; // Сдвигаем, если полностью за нижней границей
            }

            // Убедимся, что ширина и высота не меньше минимального размера
            width = Math.max(minSize, width);
            height = Math.max(minSize, height);

            window.setBounds(x, y, width, height);
            window.setVisible(Boolean.TRUE.equals(state.get("visible")));

            if (window.getParent() == null) {
                frame.getContentPane().add(window);
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

        // Создаём отсутствующие окна
        for (String windowId : states.keySet()) {
            if (windowId.equals("language")) continue;
            if (!frame.getInternalWindows().containsKey(windowId) || frame.getInternalWindows().get(windowId).isClosed()) {
                JInternalFrame window = createWindow(windowId, frame.logSource, frame.bundle);
                if (window != null) {
                    frame.addWindow(windowId, window);
                }
            }
        }

        // Применяем состояния ко всем окнам
        for (String windowId : frame.getInternalWindows().keySet()) {
            Map<String, Object> state = (Map<String, Object>) states.get(windowId);
            if (state != null) {
                restoreWindowState(frame, state, screenBounds, windowId);
            }
        }

        // Применяем специальные состояния
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
        } catch (Exception e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
        return states;
    }

    public static LogWindow createLogWindow(LogWindowSource logSource, ResourceBundle bundle) {
        LogWindow window = new LogWindow(logSource, bundle);
        Logger.debug(bundle.getString("logMessage"));
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
        window.setMinimumSize(new Dimension(400, 300));
        return window;
    }

    public static JInternalFrame createWindow(String windowId, LogWindowSource logSource, ResourceBundle bundle) {
        return switch (windowId) {
            case "logWindow" -> createLogWindow(logSource, bundle);
            case "gameWindow" -> createGameWindow(bundle);
            default -> {
                Logger.error("Unknown window ID: " + windowId);
                yield null;
            }
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