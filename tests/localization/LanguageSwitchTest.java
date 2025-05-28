package localization;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertEquals;

// проверка того, что после смены языка возвращается фраза на правильном языке
public class LanguageSwitchTest {

    @Test
    public void testLanguageSwitch() {
        ResourceBundle bundleRu = ResourceBundle.getBundle("messages", new Locale("ru", "RU"));
        assertEquals("Режим просмотра", bundleRu.getString("lookAndFeelMenu"), "Некорректный текст после смены языка на русский");

        ResourceBundle bundleEn = ResourceBundle.getBundle("messages", new Locale("en", "EN"));
        assertEquals("View Mode", bundleEn.getString("lookAndFeelMenu"), "Некорректный текст после смены языка на английский");
    }
}
