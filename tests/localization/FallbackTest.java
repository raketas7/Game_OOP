package localization;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FallbackTest {

    // проверка, что если подключаем несуществующий язык, то язык == языку системы
    @Test
    public void testFallbackToSystemLanguage() {
        Locale systemLocale = Locale.getDefault();

        ResourceBundle systemBundle = ResourceBundle.getBundle("messages", systemLocale);

        ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("fr", "FR"));

        assertEquals(systemBundle.getString("lookAndFeelMenu"), bundle.getString("lookAndFeelMenu"), "Некорректный fallback для lookAndFeelMenu");
        assertEquals(systemBundle.getString("exitMenu"), bundle.getString("exitMenu"), "Некорректный fallback для exitMenu");
    }
}
