package gui.Enemies;

import java.awt.*;

public class FastEnemy extends Enemy {
    private static final int XP_REWARD = 10;
    private static final int DAMAGE = 10; // Added damage value

    public FastEnemy(double x, double y) {
        super(x, y, 25, 4.0, Color.BLUE, 25, 10);
    }

    public int getXpReward() {
        return XP_REWARD;
    }

    public int getDamage() {
        return DAMAGE;
    }
}