package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.*;

import gui.profiling.ProfileManager;
import gui.profiling.WindowStateManager;
import gui.windows.BasicWindow;
import gui.windows.GameWindow;
import gui.windows.LogWindow;
import log.Logger;
import log.LogWindowSource;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private ResourceBundle bundle;
    private final LogWindowSource logSource;
    private Locale currentLocale;

    public LogWindow logWindow;
    public GameWindow gameWindow;

    public MainApplicationFrame(ResourceBundle bundle, LogWindowSource logSource) {
        this.bundle = bundle;
        this.logSource = logSource;
        this.currentLocale = bundle.getLocale();

        initializeFrameSettings();
        createAndPositionDefaultWindows();
        checkAndLoadProfile();
        setupMenuBar();
        setupWindowListener();
    }

    private void initializeFrameSettings() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(desktopPane);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
        setBounds(100, 100,
                Math.min(1200, screenBounds.width - 200),
                Math.min(800, screenBounds.height - 200));
    }

    private void createAndPositionDefaultWindows() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();

        logWindow = createLogWindow();
        gameWindow = createGameWindow();

        positionWindow(logWindow, screenBounds, 50, 50, 400, 500);
        positionWindow(gameWindow, screenBounds, 470, 50, 800, 600);

        desktopPane.add(logWindow);
        desktopPane.add(gameWindow);
        logWindow.setVisible(true);
        gameWindow.setVisible(true);
    }

    private void positionWindow(JInternalFrame window, Rectangle screenBounds,
                                int defaultX, int defaultY,
                                int defaultWidth, int defaultHeight) {
        if (window == null) return;

        int x = Math.max(0, Math.min(defaultX, screenBounds.width - defaultWidth - 50));
        int y = Math.max(0, Math.min(defaultY, screenBounds.height - defaultHeight - 50));
        int width = Math.min(defaultWidth, screenBounds.width - x - 50);
        int height = Math.min(defaultHeight, screenBounds.height - y - 50);

        window.setBounds(x, y, width, height);
    }

    private void checkAndLoadProfile() {
        if (ProfileManager.hasSavedProfiles()) {
            Object[] options = {
                    bundle.getString("yesButtonText"),
                    bundle.getString("noButtonText")
            };

            int option = JOptionPane.showOptionDialog(
                    this,
                    bundle.getString("loadProfileQuestion"),
                    bundle.getString("loadProfile"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (option == JOptionPane.YES_OPTION) {
                loadSelectedProfile();
            }
        }
    }

    private void loadSelectedProfile() {
        String profileName = (String) JOptionPane.showInputDialog(
                this,
                bundle.getString("selectProfileToLoad"),
                bundle.getString("loadProfile"),
                JOptionPane.QUESTION_MESSAGE,
                null,
                ProfileManager.getAvailableProfiles().toArray(),
                null
        );

        if (profileName != null && !profileName.isEmpty()) {
            try {
                Map<String, Object> states = ProfileManager.loadProfile(profileName);
                applyProfileState(states);
                revalidate();
                repaint();
            } catch (Exception e) {
                handleProfileLoadError(e);
            }
        }
    }

    private void applyProfileState(Map<String, Object> states) {
        if (states == null) return;

        updateLocaleFromProfile(states);
        applyWindowStates(states);
        setupMenuBar();
    }

    private void updateLocaleFromProfile(Map<String, Object> states) {
        if (states == null) return;

        String savedLanguage = (String) states.get("language");
        if (savedLanguage == null) return;

        Locale newLocale;
        if (savedLanguage.equals("ru") || savedLanguage.equals("ru_RU")) {
            newLocale = new Locale("ru", "RU");
        } else {
            newLocale = Locale.ENGLISH;
        }

        if (!newLocale.equals(currentLocale)) {
            currentLocale = newLocale;
            this.bundle = ResourceBundle.getBundle("messages", currentLocale);
        }
    }

    private void applyWindowStates(Map<String, Object> states) {
        if (states == null) return;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();

        if (logWindow == null || logWindow.isClosed()) {
            logWindow = createLogWindow();
            desktopPane.add(logWindow);
        }
        if (gameWindow == null || gameWindow.isClosed()) {
            gameWindow = createGameWindow();
            desktopPane.add(gameWindow);
        }

        restoreWindowState((Map<String, Object>) states.get("logWindow"), screenBounds, true);
        restoreWindowState((Map<String, Object>) states.get("gameWindow"), screenBounds, false);

        SwingUtilities.invokeLater(() -> {
            applySpecialStates(states);
        });
    }

    private void restoreWindowState(Map<String, Object> state,
                                    Rectangle screenBounds,
                                    boolean isLogWindow) {
        if (state == null) return;

        JInternalFrame window = isLogWindow ? logWindow : gameWindow;
        if (window == null) return;

        try {
            resetWindowState(window);

            int defaultWidth = isLogWindow ? 400 : 800;
            int defaultHeight = isLogWindow ? 500 : 600;
            int minSize = isLogWindow ? 300 : 400;

            int x = calculateSafeCoordinate(getIntValue(state.get("x")),
                    screenBounds.width, defaultWidth);
            int y = calculateSafeCoordinate(getIntValue(state.get("y")),
                    screenBounds.height, defaultHeight);
            int width = calculateSafeSize(getIntValue(state.get("width")),
                    minSize, screenBounds.width - x, defaultWidth);
            int height = calculateSafeSize(getIntValue(state.get("height")),
                    minSize, screenBounds.height - y, defaultHeight);

            window.setBounds(x, y, width, height);
            window.setVisible(Boolean.TRUE.equals(state.get("visible")));

            if (window.getParent() == null) {
                desktopPane.add(window);
            }

            if (window instanceof BasicWindow) {
                ((BasicWindow) window).setTranslatedTitle(bundle);
            }
        } catch (Exception e) {
            Logger.error("Error restoring window state: " + e.getMessage());
        }
    }

    private void resetWindowState(JInternalFrame window) throws PropertyVetoException {
        if (window.isMaximum()) window.setMaximum(false);
        if (window.isIcon()) window.setIcon(false);
    }

    private void applySpecialStates(Map<String, Object> states) {
        applyWindowSpecialState((Map<String, Object>) states.get("logWindow"), logWindow);
        applyWindowSpecialState((Map<String, Object>) states.get("gameWindow"), gameWindow);
    }

    private void applyWindowSpecialState(Map<String, Object> state, JInternalFrame window) {
        if (state == null || window == null) return;

        try {
            if (Boolean.TRUE.equals(state.get("maximized"))) {
                window.setMaximum(true);
            } else if (Boolean.TRUE.equals(state.get("iconified"))) {
                window.setIcon(true);
            }
        } catch (PropertyVetoException e) {
            Logger.error("Can't apply special state: " + e.getMessage());
        }
    }

    private void setupMenuBar() {
        ApplicationMenuBar menuBar = new ApplicationMenuBar(bundle, this);
        setJMenuBar(menuBar.getMenuBar());
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    confirmAndClose();
                } catch (Exception ex) {
                    Logger.error("Error during window closing: " + ex.getMessage());
                }
            }
        });
    }

    protected LogWindow createLogWindow() {
        LogWindow window = new LogWindow(logSource, bundle);
        Logger.debug(bundle.getString("logMessage"));
        window.setClosable(true);
        window.setResizable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        return window;
    }

    protected GameWindow createGameWindow() {
        GameWindow window = new GameWindow(bundle);
        window.setClosable(true);
        window.setResizable(true);
        window.setMaximizable(true);
        window.setIconifiable(true);
        window.setMinimumSize(new Dimension(400, 300));
        return window;
    }

    public void saveCurrentState(String profileName) throws PropertyVetoException, IOException {
        Map<String, Object> states = new HashMap<>();
        states.put("language", currentLocale.toString());

        if (logWindow != null) {
            states.put("logWindow", getWindowStateWithFocus(logWindow, true));
        }
        if (gameWindow != null) {
            states.put("gameWindow", getWindowStateWithFocus(gameWindow, false));
        }

        ProfileManager.saveProfile(profileName, states);
    }

    private Map<String, Object> getWindowStateWithFocus(JInternalFrame window, boolean isFocused)
            throws PropertyVetoException {
        Map<String, Object> state = WindowStateManager.getWindowState(window);
        state.put("focused", isFocused && window.isSelected());
        return state;
    }

    private void confirmAndClose() {
        Object[] options = {
                bundle.getString("yesButtonText"),
                bundle.getString("noButtonText")
        };

        int option = JOptionPane.showOptionDialog(
                this,
                bundle.getString("saveBeforeExit"),
                bundle.getString("exitConfirmation"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (option == JOptionPane.YES_OPTION) {
            boolean saved = saveProfileWithValidation();
            if (saved) {
                System.exit(0);
            }
            // Если сохранение не удалось, остаемся в приложении
        } else {
            System.exit(0);
        }
    }

    boolean saveProfileWithValidation() {
        String profileName;
        do {
            profileName = JOptionPane.showInputDialog(
                    this,
                    bundle.getString("enterProfileName"),
                    bundle.getString("saveProfileTitle"),
                    JOptionPane.QUESTION_MESSAGE
            );

            if (profileName == null) {
                return false; // Пользователь отменил ввод
            }

            if (!ProfileManager.isValidProfileName(profileName)) {
                JOptionPane.showMessageDialog(
                        this,
                        bundle.getString("invalidName"),
                        bundle.getString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE
                );
                continue;
            }

            try {
                saveCurrentState(profileName);
                JOptionPane.showMessageDialog(
                        this,
                        bundle.getString("profileSavedSuccess"),
                        bundle.getString("successTitle"),
                        JOptionPane.INFORMATION_MESSAGE
                );
                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        bundle.getString("saveError") + e.getMessage(),
                        bundle.getString("errorTitle"),
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        } while (true);
    }

    public void updateLocale(Locale locale) {
        Map<String, Object> windowStates = saveCurrentWindowStates();
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("messages", locale);
        recreateUI(windowStates);
    }

    private Map<String, Object> saveCurrentWindowStates() {
        Map<String, Object> states = new HashMap<>();
        try {
            if (logWindow != null) {
                states.put("logWindow", getWindowStateWithFocus(logWindow, true));
            }
            if (gameWindow != null) {
                states.put("gameWindow", getWindowStateWithFocus(gameWindow, false));
            }
        } catch (Exception e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
        return states;
    }

    private void recreateUI(Map<String, Object> windowStates) {
        boolean wasLogVisible = logWindow != null && logWindow.isVisible();
        boolean wasGameVisible = gameWindow != null && gameWindow.isVisible();

        if (logWindow != null) {
            desktopPane.remove(logWindow);
            logWindow.dispose();
        }
        if (gameWindow != null) {
            desktopPane.remove(gameWindow);
            gameWindow.dispose();
        }

        logWindow = createLogWindow();
        gameWindow = createGameWindow();

        try {
            if (windowStates != null) {
                applyWindowStates(windowStates);
            } else {
                if (wasLogVisible) {
                    logWindow.setVisible(true);
                    desktopPane.add(logWindow);
                }
                if (wasGameVisible) {
                    gameWindow.setVisible(true);
                    desktopPane.add(gameWindow);
                }
            }
        } catch (Exception e) {
            Logger.error("Error recreating UI: " + e.getMessage());
        }

        setupMenuBar();
        revalidate();
        repaint();
    }

    public void addWindow(JInternalFrame frame) {
        if (frame instanceof LogWindow) {
            handleLogWindowAddition((LogWindow) frame);
        } else if (frame instanceof GameWindow) {
            handleGameWindowAddition((GameWindow) frame);
        } else {
            desktopPane.add(frame);
            frame.setVisible(true);
        }
    }

    private void handleLogWindowAddition(LogWindow frame) {
        if (logWindow != null && logWindow.isVisible() && !logWindow.isClosed()) {
            logWindow.toFront();
            return;
        }
        logWindow = frame;
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private void handleGameWindowAddition(GameWindow frame) {
        if (gameWindow != null && gameWindow.isVisible() && !gameWindow.isClosed()) {
            gameWindow.toFront();
            return;
        }
        gameWindow = frame;
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
            gameWindow = createGameWindow();
        }
        addWindow(gameWindow);
    }

    private void handleProfileLoadError(Exception e) {
        Logger.error("Error loading profile: " + e.getMessage());
        JOptionPane.showMessageDialog(
                this,
                bundle.getString("loadError") + e.getMessage(),
                bundle.getString("errorTitle"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    private int getIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private int calculateSafeCoordinate(int value, int screenSize, int windowSize) {
        return Math.max(0, Math.min(value, screenSize - windowSize));
    }

    private int calculateSafeSize(int value, int minSize, int maxSize, int defaultSize) {
        if (value <= 0) return defaultSize;
        return Math.max(minSize, Math.min(value, maxSize));
    }
}