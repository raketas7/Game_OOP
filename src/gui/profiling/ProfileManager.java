package gui.profiling;

import gui.MainApplicationFrame;
import log.Logger;
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
    public static final String PROFILES_DIR = "profiles";

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
            for (Object value : result.values()) {
                if (value instanceof JSONObject windowState) {
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

        // Создаем массив опций с переведенными текстами
        Object[] options = {
                bundle.getString("loadButtonText"),
                bundle.getString("cancelButtonText")
        };

        // Создаем диалог с переведенными текстами
        JOptionPane pane = new JOptionPane(
                bundle.getString("selectProfileToLoad"),
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null,
                options,
                options[0]
        );

        // Получаем выбранный профиль
        Object selectedValue = pane.getValue();
        if (selectedValue == null ||
                selectedValue.equals(bundle.getString("cancelButtonText")) ||
                ((selectedValue instanceof Integer) && (Integer)selectedValue == JOptionPane.CLOSED_OPTION)) {
            return null;
        }

        // Получаем выбранный профиль из ComboBox
        JComboBox<String> comboBox = new JComboBox<>(profiles.toArray(new String[0]));
        comboBox.setSelectedIndex(0);
        pane.setMessage(new Object[] {bundle.getString("selectProfileToLoad"), comboBox});
        pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = pane.createDialog(parent, bundle.getString("loadProfile"));
        dialog.setVisible(true);

        selectedValue = pane.getValue();
        if (selectedValue == null ||
                selectedValue.equals(JOptionPane.CANCEL_OPTION) ||
                ((selectedValue instanceof Integer) && (Integer)selectedValue == JOptionPane.CLOSED_OPTION)) {
            return null;
        }

        return (String) comboBox.getSelectedItem();
    }

    public static boolean isValidProfileName(String name) {
        return name != null && !name.trim().isEmpty() &&
                !name.contains(File.separator) && !name.contains(".") &&
                name.matches("[a-zA-Z0-9_\\- ]+");
    }

    public static void checkAndLoadProfile(MainApplicationFrame frame, ResourceBundle bundle) {
        if (hasSavedProfiles()) {
            Object[] options = {
                    bundle.getString("yesButtonText"),
                    bundle.getString("noButtonText")
            };

            int option = JOptionPane.showOptionDialog(
                    frame,
                    bundle.getString("loadProfileQuestion"),
                    bundle.getString("loadProfile"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (option == JOptionPane.YES_OPTION) {
                loadSelectedProfile(frame, bundle);
            } else {
                frame.resetPlayerCoins();
            }
        }
    }

    public static void loadSelectedProfile(MainApplicationFrame frame, ResourceBundle bundle) {
        String profileName = showLoadDialog(frame, bundle);

        if (profileName != null && !profileName.isEmpty()) {
            try {
                Map<String, Object> states = loadProfile(profileName);
                frame.applyProfileState(states);
                frame.revalidate();
                frame.repaint();
            } catch (Exception e) {
                handleProfileLoadError(frame, bundle, e);
            }
        }
    }

    public static boolean saveProfileWithValidation(MainApplicationFrame frame, ResourceBundle bundle) {
        String profileName;
        do {
            profileName = JOptionPane.showInputDialog(
                    frame,
                    bundle.getString("enterProfileName"),
                    bundle.getString("saveProfileTitle"),
                    JOptionPane.QUESTION_MESSAGE
            );

            if (profileName == null) {
                return false; // Пользователь отменил ввод
            }

            if (!isValidProfileName(profileName)) {
                JOptionPane.showMessageDialog(
                        frame,
                        bundle.getString("invalidName"),
                        bundle.getString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE
                );
                continue;
            }

            try {
                frame.saveCurrentState(profileName);
                JOptionPane.showMessageDialog(
                        frame,
                        bundle.getString("profileSavedSuccess"),
                        bundle.getString("successTitle"),
                        JOptionPane.INFORMATION_MESSAGE
                );
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame,
                        bundle.getString("saveError") + e.getMessage(),
                        bundle.getString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        } while (true);
    }

    public static void handleProfileLoadError(Component parent, ResourceBundle bundle, Exception e) {
        Logger.error("Error loading profile: " + e.getMessage());
        JOptionPane.showMessageDialog(
                parent,
                bundle.getString("loadError") + e.getMessage(),
                bundle.getString("errorTitle"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void confirmAndClose(MainApplicationFrame frame, ResourceBundle bundle) {
        // Сначала спрашиваем подтверждение выхода
        Object[] exitOptions = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText")
        };

        int exitChoice = JOptionPane.showOptionDialog(
                frame,
                bundle.getString("confirmExitQuestion"), // Нужно добавить этот ключ в ресурсы
                bundle.getString("exitConfirmation"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                exitOptions,
                exitOptions[1]
        );

        // Если пользователь не хочет выходить, просто возвращаемся
        if (exitChoice != JOptionPane.YES_OPTION) {
            return;
        }

        // Теперь спрашиваем о сохранении
        Object[] saveOptions = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText"),
                bundle.getString("cancelButtonText")
        };

        int saveChoice = JOptionPane.showOptionDialog(
                frame,
                bundle.getString("saveBeforeExit"),
                bundle.getString("saveConfirmation"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                saveOptions,
                saveOptions[2]
        );

        if (saveChoice == JOptionPane.YES_OPTION) {
            boolean saved = saveProfileWithValidation(frame, bundle);
            if (saved) {
                System.exit(0);
            }
        } else if (saveChoice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }
}