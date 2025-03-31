package gui;

import java.awt.*;

public class Player {
    public static final int SIZE = 30;
    private static final double SPEED = 5.0;

    private double x;
    private double y;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;
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

    public double getX() { return x; }
    public double getY() { return y; }
    public static double getSpeed() { return SPEED; }

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
            return SPEED * Math.sqrt(2) / 2;
        }
        return SPEED;
    }
}