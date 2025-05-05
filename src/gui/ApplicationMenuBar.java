package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.beans.PropertyVetoException;

import gui.profiling.ProfileManager;
import log.Logger;

public class ApplicationMenuBar {
    private final JMenuBar menuBar;
    private final ResourceBundle bundle;
    private final MainApplicationFrame mainFrame;

    public ApplicationMenuBar(ResourceBundle bundle, MainApplicationFrame mainFrame) {
        this.bundle = bundle;
        this.mainFrame = mainFrame;
        menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createLanguageMenu());
        menuBar.add(createWindowsMenu());
        menuBar.add(createExitMenu());
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    private JMenu createWindowsMenu() {
        JMenu windowsMenu = new JMenu(bundle.getString("windowsMenu"));
        windowsMenu.setMnemonic(KeyEvent.VK_W);

        JMenuItem gameWindowItem = new JMenuItem(bundle.getString("gameWindowItem"), KeyEvent.VK_G);
        gameWindowItem.addActionListener((event) -> mainFrame.showGameWindow());

        JMenuItem logWindowItem = new JMenuItem(bundle.getString("logWindowItem"), KeyEvent.VK_L);
        logWindowItem.addActionListener((event) -> mainFrame.showLogWindow());

        windowsMenu.add(gameWindowItem);
        windowsMenu.add(logWindowItem);

        return windowsMenu;
    }

    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu(bundle.getString("lookAndFeelMenu"));
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                bundle.getString("lookAndFeelDescription"));

        lookAndFeelMenu.add(createSystemLookAndFeelMenuItem());
        lookAndFeelMenu.add(createCrossPlatformLookAndFeelMenuItem());

        return lookAndFeelMenu;
    }

    private JMenuItem createSystemLookAndFeelMenuItem() {
        JMenuItem systemLookAndFeel = new JMenuItem(bundle.getString("systemLookAndFeel"), KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
        return systemLookAndFeel;
    }

    private JMenuItem createCrossPlatformLookAndFeelMenuItem() {
        JMenuItem crossplatformLookAndFeel = new JMenuItem(bundle.getString("crossPlatformLookAndFeel"), KeyEvent.VK_U);
        crossplatformLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()));
        return crossplatformLookAndFeel;
    }

    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu(bundle.getString("testMenu"));
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                bundle.getString("testMenuDescription"));

        testMenu.add(createAddLogMessageItem());

        return testMenu;
    }

    private JMenuItem createAddLogMessageItem() {
        JMenuItem addLogMessageItem = new JMenuItem(bundle.getString("addLogMessageItem"), KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> Logger.debug("New log message"));
        return addLogMessageItem;
    }

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

    private void changeLanguage(Locale locale) {
        mainFrame.updateLocale(locale);
        SwingUtilities.updateComponentTreeUI(mainFrame);
    }

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

    private void confirmAndExit() {
        Object[] options = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText")
        };

        int option = JOptionPane.showOptionDialog(
                mainFrame,
                bundle.getString("saveBeforeExit"),
                bundle.getString("exitConfirmation"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (option == JOptionPane.YES_OPTION) {
            boolean saved = mainFrame.saveProfileWithValidation();
            if (saved) {
                System.exit(0);
            }
            // Если сохранение не удалось, остаемся в приложении
        } else {
            System.exit(0);
        }
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(mainFrame);
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            Logger.error("Error setting look and feel: " + e.getMessage());
        }
    }
}