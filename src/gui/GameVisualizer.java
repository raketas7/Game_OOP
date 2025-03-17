package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class GameVisualizer extends JPanel implements KeyListener {

    public static final int WINDOW_WIDTH = 1524;  // Размер окна
    public static final int WINDOW_HEIGHT = 739;
    public static final int MAP_SIZE = 3200;      // Размер карты
    public static final double SPEED = 5.0;       // Скорость движения
    public static final int ROBOT_SIZE = 30;      // Размер робота
    public static final int BORDER_PADDING = 5;   // Граница, за которую не должен заходить робот

    public double offsetX = WINDOW_WIDTH / 2.0;   // Начальное смещение камеры
    public double offsetY = WINDOW_HEIGHT / 2.0;

    public double robotX = MAP_SIZE / 2.0;        // Начальная позиция робота
    public double robotY = MAP_SIZE / 2.0;

    public final Set<Integer> activeKeys = new HashSet<>(); // Нажатые клавиши
    private final Timer moveTimer;
    private BufferedImage backgroundImage;

    public GameVisualizer() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Фокусировка при клике
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        setDoubleBuffered(true);

        // Загрузка фонового изображения
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/Resource Bundle 'textures'/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Таймер для обновления движения
        moveTimer = new Timer(true);
        moveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                moveRobotAndCamera();
            }
        }, 0, 20); // Обновление каждые 20 мс
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

        // Нормализация скорости по диагонали (чтобы не было быстрее, чем по прямой)
        if (dx != 0 && dy != 0) {
            double normFactor = Math.sqrt(2) / 2;
            dx *= normFactor;
            dy *= normFactor;
        }

        // Двигаем робота
        robotX += dx;
        robotY += dy;

        // Ограничиваем движение робота в пределах карты с учетом границы в 5 пикселей
        robotX = Math.max(BORDER_PADDING, Math.min(robotX, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));
        robotY = Math.max(BORDER_PADDING, Math.min(robotY, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));

        // Двигаем камеру в зависимости от положения робота
        double targetOffsetX = robotX - WINDOW_WIDTH / 2.0;
        double targetOffsetY = robotY - WINDOW_HEIGHT / 2.0;

        // Ограничиваем движение камеры в пределах карты
        offsetX = Math.max(0, Math.min(targetOffsetX, MAP_SIZE - WINDOW_WIDTH));
        offsetY = Math.max(0, Math.min(targetOffsetY, MAP_SIZE - WINDOW_HEIGHT));

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        // Рисуем фон с учетом смещения
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, -(int) offsetX, -(int) offsetY, null);
        }

        // Рисуем робота в текущей позиции
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
        activeKeys.add(e.getKeyCode()); // Добавляем клавишу в активные
    }

    @Override
    public void keyReleased(KeyEvent e) {
        activeKeys.remove(e.getKeyCode()); // Убираем клавишу из активных
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Не требуется
    }
}
