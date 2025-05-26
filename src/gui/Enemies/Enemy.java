package gui.Enemies;
import java.awt.*;
import java.util.List;
public abstract class Enemy {
    protected double x;
    protected double y;
    protected final int size;
    protected final double speed;
    protected final Color color;
    protected final int collisionRadius;
    protected double pushForce = 0.5;
    protected int health;
    public Enemy(double x, double y, int size, double speed, Color color, int collisionRadius, int health) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.color = color;
        this.collisionRadius = collisionRadius;
        this.health = health;
    }
    public void move(double targetX, double targetY, List<Enemy> allEnemies) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance > 0) {
            dx = dx / distance * speed;
            dy = dy / distance * speed;
        }
        double newX = x + dx;
        double newY = y + dy;
        Rectangle futureBounds = new Rectangle(
                (int)(newX - (double) collisionRadius / 2),
                (int)(newY - (double) collisionRadius / 2),
                collisionRadius,
                collisionRadius
        );
        for (Enemy other : allEnemies) {
            if (other != this && futureBounds.intersects(other.getCollisionBounds())) {
                double collisionDx = x - other.getX();
                double collisionDy = y - other.getY();
                double collisionDist = Math.sqrt(collisionDx * collisionDx + collisionDy * collisionDy);
                if (collisionDist > 0) {
                    collisionDx /= collisionDist;
                    collisionDy /= collisionDist;
                    double penetration = (double) (collisionRadius + other.collisionRadius) / 2 - collisionDist;
                    double massRatio = (double)size / (size + other.size);
                    newX += collisionDx * penetration * massRatio * pushForce;
                    newY += collisionDy * penetration * massRatio * pushForce;
                }
            }
        }
        x = newX;
        y = newY;
    }
    public void draw(Graphics2D g2d, double cameraOffsetX, double cameraOffsetY) {
        int drawX = (int)(x - cameraOffsetX);
        int drawY = (int)(y - cameraOffsetY);
        g2d.setColor(color);
        g2d.fillOval(drawX, drawY, size, size);
        g2d.setColor(Color.RED);
        g2d.drawOval(drawX, drawY, size, size);
    }
    public Rectangle getBounds(double cameraOffsetX, double cameraOffsetY) {
        return new Rectangle(
                (int)(x - cameraOffsetX),
                (int)(y - cameraOffsetY),
                size,
                size
        );
    }
    public Rectangle getCollisionBounds() {
        return new Rectangle(
                (int)(x - (double) collisionRadius / 2),
                (int)(y - (double) collisionRadius / 2),
                collisionRadius,
                collisionRadius
        );
    }
    public void takeDamage(int damage) {
        health -= damage;
    }
    public boolean isAlive() {
        return health > 0;
    }

    public abstract int getXpReward();
    public abstract int getDamage();
    public abstract int getCoinReward();

    public double getX() { return x; }
    public double getY() { return y; }
    public int getSize() { return size; }
    public int getHealth() { return health; }
    public Color getColor() { return color; }
}