package gui.Visuals;

import gui.Enemies.Enemy;
import gui.PlayerMechanics.Player;
import gui.PlayerMechanics.Bullet;
import gui.PlayerMechanics.ShopUpgradeType;
import gui.PlayerMechanics.UpgradeType;
import gui.WaveManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class GameVisualizer extends JPanel implements KeyListener, ComponentListener, MouseMotionListener {
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
    private final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<>());
    private final WaveManager waveManager;
    private int mouseX;
    private int mouseY;
    private boolean isPaused = false;
    private boolean upgradeSelectionMode = false;
    private List<UpgradeType> offeredUpgrades = new ArrayList<>();
    private boolean gameOver = false;
    private int frameCounter = 0;
    private int countdown = 10;
    private Timer countdownTimer;
    private enum GameState { START_SCREEN, PLAYING, GAME_OVER, SHOP }
    private GameState gameState = GameState.START_SCREEN;
    private JButton startButton;
    private JButton shopButton;
    private JPanel shopPanel;

    public GameVisualizer(ResourceBundle bundle) {
        this.bundle = bundle;
        player = new Player(MAP_SIZE / 2.0, MAP_SIZE / 2.0);
        waveManager = new WaveManager();
        initUI();
        loadResources();
        setupTimers();
        initStartButton();
        initShopButton();
        initShopPanel();
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
        setLayout(null);
        updateWindowSize();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addComponentListener(this);
        addMouseMotionListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                if (gameState == GameState.START_SCREEN) {
                    startButton.requestFocusInWindow();
                    shopButton.requestFocusInWindow();
                }
            }
        });

        setDoubleBuffered(true);
    }

    private void initStartButton() {
        startButton = new JButton(bundle.getString("startButtonText"));
        startButton.setFont(new Font("Arial", Font.BOLD, 24));
        startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 50, 200, 50);
        startButton.setVisible(true);
        startButton.addActionListener(e -> {
            gameState = GameState.PLAYING;
            startButton.setVisible(false);
            shopButton.setVisible(false);
            requestFocusInWindow();
            repaint();
        });
        add(startButton);
    }

    private void initShopButton() {
        shopButton = new JButton(bundle.getString("shopButtonText"));
        shopButton.setFont(new Font("Arial", Font.BOLD, 24));
        shopButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 110, 200, 50);
        shopButton.setVisible(true);
        shopButton.addActionListener(e -> {
            gameState = GameState.SHOP;
            startButton.setVisible(false);
            shopButton.setVisible(false);
            shopPanel.setVisible(true);
            updateShopButtons();
            requestFocusInWindow();
            repaint();
        });
        add(shopButton);
    }

    private void initShopPanel() {
        shopPanel = new JPanel();
        shopPanel.setLayout(new GridLayout(player.getShop().getUpgrades().size() + 1, 1, 10, 10));
        shopPanel.setBounds(windowWidth / 2 - 150, windowHeight / 2 - 100, 300, 50 * (player.getShop().getUpgrades().size() + 1));
        shopPanel.setOpaque(false);

        for (ShopUpgradeType upgradeType : player.getShop().getUpgrades().keySet()) {
            JButton button = new JButton();
            updateShopButtonText(button, upgradeType);
            button.addActionListener(e -> purchaseUpgrade(upgradeType, button));
            shopPanel.add(button);
        }

        JButton backButton = new JButton(bundle.getString("backButtonText"));
        backButton.addActionListener(e -> {
            gameState = GameState.START_SCREEN;
            shopPanel.setVisible(false);
            startButton.setVisible(true);
            shopButton.setVisible(true);
            repaint();
        });
        shopPanel.add(backButton);
        shopPanel.setVisible(false);
        add(shopPanel);
    }

    private void updateShopButtonText(JButton button, ShopUpgradeType upgrade) {
        int level = player.getShopUpgradeLevel(upgrade);
        int cost = player.getShopUpgradeCost(upgrade);
        boolean canUpgrade = player.canUpgrade(upgrade);
        button.setText(String.format("%s (Level %d, Cost: %d)",
                bundle.getString(upgrade.getDescriptionKey()), level, canUpgrade ? cost : 0));
        button.setEnabled(canUpgrade && player.canAffordShopUpgrade(upgrade));
    }

    private void updateShopButtons() {
        int index = 0;
        for (Component comp : shopPanel.getComponents()) {
            if (comp instanceof JButton && !comp.equals(shopPanel.getComponent(shopPanel.getComponentCount() - 1))) {
                ShopUpgradeType upgrade = player.getShop().getUpgrades().keySet().toArray(new ShopUpgradeType[0])[index];
                updateShopButtonText((JButton) comp, upgrade);
                index++;
            }
        }
    }

    private void purchaseUpgrade(ShopUpgradeType upgrade, JButton button) {
        player.purchaseUpgrade(upgrade);
        updateShopButtonText(button, upgrade);
        updateShopButtons();
        repaint();
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
                if (gameState == GameState.PLAYING && !isPaused && !gameOver) {
                    movePlayer();
                    updateCamera();
                    shoot();
                    updateBullets();
                    moveEnemies();
                    checkCollisions();
                    frameCounter++;
                    if (frameCounter >= 5) {
                        player.regenerateHealth(1);
                        frameCounter = 0;
                    }
                    repaint();
                    checkWaveSpawning();
                } else if (gameState == GameState.GAME_OVER && countdown > 0) {
                    repaint();
                }
            }
        }, 0, 20);

        countdownTimer = new Timer(true);
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (gameState == GameState.GAME_OVER && countdown > 0) {
                    countdown--;
                    repaint();
                    if (countdown == 0) {
                        resetGame();
                    }
                }
            }
        }, 0, 1000);
    }

    private void resetGame() {
        player.reset();
        enemies.clear();
        bullets.clear();
        waveManager.reset();
        gameOver = false;
        isPaused = false;
        countdown = 10;
        gameState = GameState.START_SCREEN;
        startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 50, 200, 50);
        shopButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 110, 200, 50);
        startButton.setVisible(true);
        shopButton.setVisible(true);
        shopPanel.setVisible(false);
        repaint();
    }

    protected void updateWindowSize() {
        windowWidth = getWidth() > 0 ? getWidth() : Toolkit.getDefaultToolkit().getScreenSize().width;
        windowHeight = getHeight() > 0 ? getHeight() : Toolkit.getDefaultToolkit().getScreenSize().height;
        if (startButton != null) {
            startButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 50, 200, 50);
        }
        if (shopButton != null) {
            shopButton.setBounds(windowWidth / 2 - 100, windowHeight / 2 + 110, 200, 50);
        }
        if (shopPanel != null) {
            shopPanel.setBounds(windowWidth / 2 - 150, windowHeight / 2 - 100, 300, 50 * (player.getShop().getUpgrades().size() + 1));
        }
    }

    protected void movePlayer() {
        double dx = 0, dy = 0;

        if (activeKeys.contains(KeyEvent.VK_W)) dy -= Player.calculateNormalizedSpeed(0, dy) * player.getSpeed();
        if (activeKeys.contains(KeyEvent.VK_S)) dy += Player.calculateNormalizedSpeed(0, dy) * player.getSpeed();
        if (activeKeys.contains(KeyEvent.VK_A)) dx -= Player.calculateNormalizedSpeed(dx, 0) * player.getSpeed();
        if (activeKeys.contains(KeyEvent.VK_D)) dx += Player.calculateNormalizedSpeed(dx, 0) * player.getSpeed();

        player.move(dx, dy, MAP_SIZE);
    }

    protected void updateCamera() {
        double targetX = player.getX() - windowWidth / 2.0 + Player.SIZE / 2.0;
        double targetY = player.getY() - windowHeight / 2.0 + Player.SIZE / 2.0;

        cameraOffsetX = Math.max(0, Math.min(targetX, MAP_SIZE - windowWidth));
        cameraOffsetY = Math.max(0, Math.min(targetY, MAP_SIZE - windowHeight));
    }

    private void shoot() {
        synchronized(bullets) {
            double adjustedMouseX = mouseX + cameraOffsetX;
            double adjustedMouseY = mouseY + cameraOffsetY;
            List<Bullet> newBullets = player.shoot(adjustedMouseX, adjustedMouseY);
            bullets.addAll(newBullets);
        }
    }

    private void updateBullets() {
        synchronized(bullets) {
            Iterator<Bullet> bulletIterator = bullets.iterator();
            int previousLevel = player.getLevel();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update();
                if (bullet.getX() < 0 || bullet.getX() > MAP_SIZE || bullet.getY() < 0 || bullet.getY() > MAP_SIZE) {
                    bullet.deactivate();
                }
                synchronized(enemies) {
                    Iterator<Enemy> enemyIterator = enemies.iterator();
                    while (enemyIterator.hasNext()) {
                        Enemy enemy = enemyIterator.next();
                        if (bullet.checkCollision(enemy)) {
                            bullet.deactivate();
                            enemy.takeDamage(bullet.getDamage());
                            if (!enemy.isAlive()) {
                                player.addXp(enemy.getXpReward());
                                player.addCoins(enemy.getCoinReward());
                                enemyIterator.remove();
                                waveManager.enemyDied();
                            }
                        }
                    }
                }
                if (!bullet.isActive()) {
                    bulletIterator.remove();
                }
            }
            if (player.getLevel() > previousLevel) {
                isPaused = true;
                upgradeSelectionMode = true;
                offeredUpgrades = player.getUpgradeOptions();
            }
        }
    }

    private void checkWaveSpawning() {
        if (gameState == GameState.PLAYING && waveManager.shouldSpawnWave()) {
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
                    player.takeDamage(enemy.getDamage());
                    iterator.remove();
                    waveManager.enemyDied();
                    if (!player.isAlive()) {
                        gameOver = true;
                        isPaused = true;
                        gameState = GameState.GAME_OVER;
                        repaint();
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, -(int)cameraOffsetX, -(int)cameraOffsetY, null);
        } else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(-(int)cameraOffsetX, -(int)cameraOffsetY, MAP_SIZE, MAP_SIZE);
        }

        g2d.setColor(Color.RED);
        g2d.drawRect(-(int)cameraOffsetX, -(int)cameraOffsetY, MAP_SIZE, MAP_SIZE);

        if (gameState == GameState.PLAYING || gameState == GameState.GAME_OVER) {
            synchronized(enemies) {
                for (Enemy enemy : enemies) {
                    enemy.draw(g2d, cameraOffsetX, cameraOffsetY);
                }
            }

            synchronized(bullets) {
                for (Bullet bullet : bullets) {
                    bullet.draw(g2d, cameraOffsetX, cameraOffsetY);
                }
            }

            player.draw(g2d, cameraOffsetX, cameraOffsetY);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString(bundle.getString("waves") + waveManager.getCurrentWave(), 20, 30);
            g2d.drawString(bundle.getString("enemies") + waveManager.getEnemiesAlive(), 20, 60);
            g2d.drawString(bundle.getString("levelLabel") + player.getLevel() + " " + bundle.getString("xpLabel") + player.getXp() + "/" + player.getXpToNextLevel(), 20, 90);
            g2d.drawString(bundle.getString("healthLabel") + player.getHealth() + "/" + player.getMaxHealth(), 20, 120);
            g2d.drawString(bundle.getString("coinsLabel") + player.getCoins(), 20, 150);

            if (upgradeSelectionMode) {
                g2d.setColor(new Color(0, 0, 0, 0.7f));
                g2d.fillRect(0, 0, windowWidth, windowHeight);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.drawString(String.format(bundle.getString("upgradePrompt"), player.getLevel()), windowWidth / 2 - 200, windowHeight / 2 - 100);
                g2d.setFont(new Font("Arial", Font.PLAIN, 20));
                g2d.drawString(bundle.getString("currentDamageLabel") + player.getBulletDamage(), windowWidth / 2 - 150, windowHeight / 2 - 70);
                for (int i = 0; i < offeredUpgrades.size(); i++) {
                    g2d.drawString((i + 1) + ": " + bundle.getString(offeredUpgrades.get(i).getDescriptionKey()), windowWidth / 2 - 150, windowHeight / 2 - 40 + i * 30);
                }
                g2d.drawString(bundle.getString("selectUpgradeInstruction"), windowWidth / 2 - 100, windowHeight / 2 + 50);
            }

            if (gameState == GameState.GAME_OVER) {
                g2d.setColor(new Color(0, 0, 0, 0.7f));
                g2d.fillRect(0, 0, windowWidth, windowHeight);
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                g2d.drawString(bundle.getString("gameOverMessage"), windowWidth / 2 - 150, windowHeight / 2 - 50);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 30));
                g2d.drawString(bundle.getString("restartCountdownPrefix") + countdown, windowWidth / 2 - 150, windowHeight / 2 + 20);
            }
        } else if (gameState == GameState.START_SCREEN) {
            g2d.setColor(new Color(0, 0, 0, 0.7f));
            g2d.fillRect(0, 0, windowWidth, windowHeight);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString(bundle.getString("welcomeMessage"), windowWidth / 2 - 150, windowHeight / 2 - 50);
        } else if (gameState == GameState.SHOP) {
            g2d.setColor(new Color(0, 0, 0, 0.7f));
            g2d.fillRect(0, 0, windowWidth, windowHeight);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString(bundle.getString("shopTitle"), windowWidth / 2 - 100, windowHeight / 2 - 150);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString(bundle.getString("coinsLabel") + player.getCoins(), windowWidth / 2 - 100, windowHeight / 2 - 120);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (upgradeSelectionMode && gameState == GameState.PLAYING) {
            int choice = -1;
            if (e.getKeyCode() == KeyEvent.VK_1) choice = 1;
            else if (e.getKeyCode() == KeyEvent.VK_2) choice = 2;
            else if (e.getKeyCode() == KeyEvent.VK_3) choice = 3;

            if (choice > 0 && choice <= offeredUpgrades.size()) {
                UpgradeType selectedUpgrade = offeredUpgrades.get(choice - 1);
                player.applyUpgrade(selectedUpgrade);
                upgradeSelectionMode = false;
                isPaused = false;
                offeredUpgrades.clear();
                repaint();
            }
        } else if (gameState == GameState.PLAYING) {
            activeKeys.add(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameState == GameState.PLAYING) {
            activeKeys.remove(e.getKeyCode());
        }
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

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}