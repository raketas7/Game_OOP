package gui;

import log.LogWindowSource;
import log.Logger;

import java.awt.Frame;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class RobotsProgram {
  public static void main(String[] args) {
    // Установка Look and Feel
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Загрузка ресурсов
    ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    LogWindowSource logSource = Logger.getDefaultLogSource();

    // Запуск приложения
    SwingUtilities.invokeLater(() -> {
      MainApplicationFrame frame = new MainApplicationFrame(bundle, logSource); // Передаем ResourceBundle
      frame.pack();
      frame.setVisible(true);
      frame.setExtendedState(Frame.MAXIMIZED_BOTH);
    });
  }
}