package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import log.Logger;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();

    public MainApplicationFrame() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width  - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        ApplicationMenuBar menuBar = new ApplicationMenuBar();
        setJMenuBar(menuBar.getMenuBar());

        // Устанавливаем поведение при закрытии окна
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Отключаем стандартное закрытие

        // Добавляем слушатель для подтверждения закрытия
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndClose();
            }
        });
    }

    // Создает и настраивает окно протокола
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    // Добавляет окна (логи и игру)
    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    //обработка выхода из приложения
    private void exitApplication() {
        // Закрываем приложение
        System.exit(0);
    }

    // Метод для подтверждения закрытия
    private void confirmAndClose() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Вы уверены, что хотите закрыть приложение?",
                "Подтверждение закрытия",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_NO_OPTION) {
            exitApplication(); // Выход, если пользователь подтвердил
        }
    }
}