package gui.profiling;

import javax.swing.JInternalFrame;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gui.MainApplicationFrame;
import log.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WindowStateManager {
    private static final String STATE_FILE = "window_state.json";

    public static void saveWindowStates(MainApplicationFrame frame) throws PropertyVetoException {
        Map<String, Object> windowStates = new HashMap<>();
        windowStates.put("language", frame.getCurrentLocale().toString());

        if (frame.logWindow != null) {
            windowStates.put("logWindow", getWindowState(frame.logWindow));
        }
        if (frame.gameWindow != null) {
            windowStates.put("gameWindow", getWindowState(frame.gameWindow));
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

    private static int getIntValue(Object value) {
        return ((Number) value).intValue();
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
            // Минимальные размеры окна
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
}