package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;
import log.Logger;
import log.LogWindowSource;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private ResourceBundle bundle;
    private final WindowCloseHandler windowCloseHandler;
    private final LogWindowSource logSource;

    public LogWindow logWindow;
    public GameWindow gameWindow;

    public MainApplicationFrame(ResourceBundle bundle, LogWindowSource logSource) {
        this.bundle = bundle;
        this.logSource = logSource;
        this.windowCloseHandler = new WindowCloseHandler(bundle);
        initializeUI();
        loadWindowStates();
    }

    private void initializeUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(50, 50, screenSize.width - 100, screenSize.height - 100);
        setContentPane(desktopPane);

        // Создаем окно логов
        logWindow = createLogWindow();
        addWindow(logWindow);

        // Создаем игровое окно без принудительного размера
        gameWindow = new GameWindow(bundle);
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

    private void loadWindowStates() {
        Map<String, Map<String, Object>> states = WindowStateManager.loadWindowStates();

        // Применяем состояние для logWindow
        Map<String, Object> logState = states.get("logWindow");
        if (logState != null) {
            WindowStateManager.applyWindowState(logWindow, logState);
        } else {
            logWindow.setLocation(10, 10);
            logWindow.setSize(300, 800);
        }

        // Применяем состояние для gameWindow
        Map<String, Object> gameState = states.get("gameWindow");
        if (gameState != null) {
            WindowStateManager.applyWindowState(gameWindow, gameState);
        } else {
            // Устанавливаем размер по умолчанию, только если состояния нет
            gameWindow.setSize(getWidth() - 300, getHeight() - 100);
            gameWindow.setLocation(320, 10);
        }
    }

    protected LogWindow createLogWindow() {
        LogWindow window = new LogWindow(logSource, bundle);
        window.pack();
        Logger.debug(bundle.getString("logMessage"));
        return window;
    }

    public void addWindow(JInternalFrame frame) {
        if (frame instanceof LogWindow) {
            if (logWindow != null && logWindow.isVisible() && !logWindow.isClosed()) {
                logWindow.toFront();
                return;
            }
            logWindow = (LogWindow) frame;
        }
        else if (frame instanceof GameWindow) {
            if (gameWindow != null && gameWindow.isVisible() && !gameWindow.isClosed()) {
                gameWindow.toFront();
                return;
            }
            gameWindow = (GameWindow) frame;
        }

        desktopPane.add(frame);
        frame.setVisible(true);
    }

    public void showLogWindow() {
        if (logWindow == null || logWindow.isClosed()) {
            logWindow = createLogWindow();
        }
        addWindow(logWindow);
    }

    public void showGameWindow() {
        if (gameWindow == null || gameWindow.isClosed()) {
            gameWindow = new GameWindow(bundle);
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
            WindowStateManager.saveWindowStates(this);
            System.exit(0);
        }
    }
}