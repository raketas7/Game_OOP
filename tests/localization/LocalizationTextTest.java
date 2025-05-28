package localization;

import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import static org.junit.jupiter.api.Assertions.assertEquals;

// проверка корректности связи языка с файлом ресурсов
public class LocalizationTextTest {

    @Test
    public void testEnglishText() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("en", "EN"));
        assertEquals("View Mode", bundle.getString("lookAndFeelMenu"), "Некорректный текст для lookAndFeelMenu на английском");
        assertEquals("Exit", bundle.getString("exitMenu"), "Некорректный текст для exitMenu на английском");
    }

    @Test
    public void testRussianText() {
        ResourceBundle bundle = ResourceBundle.getBundle("messages", new Locale("ru", "RU"));
        assertEquals("Режим просмотра", bundle.getString("lookAndFeelMenu"), "Некорректный текст для lookAndFeelMenu на русском");
        assertEquals("Выход", bundle.getString("exitMenu"), "Некорректный текст для exitMenu на русском");
    }
}
