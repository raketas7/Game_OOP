package gui.PlayerMechanics;

import gui.GameVisualizer;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Player {
    public static final int SIZE = 30;
    private static double speed = 5.0;
    private long fireRate = 150;
    private int bulletDamage = 10;
    private int health = 1000;
    private final int maxHealth = 1000;

    private double x;
    private double y;
    private long lastShotTime;
    private int level = 1;
    private int xp = 0;
    private int xpToNextLevel = 100;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
        this.lastShotTime = 0;
    }

    public void move(double dx, double dy, int mapSize) {
        if (dx != 0 && dy != 0) {
            double normFactor = Math.sqrt(2) / 2;
            dx *= normFactor;
            dy *= normFactor;
        }
        x = Math.max(GameVisualizer.BORDER_PADDING, Math.min(x + dx, mapSize - SIZE - GameVisualizer.BORDER_PADDING));
        y = Math.max(GameVisualizer.BORDER_PADDING, Math.min(y + dy, mapSize - SIZE - GameVisualizer.BORDER_PADDING));
    }

    public List<Bullet> shoot(double mouseX, double mouseY) {
        List<Bullet> newBullets = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= fireRate) {
            Bullet bullet = new Bullet(x + SIZE / 2.0, y + SIZE / 2.0, mouseX, mouseY, bulletDamage);
            newBullets.add(bullet);
            lastShotTime = currentTime;
        }
        return newBullets;
    }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        xp -= xpToNextLevel;
        level++;
        xpToNextLevel = 100 * level;
    }

    public List<UpgradeType> getUpgradeOptions() {
        List<UpgradeType> availableUpgrades = new ArrayList<>(Arrays.asList(UpgradeType.values()));
        Collections.shuffle(availableUpgrades);
        return availableUpgrades.subList(0, Math.min(3, availableUpgrades.size()));
    }

    public void applyUpgrade(UpgradeType upgrade) {
        switch (upgrade) {
            case SPEED:
                speed *= 1.1;
                break;
            case DAMAGE:
                bulletDamage += 5;
                break;
            case FIRE_RATE:
                fireRate = Math.max(100, fireRate - 50);
                break;
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public void regenerateHealth(int amount) {
        if (isAlive() && health < maxHealth) {
            health = Math.min(health + amount, maxHealth);
        }
    }

    public void reset() {
        health = maxHealth;
        level = 1;
        xp = 0;
        xpToNextLevel = 100;
        speed = 5.0;
        fireRate = 150;
        bulletDamage = 10;
        lastShotTime = 0;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public double getX() { return x; }
    public double getY() { return y; }
    public static double getSpeed() { return speed; }
    public long getFireRate() { return fireRate; }
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public int getBulletDamage() { return bulletDamage; }

    public void draw(Graphics2D g2d, double cameraOffsetX, double cameraOffsetY) {
        int drawX = (int)(x - cameraOffsetX);
        int drawY = (int)(y - cameraOffsetY);

        g2d.setColor(Color.RED);
        g2d.fillOval(drawX, drawY, SIZE, SIZE);
        g2d.setColor(Color.WHITE);
        g2d.drawOval(drawX, drawY, SIZE, SIZE);
    }

    public Rectangle getBounds(double cameraOffsetX, double cameraOffsetY) {
        return new Rectangle(
                (int)(x - cameraOffsetX),
                (int)(y - cameraOffsetY),
                SIZE,
                SIZE
        );
    }

    public static double calculateNormalizedSpeed(double dx, double dy) {
        if (dx != 0 && dy != 0) {
            return 1.0 * Math.sqrt(2) / 2;
        }
        return 1.0;
    }
}