package gui;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import log.Logger;

public class ApplicationMenuBar {

    private final JMenuBar menuBar;
    private final ResourceBundle bundle;
    private final MainApplicationFrame mainFrame;
    private final WindowCloseHandler windowCloseHandler;

    public ApplicationMenuBar(ResourceBundle bundle, MainApplicationFrame mainFrame) {
        this.bundle = bundle;
        this.mainFrame = mainFrame;
        this.windowCloseHandler = new WindowCloseHandler(bundle);
        menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu()); // меню режима отображения
        menuBar.add(createTestMenu()); // тестовое меню
        menuBar.add(createLanguageMenu()); // меню языка
        menuBar.add(createExitMenu()); // меню выйти
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    // меню режима отображения
    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu(bundle.getString("lookAndFeelMenu"));
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                bundle.getString("lookAndFeelDescription"));

        lookAndFeelMenu.add(createSystemLookAndFeelMenuItem());
        lookAndFeelMenu.add(createCrossPlatformLookAndFeelMenuItem());

        return lookAndFeelMenu;
    }

    // подраздел (системная схема)
    private JMenuItem createSystemLookAndFeelMenuItem() {
        JMenuItem systemLookAndFeel = new JMenuItem(bundle.getString("systemLookAndFeel"), KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
        return systemLookAndFeel;
    }

    // подраздел (универсальная схема)
    private JMenuItem createCrossPlatformLookAndFeelMenuItem() {
        JMenuItem crossplatformLookAndFeel = new JMenuItem(bundle.getString("crossPlatformLookAndFeel"), KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()));
        return crossplatformLookAndFeel;
    }

    // тестовое меню
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu(bundle.getString("testMenu"));
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                bundle.getString("testMenuDescription"));

        testMenu.add(createAddLogMessageItem());

        return testMenu;
    }

    // подраздел (добавление сообщения в лог)
    private JMenuItem createAddLogMessageItem() {
        JMenuItem addLogMessageItem = new JMenuItem(bundle.getString("addLogMessageItem"), KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> Logger.debug("Новая строка"));
        return addLogMessageItem;
    }

    // меню языка
    private JMenu createLanguageMenu() {
        JMenu languageMenu = new JMenu(bundle.getString("languageMenu"));
        languageMenu.setMnemonic(KeyEvent.VK_L);

        JMenuItem russianLanguageItem = new JMenuItem("Русский");
        russianLanguageItem.addActionListener((event) -> changeLanguage(new Locale("ru", "RU")));

        JMenuItem englishLanguageItem = new JMenuItem("English");
        englishLanguageItem.addActionListener((event) -> changeLanguage(Locale.ENGLISH));

        languageMenu.add(russianLanguageItem);
        languageMenu.add(englishLanguageItem);

        return languageMenu;
    }

    // метод для изменения языка
    private void changeLanguage(Locale locale) {
        mainFrame.updateLocale(locale); // Обновляем локаль в главном окне
    }

    // меню выйти
    private JMenu createExitMenu() {
        JMenu exitMenu = new JMenu(bundle.getString("exitMenu"));
        exitMenu.setMnemonic(KeyEvent.VK_Q);
        exitMenu.getAccessibleContext().setAccessibleDescription(
                bundle.getString("exitMenuDescription"));

        JMenuItem exitMenuItem = new JMenuItem(bundle.getString("exitMenuItem"), KeyEvent.VK_Q);
        exitMenuItem.addActionListener((event) -> confirmAndExit());
        exitMenu.add(exitMenuItem);

        return exitMenu;
    }

    // подтверждение выхода
    private void confirmAndExit() {
        if (windowCloseHandler.confirmExit(this.getMenuBar())) {
            System.exit(0);
        }
    }

    // динамическое изменение внешнего вида
    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(menuBar.getTopLevelAncestor());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Игнорируем ошибки
        }
    }
}