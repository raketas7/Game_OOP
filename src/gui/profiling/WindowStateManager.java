package gui.profiling;

import gui.MainApplicationFrame;
import gui.windows.BasicWindow;
import gui.windows.GameWindow;
import gui.windows.LogWindow;
import log.Logger;
import log.LogWindowSource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class WindowStateManager {
    private static final String STATE_FILE = "window_state.json";

    public static void saveWindowStates(MainApplicationFrame frame) throws PropertyVetoException {
        Map<String, Object> windowStates = new HashMap<>();
        windowStates.put("language", frame.getCurrentLocale().toString());

        for (Map.Entry<String, JInternalFrame> entry : frame.getInternalWindows().entrySet()) {
            windowStates.put(entry.getKey(), getWindowState(entry.getValue()));
        }

        try (FileWriter file = new FileWriter(STATE_FILE)) {
            file.write(new JSONObject(windowStates).toJSONString());
        } catch (IOException e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
    }

    public static Map<String, Object> loadWindowStates() {
        Map<String, Object> windowStates = new HashMap<>();
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(STATE_FILE)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            windowStates.put("language", jsonObject.get("language"));

            for (Object key : jsonObject.keySet()) {
                String windowName = (String) key;
                if (windowName.equals("language")) continue;

                JSONObject state = (JSONObject) jsonObject.get(windowName);
                Map<String, Object> stateMap = new HashMap<>();
                stateMap.put("iconified", state.get("iconified"));
                stateMap.put("maximized", state.get("maximized"));
                stateMap.put("x", getIntValue(state.get("x")));
                stateMap.put("y", getIntValue(state.get("y")));
                stateMap.put("width", getIntValue(state.get("width")));
                stateMap.put("height", getIntValue(state.get("height")));
                stateMap.put("visible", state.get("visible"));
                stateMap.put("closed", state.get("closed"));

                windowStates.put(windowName, stateMap);
            }
        } catch (IOException | ParseException e) {
            Logger.debug("No saved window states found or error loading: " + e.getMessage());
        }

        return windowStates;
    }

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

    public static void applyWindowState(JInternalFrame window, Map<String, Object> state) {
        if (state == null || window == null) return;

        try {
            int minWidth = 400;
            int minHeight = 300;

            int x = Math.max(0, getIntValue(state.get("x")));
            int y = Math.max(0, getIntValue(state.get("y")));
            int width = Math.max(minWidth, getIntValue(state.get("width")));
            int height = Math.max(minHeight, getIntValue(state.get("height")));

            window.setBounds(x, y, width, height);
            window.setVisible(Boolean.parseBoolean(state.get("visible").toString()));

        } catch (Exception e) {
            Logger.error("Error applying window state: " + e.getMessage());
        }
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

            int defaultWidth = windowId.equals("logWindow") ? 400 : 800;
            int defaultHeight = windowId.equals("logWindow") ? 500 : 600;
            int minSize = windowId.equals("logWindow") ? 300 : 400;

            int x = calculateSafeCoordinate(getIntValue(state.get("x")),
                    screenBounds.width, defaultWidth);
            int y = calculateSafeCoordinate(getIntValue(state.get("y")),
                    screenBounds.height, defaultHeight);
            int width = calculateSafeSize(getIntValue(state.get("width")),
                    minSize, screenBounds.width - x, defaultWidth);
            int height = calculateSafeSize(getIntValue(state.get("height")),
                    minSize, screenBounds.height - y, defaultHeight);

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
        SwingUtilities.invokeLater(() -> {
            applySpecialStates(frame, states);
        });
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
        switch (windowId) {
            case "logWindow":
                return createLogWindow(logSource, bundle);
            case "gameWindow":
                return createGameWindow(bundle);
            default:
                Logger.error("Unknown window ID: " + windowId);
                return null;
        }
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

    private static int calculateSafeCoordinate(int value, int screenSize, int windowSize) {
        return Math.max(0, Math.min(value, screenSize - windowSize));
    }

    private static int calculateSafeSize(int value, int minSize, int maxSize, int defaultSize) {
        if (value <= 0) return defaultSize;
        return Math.max(minSize, Math.min(value, maxSize));
    }
}