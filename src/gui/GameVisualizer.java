package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

public class GameVisualizer extends JPanel implements KeyListener {

    public static final int MAP_SIZE = 3200;      // Размер карты в пикселях
    public static final double SPEED = 5.0;       // Скорость движения
    public static final int ROBOT_SIZE = 30;      // Размер робота
    public static final int BORDER_PADDING = 5;   // Граница, за которую не должен заходить робот

    public double offsetX;   // Смещение камеры
    private double offsetY;
    public double robotX = MAP_SIZE / 2.0;        // Позиция робота
    public double robotY = MAP_SIZE / 2.0;

    public final Set<Integer> activeKeys = new HashSet<>(); // Нажатые клавиши
    private BufferedImage backgroundImage;
    private Dimension lastSize; // Последний известный размер панели

    public GameVisualizer() {
        // Устанавливаем начальный размер панели
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);
        lastSize = screenSize;

        // Начальное смещение камеры (центрируем на роботе)
        offsetX = robotX - screenSize.width / 2.0;
        offsetY = robotY - screenSize.height / 2.0;

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
            backgroundImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("/Resource Bundle 'textures'/background.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Таймер для обновления движения
        Timer moveTimer = new Timer(true);
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

        // Нормализация скорости по диагонали
        if (dx != 0 && dy != 0) {
            double normFactor = Math.sqrt(2) / 2;
            dx *= normFactor;
            dy *= normFactor;
        }

        // Двигаем робота
        robotX += dx;
        robotY += dy;

        // Ограничиваем движение робота в пределах карты с учетом границы
        robotX = Math.max(BORDER_PADDING, Math.min(robotX, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));
        robotY = Math.max(BORDER_PADDING, Math.min(robotY, MAP_SIZE - ROBOT_SIZE - BORDER_PADDING));

        // Обновляем размер панели
        Dimension currentSize = getSize();
        if (!currentSize.equals(lastSize)) {
            lastSize = currentSize;
        }

        // Двигаем камеру в зависимости от положения робота
        double targetOffsetX = robotX - currentSize.width / 2.0;
        double targetOffsetY = robotY - currentSize.height / 2.0;

        // Ограничиваем движение камеры в пределах карты
        offsetX = Math.max(0, Math.min(targetOffsetX, MAP_SIZE - currentSize.width));
        offsetY = Math.max(0, Math.min(targetOffsetY, MAP_SIZE - currentSize.height));

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Рисуем фон с учетом смещения
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, -(int) offsetX, -(int) offsetY, null);
        }

        // Рисуем робота в текущей позиции (относительно камеры)
        int robotScreenX = (int) (robotX - offsetX);
        int robotScreenY = (int) (robotY - offsetY);
        drawRobot(g2d, robotScreenX, robotScreenY);
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
    public void keyTyped(KeyEvent e) {
        // Не требуется
    }

    public int getHeight() {
        return lastSize.height;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Game Visualizer");
            GameVisualizer game = new GameVisualizer();
            frame.add(game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Максимальный размер окна
            frame.setUndecorated(true); // Убираем рамку окна (опционально)
            frame.pack();
            frame.setVisible(true);
        });
    }
}