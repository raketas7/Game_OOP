package gui.GameMechanics;

import gui.Visuals.GameVisualizer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

public class Player {
    public static final int SIZE = 30;
    private static double speed = 5.0;
    private long fireRate = 150;
    private int bulletDamage = 10;
    private int levelUpDamageBonus = 0;
    private double levelUpSpeedMultiplier = 1.0;
    private long levelUpFireRateReduction = 0;
    private int health = 200;
    private final int maxHealth = 200;
    private int coins;
    private int enemiesKilled;
    private double x;
    private double y;
    private long lastShotTime;
    private int level = 1;
    private int xp = 0;
    private int xpToNextLevel = 100;
    private final Shop shop;
    private final List<Achievement> achievements;

    public Player(double startX, double startY, List<Achievement> achievements) {
        this.x = startX;
        this.y = startY;
        this.lastShotTime = 0;
        this.coins = loadCoins();
        this.enemiesKilled = loadEnemiesKilled();
        this.shop = new Shop(this);
        this.achievements = achievements;
        updateBulletDamage();
        updateSpeed();
        updateFireRate();
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
                levelUpSpeedMultiplier *= 1.1;
                updateSpeed();
                break;
            case DAMAGE:
                levelUpDamageBonus += 5;
                updateBulletDamage();
                break;
            case FIRE_RATE:
                levelUpFireRateReduction += 50;
                updateFireRate();
                break;
        }
    }

    public void increaseBulletDamage(int amount) {
        levelUpDamageBonus += amount;
        updateBulletDamage();
    }

    public void decreaseFireRate(int amount) {
        levelUpFireRateReduction += amount;
        updateFireRate();
    }

    public void increaseSpeed(double multiplier) {
        levelUpSpeedMultiplier *= multiplier;
        updateSpeed();
    }

    public void applyShopUpgrade(ShopUpgradeType upgrade) {
        shop.applyShopUpgrade(upgrade);
        updateBulletDamage();
        updateSpeed();
        updateFireRate();
    }

    public boolean canAffordShopUpgrade(ShopUpgradeType upgrade) {
        return shop.canAffordShopUpgrade(upgrade);
    }

    public int getShopUpgradeCost(ShopUpgradeType upgrade) {
        return shop.getShopUpgradeCost(upgrade);
    }

    public boolean canUpgrade(ShopUpgradeType upgrade) {
        return shop.canUpgrade(upgrade);
    }

    public int getShopUpgradeLevel(ShopUpgradeType upgrade) {
        return shop.getShopUpgradeLevel(upgrade);
    }

    public void purchaseUpgrade(ShopUpgradeType upgrade) {
        shop.purchaseUpgrade(upgrade);
        updateBulletDamage();
        updateSpeed();
        updateFireRate();
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

    public void addCoins(int amount) {
        coins += amount;
        saveCoins();
    }

    public void addEnemyKill() {
        enemiesKilled++;
        saveEnemiesKilled();
        updateBulletDamage();
    }

    public void setEnemiesKilled(int amount) {
        enemiesKilled = Math.max(0, amount);
        saveEnemiesKilled();
        updateBulletDamage();
    }

    public int getCoins() {
        return coins;
    }

    public int getEnemiesKilled() {
        return enemiesKilled;
    }

    public void saveCoins() {
        Preferences prefs = Preferences.userNodeForPackage(Player.class);
        prefs.putInt("playerCoins", coins);
    }

    private int loadCoins() {
        Preferences prefs = Preferences.userNodeForPackage(Player.class);
        return prefs.getInt("playerCoins", 0);
    }

    public void saveEnemiesKilled() {
        Preferences prefs = Preferences.userNodeForPackage(Player.class);
        prefs.putInt("enemiesKilled", enemiesKilled);
    }

    private int loadEnemiesKilled() {
        Preferences prefs = Preferences.userNodeForPackage(Player.class);
        return prefs.getInt("enemiesKilled", 0);
    }

    private void updateSpeed() {
        speed = 5.0 * Math.pow(1.03, shop.getShopUpgradeLevel(ShopUpgradeType.SPEED)) * levelUpSpeedMultiplier;
    }

    private void updateFireRate() {
        fireRate = Math.max(100, 150 - (shop.getShopUpgradeLevel(ShopUpgradeType.FIRE_RATE) * 10L) - levelUpFireRateReduction);
    }

    private int calculateAchievementDamageBonus() {
        int bonus = 0;
        if (achievements != null) {
            for (Achievement achievement : achievements) {
                achievement.updateStatus(enemiesKilled);
                if (achievement.isUnlocked()) {
                    bonus += achievement.getDamageBonus();
                }
            }
        }
        return bonus;
    }

    private void updateBulletDamage() {
        int achievementBonus = calculateAchievementDamageBonus();
        bulletDamage = 10 + shop.getShopUpgradeLevel(ShopUpgradeType.DAMAGE) + achievementBonus + levelUpDamageBonus;
    }


    public void reset() {
        health = maxHealth;
        level = 1;
        xp = 0;
        xpToNextLevel = 100;
        levelUpDamageBonus = 0; // Reset level-up damage bonus
        levelUpSpeedMultiplier = 1.0; // Reset level-up speed multiplier
        levelUpFireRateReduction = 0; // Reset level-up fire rate reduction
        updateBulletDamage(); // Recalculate with achievement bonuses
        updateSpeed(); // Recalculate with shop upgrades
        updateFireRate(); // Recalculate with shop upgrades
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
    public int getLevel() { return level; }
    public int getXp() { return xp; }
    public int getXpToNextLevel() { return xpToNextLevel; }
    public int getBulletDamage() { return bulletDamage; }

    public Shop getShop() {
        return shop;
    }

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
            return Math.sqrt(2) / 2;
        }
        return 1.0;
    }

    public long getFireRate() {
        return fireRate;
    }
}