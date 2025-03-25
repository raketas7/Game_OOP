package localization;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LocalizationKeyTest {

    @Test
    public void testAllKeysPresent() {
        Set<String> expectedKeys = getExpectedKeys();

        ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en", "EN"));
        for (String key : expectedKeys) {
            assertTrue(bundleEn.containsKey(key),
                    "Key '" + key + "' is missing in English resource file");
        }

        ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru", "RU"));
        for (String key : expectedKeys) {
            assertTrue(bundleRu.containsKey(key),
                    "Key '" + key + "' is missing in Russian resource file");
        }
    }

    private static Set<String> getExpectedKeys() {
        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("lookAndFeelMenu");
        expectedKeys.add("lookAndFeelDescription");
        expectedKeys.add("systemLookAndFeel");
        expectedKeys.add("crossPlatformLookAndFeel");
        expectedKeys.add("testMenu");
        expectedKeys.add("testMenuDescription");
        expectedKeys.add("addLogMessageItem");
        expectedKeys.add("exitMenu");
        expectedKeys.add("exitMenuDescription");
        expectedKeys.add("exitMenuItem");
        expectedKeys.add("languageMenu");
        expectedKeys.add("confirmCloseWindow");
        expectedKeys.add("confirmCloseTitle");
        expectedKeys.add("logWindowTitle");
        expectedKeys.add("gameWindowTitle");
        expectedKeys.add("logMessage");
        expectedKeys.add("yesButtonText");
        expectedKeys.add("noButtonText");

        return expectedKeys;
    }
}