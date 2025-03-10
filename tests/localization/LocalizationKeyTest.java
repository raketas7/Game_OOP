package localization;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.assertTrue;

// проверка наличия всех ключей в файлах с ресурсами
public class LocalizationKeyTest {

    @Test
    public void testAllKeysPresent() {
        Set<String> expectedKeys = getStrings();

        ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en", "EN"));
        for (String key : expectedKeys) {
            assertTrue(bundleEn.containsKey(key), "Ключ " + key + " отсутствует в английском файле ресурсов");
        }

        ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru", "RU"));
        for (String key : expectedKeys) {
            assertTrue(bundleRu.containsKey(key), "Ключ " + key + " отсутствует в русском файле ресурсов");
        }
    }

    private static Set<String> getStrings() {
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
        expectedKeys.add("confirmExit");
        expectedKeys.add("confirmClose");
        expectedKeys.add("logWindowTitle");
        expectedKeys.add("gameWindowTitle");
        expectedKeys.add("logMessage");
        return expectedKeys;
    }
}
