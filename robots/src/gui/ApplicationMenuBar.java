package gui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import log.Logger;

public class ApplicationMenuBar {

    private final JMenuBar menuBar;

    //добавляет новые разделы меню через add
    public ApplicationMenuBar() {
        menuBar = new JMenuBar();
        menuBar.add(createLookAndFeelMenu());
        menuBar.add(createTestMenu());
        menuBar.add(createExitMenu()); // Добавляем меню "Выйти"
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    //создание меню для изменения внешнего вида
    private JMenu createLookAndFeelMenu() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription(
                "Управление режимом отображения приложения");

        lookAndFeelMenu.add(createSystemLookAndFeelMenuItem());
        lookAndFeelMenu.add(createCrossPlatformLookAndFeelMenuItem());

        return lookAndFeelMenu;
    }

    //создает подраздел меню режима отображения
    private JMenuItem createSystemLookAndFeelMenuItem() {
        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
        return systemLookAndFeel;
    }

    //создает подраздел меню режима отображения
    private JMenuItem createCrossPlatformLookAndFeelMenuItem() {
        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((event) -> setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()));
        return crossplatformLookAndFeel;
    }

    //создание тестового меню для работы с окном логов
    private JMenu createTestMenu() {
        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription(
                "Тестовые команды");

        testMenu.add(createAddLogMessageItem());

        return testMenu;
    }

    //создание подраздела тестового меню
    private JMenuItem createAddLogMessageItem() {
        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((event) -> Logger.debug("Новая строка"));
        return addLogMessageItem;
    }

    //создание меню "Выйти"
    private JMenu createExitMenu() {
        JMenu exitMenu = new JMenu("Выйти");
        exitMenu.setMnemonic(KeyEvent.VK_Q);
        exitMenu.getAccessibleContext().setAccessibleDescription("Выход из приложения");

        JMenuItem exitMenuItem = new JMenuItem("Выйти", KeyEvent.VK_Q);
        exitMenuItem.addActionListener((event) -> confirmAndExit());
        exitMenu.add(exitMenuItem);

        return exitMenu;
    }

    //подтверждение выхода из приложения
    private void confirmAndExit() {
        int option = JOptionPane.showConfirmDialog(
                menuBar.getTopLevelAncestor(), // Родительское окно для диалога
                "Вы уверены, что хотите выйти?", // Сообщение
                "Подтверждение выхода", // Заголовок
                JOptionPane.YES_NO_OPTION // Тип опций
        );

        if (option == JOptionPane.YES_NO_OPTION) {
            exitApplication(); // Выход, если пользователь подтвердил
        }
    }

    //обработка выхода из приложения
    private void exitApplication() {
        // Закрываем приложение
        System.exit(0);
    }

    //динамически изменяет внешний вид приложения
    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(menuBar.getTopLevelAncestor());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // just ignore
        }
    }
}