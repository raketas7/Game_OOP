package gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import log.Logger;

public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private ResourceBundle bundle;

    public MainApplicationFrame(ResourceBundle bundle) {
        this.bundle = bundle;
        initializeUI();
    }

    private void initializeUI() {
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset,
                screenSize.width - inset * 2,
                screenSize.height - inset * 2);

        setContentPane(desktopPane);

        // создание и добавление окна логов
        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        // создание и добавление игрового окна
        GameWindow gameWindow = new GameWindow(bundle);
        gameWindow.setSize(1536, 770);
        addWindow(gameWindow);

        // создание и установка меню
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

    // создает и настраивает окно логов
    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource(), bundle);
        logWindow.setLocation(10, 10);
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug(bundle.getString("logMessage"));
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    // обновление локали с сохранением размера окна
    public void updateLocale(Locale locale) {
        Rectangle bounds = getBounds();

        this.bundle = ResourceBundle.getBundle("messages", locale);
        recreateUI();

        setBounds(bounds);
    }

    // Пересоздание интерфейса
    private void recreateUI() {
        getContentPane().removeAll(); // удаление
        initializeUI(); // инициализация
        revalidate(); // обновление
        repaint();
    }

    private void exitApplication() {
        System.exit(0);
    }

    private void confirmAndClose() {
        int option = JOptionPane.showConfirmDialog(
                this,
                bundle.getString("confirmExit"),
                bundle.getString("confirmClose"),
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            exitApplication();
        }
    }
}