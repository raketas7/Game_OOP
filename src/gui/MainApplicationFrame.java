package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;
import log.Logger;
import log.LogWindowSource;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private ResourceBundle bundle;
    private final WindowCloseHandler windowCloseHandler;
    private final LogWindowSource logSource;

    private LogWindow logWindow;
    private GameWindow gameWindow;

    public MainApplicationFrame(ResourceBundle bundle, LogWindowSource logSource) {
        this.bundle = bundle;
        this.logSource = logSource;
        this.windowCloseHandler = new WindowCloseHandler(bundle);
        initializeUI();
    }

    private void initializeUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(50, 50, screenSize.width - 100, screenSize.height - 100);
        setContentPane(desktopPane);

        // Создаем окно логов
        logWindow = createLogWindow();
        addWindow(logWindow);

        // Создаем игровое окно
        gameWindow = new GameWindow(bundle);
        gameWindow.setSize(getWidth(), getHeight());
        addWindow(gameWindow);

        ApplicationMenuBar menuBar = new ApplicationMenuBar(bundle, this);
        setJMenuBar(menuBar.getMenuBar());

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndClose();
            }
        });
    }

    protected LogWindow createLogWindow() {
        LogWindow window = new LogWindow(logSource, bundle);
        window.setLocation(10, 10);
        window.setSize(300, 800);
        setMinimumSize(window.getSize());
        window.pack();
        Logger.debug(bundle.getString("logMessage"));
        return window;
    }

    public void addWindow(JInternalFrame frame) {
        // Проверяем, не пытаемся ли мы добавить дубликат окна
        if (frame instanceof LogWindow) {
            if (logWindow != null && logWindow.isVisible()) {
                logWindow.toFront();
                return;
            }
            logWindow = (LogWindow) frame;
        }
        else if (frame instanceof GameWindow) {
            if (gameWindow != null && gameWindow.isVisible()) {
                gameWindow.toFront();
                return;
            }
            gameWindow = (GameWindow) frame;
        }

        desktopPane.add(frame);
        frame.setVisible(true);
    }

    // Методы для открытия окон с проверкой дубликатов
    public void showLogWindow() {
        if (logWindow == null || logWindow.isClosed()) {
            logWindow = createLogWindow();
        }
        addWindow(logWindow);
    }

    public void showGameWindow() {
        if (gameWindow == null || gameWindow.isClosed()) {
            gameWindow = new GameWindow(bundle);
            gameWindow.setSize(800, 400);
        }
        addWindow(gameWindow);
    }

    public void updateLocale(Locale locale) {
        Rectangle bounds = getBounds();
        this.bundle = ResourceBundle.getBundle("messages", locale);
        windowCloseHandler.updateBundle(this.bundle);
        recreateUI();
        setBounds(bounds);
    }

    private void recreateUI() {
        getContentPane().removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    private void confirmAndClose() {
        if (windowCloseHandler.confirmExit(this)) {
            System.exit(0);
        }
    }
}