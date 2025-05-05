package gui;

import javax.swing.JInternalFrame;
import java.awt.Rectangle;
import java.beans.PropertyVetoException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WindowStateManager {
    private static final String STATE_FILE = "window_state.json";

    public static void saveWindowStates(MainApplicationFrame frame) {
        Map<String, Object> windowStates = new HashMap<>();

        if (frame.logWindow != null) {
            windowStates.put("logWindow", getWindowState(frame.logWindow));
        }
        if (frame.gameWindow != null) {
            windowStates.put("gameWindow", getWindowState(frame.gameWindow));
        }

        try (FileWriter file = new FileWriter(STATE_FILE)) {
            JSONObject jsonObject = new JSONObject(windowStates);
            file.write(jsonObject.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<String, Object>> loadWindowStates() {
        Map<String, Map<String, Object>> windowStates = new HashMap<>();
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(STATE_FILE)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            for (Object key : jsonObject.keySet()) {
                String windowName = (String) key;
                JSONObject state = (JSONObject) jsonObject.get(windowName);
                Map<String, Object> stateMap = new HashMap<>();

                stateMap.put("x", ((Number) state.get("x")).intValue());
                stateMap.put("y", ((Number) state.get("y")).intValue());
                stateMap.put("width", ((Number) state.get("width")).intValue());
                stateMap.put("height", ((Number) state.get("height")).intValue());
                stateMap.put("visible", (Boolean) state.get("visible"));

                windowStates.put(windowName, stateMap);
            }
        } catch (IOException | ParseException e) {
            // Если файла нет или он невалиден, возвращаем пустую карту
        }

        return windowStates;
    }

    private static Map<String, Object> getWindowState(JInternalFrame window) {
        Map<String, Object> state = new HashMap<>();
        Rectangle bounds = window.getBounds();

        state.put("x", bounds.x);
        state.put("y", bounds.y);
        state.put("width", bounds.width);
        state.put("height", bounds.height);
        state.put("visible", window.isVisible() && !window.isClosed());

        return state;
    }

    public static void applyWindowState(JInternalFrame window, Map<String, Object> state) {
        if (state != null) {
            int x = (int) state.get("x");
            int y = (int) state.get("y");
            int width = (int) state.get("width");
            int height = (int) state.get("height");
            boolean visible = (boolean) state.get("visible");

            window.setBounds(x, y, width, height);
            window.setVisible(visible);
            try {
                window.setMaximum(false);  // Сбрасываем состояние "развернуто", если оно было
            } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
            }
        }
    }
}