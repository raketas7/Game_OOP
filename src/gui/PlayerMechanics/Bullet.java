package gui.PlayerMechanics;

import java.awt.*;

public abstract class Bullet {
    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private final double vx;
    private final double vy;
    private final int size = 12;
    private final int damage;
    private boolean isActive = true;
    private final long creationTime;
    protected static final long LIFETIME = 5000;

    public Bullet(double startX, double startY, double targetX, double targetY, int bulletDamage) {
        this.x = startX;
        this.y = startY;
        this.prevX = startX;
        this.prevY = startY;
        this.creationTime = System.currentTimeMillis();
        this.damage = bulletDamage;
        double dx = targetX - startX;
        double dy = targetY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            double speed = 10.0;
            this.vx = (dx / distance) * speed;
            this.vy = (dy / distance) * speed;
        } else {
            this.vx = 0;
            this.vy = 0;
        }
    }

    public void update() {
        if (isActive) {
            // Проверяем время жизни
            if (System.currentTimeMillis() - creationTime >= LIFETIME) {
                isActive = false;
                return;
            }
            prevX = x;
            prevY = y;
            x += vx;
            y += vy;
        }
    }

    public void draw(Graphics2D g2d, double cameraOffsetX, double cameraOffsetY) {
        if (isActive) {
            int drawX = (int)(x - cameraOffsetX);
            int drawY = (int)(y - cameraOffsetY);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(drawX, drawY, size, size);
        }
    }

    public boolean checkCollision(gui.Enemies.Enemy enemy) {
        if (!isActive) return false;

        Rectangle enemyBounds = enemy.getCollisionBounds();
        int steps = 5;
        double stepX = (x - prevX) / steps;
        double stepY = (y - prevY) / steps;

        for (int i = 0; i <= steps; i++) {
            double checkX = prevX + stepX * i;
            double checkY = prevY + stepY * i;
            Rectangle bulletBounds = new Rectangle((int)checkX, (int)checkY, size, size);
            if (bulletBounds.intersects(enemyBounds)) {
                return true;
            }
        }

        return false;
    }

    public int getDamage() { return damage; }
    public boolean isActive() { return isActive; }
    public void deactivate() { isActive = false; }
    public double getX() { return x; }
    public double getY() { return y; }

    protected abstract long getCurrentTime();
}