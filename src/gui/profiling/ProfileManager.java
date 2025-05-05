package gui.profiling;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProfileManager {
    private static final String PROFILES_DIR = "profiles";

    public static void saveProfile(String profileName, Map<String, Object> windowStates) throws IOException {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (!isValidProfileName(profileName)) {
            throw new IOException("Invalid profile name");
        }

        String filePath = PROFILES_DIR + File.separator + profileName + ".json";

        try (FileWriter file = new FileWriter(filePath)) {
            JSONObject jsonObject = new JSONObject(windowStates);
            file.write(jsonObject.toJSONString());
            file.flush();
        }
    }

    public static Map<String, Object> loadProfile(String profileName) throws IOException, ParseException {
        String filePath = PROFILES_DIR + File.separator + profileName + ".json";
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(filePath)) {
            Map<String, Object> result = (Map<String, Object>) parser.parse(reader);
            // Преобразуем все Long значения в Integer для координат и размеров
            for (Object value : result.values()) {
                if (value instanceof JSONObject) {
                    JSONObject windowState = (JSONObject) value;
                    for (Object key : windowState.keySet()) {
                        String prop = (String) key;
                        if (prop.equals("x") || prop.equals("y") || prop.equals("width") || prop.equals("height")) {
                            windowState.put(prop, ((Number) windowState.get(prop)).intValue());
                        }
                    }
                }
            }
            return result;
        }
    }

    public static List<String> getAvailableProfiles() {
        List<String> profiles = new ArrayList<>();
        File dir = new File(PROFILES_DIR);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    profiles.add(file.getName().replace(".json", ""));
                }
            }
        }
        return profiles;
    }

    public static boolean hasSavedProfiles() {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) return false;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        return files != null && files.length > 0;
    }

    public static boolean showSaveDialog(Component parent, ResourceBundle bundle) {
        Object[] options = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText")
        };

        int option = JOptionPane.showOptionDialog(
                parent,
                bundle.getString("saveBeforeExit"),
                bundle.getString("exitConfirmation"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1] // default to "No"
        );

        return option == JOptionPane.YES_OPTION;
    }

    public static String showLoadDialog(Component parent, ResourceBundle bundle) {
        List<String> profiles = getAvailableProfiles();
        if (profiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    bundle.getString("noProfilesAvailable"),
                    bundle.getString("loadProfile"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }

        Object[] options = {
                bundle.getString("loadButtonText"),
                bundle.getString("cancelButtonText")
        };

        return (String) JOptionPane.showInputDialog(
                parent,
                bundle.getString("selectProfileToLoad"),
                bundle.getString("loadProfile"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                profiles.toArray(),
                profiles.get(0)
        );
    }

    public static boolean profileExists(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) {
            return false;
        }

        File profileFile = new File(PROFILES_DIR + File.separator + profileName + ".json");
        return profileFile.exists();
    }

    public static boolean isValidProfileName(String name) {
        return name != null && !name.trim().isEmpty() &&
                !name.contains(File.separator) && !name.contains(".") &&
                name.matches("[a-zA-Z0-9_\\- ]+");
    }
}