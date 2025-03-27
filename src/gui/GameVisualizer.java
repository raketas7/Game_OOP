package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

public class GameVisualizer extends JPanel implements KeyListener, ComponentListener {

    static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static final int MAP_SIZE = 3200;      // Размер карты
    public static final double SPEED = 5.0;       // Скорость движения
    public static final int ROBOT_SIZE = 30;      // Размер робота
    public static final int BORDER_PADDING = 5;   // Граница, за которую робот не должен заходить

    private int windowWidth;  // Текущая ширина окна
    private int windowHeight; // Текущая высота окна

    public double offsetX;   // Смещение камеры
    public double offsetY;

    public double robotX = MAP_SIZE / 2.0;        // Позиция робота
    public double robotY = MAP_SIZE / 2.0;

    public final Set<Integer> activeKeys = new HashSet<>(); // Нажатые клавиши
    private BufferedImage backgroundImage;

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public GameVisualizer() {
        updateWindowSize();
        setPreferredSize(new Dimension(windowWidth, windowHeight));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addComponentListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        setDoubleBuffered(true);

        try {
            backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Resource Bundle 'textures'/background.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateCameraOffset();

        Timer moveTimer = new Timer(true);
        moveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                moveRobotAndCamera();
            }
        }, 0, 20);
    }

    private void updateWindowSize() {
        windowWidth = getWidth() > 0 ? getWidth() : screenSize.width;
        windowHeight = getHeight() > 0 ? getHeight() : screenSize.height;
    }

    public void updateCameraOffset() {
        offsetX = Math.max(0, Math.min(robotX - windowWidth / 2.0 + ROBOT_SIZE / 2.0, MAP_SIZE - windowWidth));
        offsetY = Math.max(0, Math.min(robotY - windowHeight / 2.0 + ROBOT_SIZE / 2.0, MAP_SIZE - windowHeight));
    }

    public void moveRobotAndCamera() {
        double dx = 0, dy = 0;

        if (activeKeys.contains(KeyEvent.VK_W) || activeKeys.contains(KeyEvent.VK_UP)) {
            dy -= SPEED;
        }
        if (activeKeys.contains(KeyEvent.VK_S) || activeKeys.contains(KeyEvent.VK_DOWN)) {
            dy += SPEED;
        }
        if (activeKeys.contains(KeyEvent.VK_A) || activeKeys.contains(KeyEvent.VK_LEFT)) {
            dx -= SPEED;
        }
        if (activeKeys.contains(KeyEvent.VK_D) || activeKeys.contains(KeyEvent.VK_RIGHT)) {
            dx += SPEED;
        }

        if (dx != 0 && dy != 0) {
            double normFactor = Math.sqrt(2) / 2;
            dx *= normFactor;
            dy *= normFactor;
        }

        robotX = Math.max(BORDER_PADDING, Math.min(robotX + dx, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));
        robotY = Math.max(BORDER_PADDING, Math.min(robotY + dy, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));

        updateCameraOffset();
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, -(int) offsetX, -(int) offsetY, null);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, 0, MAP_SIZE, MAP_SIZE);
        }

        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, MAP_SIZE, MAP_SIZE);

        drawRobot(g2d, (int) (robotX - offsetX), (int) (robotY - offsetY));
    }

    private void drawRobot(Graphics2D g, int x, int y) {
        g.setColor(Color.RED);
        g.fillOval(x, y, ROBOT_SIZE, ROBOT_SIZE);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, ROBOT_SIZE, ROBOT_SIZE);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        activeKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void componentResized(ComponentEvent e) {
        updateWindowSize();
        updateCameraOffset();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}
