package gui;
import gui.profiling.ProfileManager;
import gui.profiling.WindowStateManager;
import gui.windows.GameWindow;
import gui.windows.LogWindow;
import log.LogWindowSource;
import log.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    public ResourceBundle bundle;
    public final LogWindowSource logSource;
    private Locale currentLocale;
    private final Map<String, JInternalFrame> windows = new HashMap<>();
    public MainApplicationFrame(ResourceBundle bundle, LogWindowSource logSource) {
        this.bundle = bundle;
        this.logSource = logSource;
        this.currentLocale = bundle.getLocale();
        initializeFrameSettings();
        createAndPositionDefaultWindows();
        ProfileManager.checkAndLoadProfile(this, bundle);
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
        LogWindow logWindow = WindowStateManager.createLogWindow(logSource, bundle);
        GameWindow gameWindow = WindowStateManager.createGameWindow(bundle);
        WindowStateManager.positionWindow(logWindow, screenBounds, 50, 50, 400, 500);
        WindowStateManager.positionWindow(gameWindow, screenBounds, 470, 50, 800, 600);
        addWindow("logWindow", logWindow);
        addWindow("gameWindow", gameWindow);
    }
    private void setupMenuBar() {
        ApplicationMenuBar menuBar = new ApplicationMenuBar(bundle, this);
        setJMenuBar(menuBar.getMenuBar());
    }
    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ProfileManager.confirmAndClose(MainApplicationFrame.this, bundle);
            }
        });
    }
    public void saveCurrentState(String profileName) throws Exception {
        Map<String, Object> states = new HashMap<>();
        states.put("language", currentLocale.toString());
        for (Map.Entry<String, JInternalFrame> entry : windows.entrySet()) {
            states.put(entry.getKey(), WindowStateManager.getWindowStateWithFocus(entry.getValue(), entry.getKey().equals("logWindow")));
        }

        if (windows.containsKey("gameWindow")) {
            GameWindow gameWindow = (GameWindow) windows.get("gameWindow");
            states.put("playerCoins", gameWindow.getPlayer().getCoins());
            for (gui.PlayerMechanics.ShopUpgradeType upgradeType : gui.PlayerMechanics.ShopUpgradeType.values()) {
                states.put(upgradeType.name() + "Level", gameWindow.getPlayer().getShopUpgradeLevel(upgradeType));
            }
        }

        ProfileManager.saveProfile(profileName, states);
    }

    public void applyProfileState(Map<String, Object> states) {
        if (states == null) return;

        updateLocaleFromProfile(states);
        WindowStateManager.applyWindowStates(this, states);

        if (windows.containsKey("gameWindow")) {
            GameWindow gameWindow = (GameWindow) windows.get("gameWindow");
            if (states.containsKey("playerCoins")) {
                int coins = ((Number) states.get("playerCoins")).intValue();
                gameWindow.getPlayer().addCoins(coins - gameWindow.getPlayer().getCoins());
            }
            for (gui.PlayerMechanics.ShopUpgradeType upgradeType : gui.PlayerMechanics.ShopUpgradeType.values()) {
                String key = upgradeType.name() + "Level";
                if (states.containsKey(key)) {
                    int level = ((Number) states.get(key)).intValue();
                    for (int i = gameWindow.getPlayer().getShopUpgradeLevel(upgradeType); i < level; i++) {
                        gameWindow.getPlayer().applyShopUpgrade(upgradeType);
                    }
                }
            }
        }

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
            updateLocale(newLocale);
        }
    }
    public void updateLocale(Locale locale) {
        Map<String, Object> windowStates = WindowStateManager.saveCurrentWindowStates(this);
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle("messages", locale);
        recreateUI(windowStates);
    }
    private void recreateUI(Map<String, Object> windowStates) {
        boolean wasLogVisible = windows.containsKey("logWindow") && windows.get("logWindow").isVisible();
        boolean wasGameVisible = windows.containsKey("gameWindow") && windows.get("gameWindow").isVisible();
        for (JInternalFrame window : windows.values()) {
            desktopPane.remove(window);
            window.dispose();
        }
        windows.clear();
        LogWindow logWindow = WindowStateManager.createLogWindow(logSource, bundle);
        GameWindow gameWindow = WindowStateManager.createGameWindow(bundle);
        windows.put("logWindow", logWindow);
        windows.put("gameWindow", gameWindow);
        try {
            if (windowStates != null) {
                WindowStateManager.applyWindowStates(this, windowStates);
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
    public void addWindow(String windowId, JInternalFrame frame) {
        if (windows.containsKey(windowId) && windows.get(windowId).isVisible() && !windows.get(windowId).isClosed()) {
            windows.get(windowId).toFront();
        } else {
            windows.put(windowId, frame);
            desktopPane.add(frame);
            frame.setVisible(true);
        }
    }
    public void showLogWindow() {
        if (!windows.containsKey("logWindow") || windows.get("logWindow").isClosed()) {
            LogWindow logWindow = WindowStateManager.createLogWindow(logSource, bundle);
            addWindow("logWindow", logWindow);
        } else {
            windows.get("logWindow").toFront();
        }
    }
    public void showGameWindow() {
        if (!windows.containsKey("gameWindow") || windows.get("gameWindow").isClosed()) {
            GameWindow gameWindow = WindowStateManager.createGameWindow(bundle);
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();
            WindowStateManager.positionWindow(gameWindow, screenBounds, 470, 50, 800, 600);
            addWindow("gameWindow", gameWindow);
        } else {
            windows.get("gameWindow").toFront();
        }
    }

    public void resetPlayerCoins() {
        if (windows.containsKey("gameWindow")) {
            GameWindow gameWindow = (GameWindow) windows.get("gameWindow");
            gameWindow.getPlayer().addCoins(-gameWindow.getPlayer().getCoins());
        }
    }

    public Map<String, JInternalFrame> getInternalWindows() {
        return windows;
    }
}