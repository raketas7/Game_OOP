package gui;

import gui.Enemies.Enemy;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class GameVisualizer extends JPanel implements KeyListener, ComponentListener {
    public static final int MAP_SIZE = 3200;
    public static final int BORDER_PADDING = 5;
    public static final int ENEMY_SPAWN_RADIUS = 400;

    private final ResourceBundle bundle;
    private int windowWidth;
    private int windowHeight;
    private double cameraOffsetX;
    private double cameraOffsetY;
    private final Player player;
    private final Set<Integer> activeKeys = new HashSet<>();
    private BufferedImage backgroundImage;
    private final List<Enemy> enemies = Collections.synchronizedList(new ArrayList<>());
    private final WaveManager waveManager;

    public GameVisualizer(ResourceBundle bundle) {
        this.bundle = bundle;
        player = new Player(MAP_SIZE / 2.0, MAP_SIZE / 2.0);
        waveManager = new WaveManager();
        initUI();
        loadResources();
        setupTimers();
    }

    public Set<Integer> getActiveKeys() { return activeKeys; }
    public int getWindowWidth() { return windowWidth; }
    public int getWindowHeight() { return windowHeight; }
    public double getCameraOffsetX() { return cameraOffsetX; }
    public double getCameraOffsetY() { return cameraOffsetY; }
    public List<Enemy> getEnemies() { return enemies; }
    public Player getPlayer() { return player; }
    public WaveManager getWaveManager() { return waveManager; }

    private void initUI() {
        updateWindowSize();
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
    }

    private void loadResources() {
        try {
            backgroundImage = ImageIO.read(Objects.requireNonNull(
                    getClass().getResource("/Resource Bundle 'textures'/background.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTimers() {
        new Timer(true).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                movePlayer();
                updateCamera();
                moveEnemies();
                checkCollisions();
                repaint();
                checkWaveSpawning();
            }
        }, 0, 20);
    }

    protected void updateWindowSize() {
        windowWidth = getWidth() > 0 ? getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
        windowHeight = getHeight() > 0 ? getHeight() : Toolkit.getDefaultToolkit().getScreenSize().height;
    }

    protected void movePlayer() {
        double dx = 0, dy = 0;

        if (activeKeys.contains(KeyEvent.VK_W)) dy -= Player.calculateNormalizedSpeed(0, dy);
        if (activeKeys.contains(KeyEvent.VK_S)) dy += Player.calculateNormalizedSpeed(0, dy);
        if (activeKeys.contains(KeyEvent.VK_A)) dx -= Player.calculateNormalizedSpeed(dx, 0);
        if (activeKeys.contains(KeyEvent.VK_D)) dx += Player.calculateNormalizedSpeed(dx, 0);

        player.move(dx, dy, MAP_SIZE);
    }

    protected void updateCamera() {
        double targetX = player.getX() - windowWidth / 2.0 + Player.SIZE / 2.0;
        double targetY = player.getY() - windowHeight / 2.0 + Player.SIZE / 2.0;

        cameraOffsetX = Math.max(0, Math.min(targetX, MAP_SIZE - windowWidth));
        cameraOffsetY = Math.max(0, Math.min(targetY, MAP_SIZE - windowHeight));
    }

    private void checkWaveSpawning() {
        if (waveManager.shouldSpawnWave()) {
            waveManager.startNextWave();
            synchronized(enemies) {
                enemies.addAll(waveManager.spawnEnemies(
                        player.getX(), player.getY(), MAP_SIZE, enemies
                ));
            }
        }
    }

    private void moveEnemies() {
        synchronized(enemies) {
            for (Enemy enemy : enemies) {
                enemy.move(player.getX(), player.getY(), enemies);
            }
        }
    }

    protected void checkCollisions() {
        synchronized(enemies) {
            Iterator<Enemy> iterator = enemies.iterator();
            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();
                Rectangle enemyBounds = enemy.getBounds(cameraOffsetX, cameraOffsetY);
                Rectangle playerBounds = player.getBounds(cameraOffsetX, cameraOffsetY);

                if (enemyBounds.intersects(playerBounds)) {
                    iterator.remove();
                    waveManager.enemyDied();
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Рисуем карту
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, -(int)cameraOffsetX, -(int)cameraOffsetY, null);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(-(int)cameraOffsetX, -(int)cameraOffsetY, MAP_SIZE, MAP_SIZE);
        }

        // Рисуем границы карты
        g2d.setColor(Color.RED);
        g2d.drawRect(-(int)cameraOffsetX, -(int)cameraOffsetY, MAP_SIZE, MAP_SIZE);

        // Рисуем врагов
        synchronized(enemies) {
            for (Enemy enemy : enemies) {
                enemy.draw(g2d, cameraOffsetX, cameraOffsetY);
            }
        }

        // Рисуем игрока
        player.draw(g2d, cameraOffsetX, cameraOffsetY);

        // Отображаем информацию о волне
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(bundle.getString("waves") + waveManager.getCurrentWave(), 20, 30);
        g2d.drawString(bundle.getString("enemies") + waveManager.getEnemiesAlive(), 20, 60);
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
        updateCamera();
        repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}
}