package gui.Enemies;

import java.awt.*;

public class TankEnemy extends Enemy {
    private static final int XP_REWARD = 50;
    private static final int DAMAGE = 40;
    private static final int COIN_REWARD = 3;

    public TankEnemy(double x, double y) {
        super(x, y, 40, 2.0, Color.GREEN, 40, 50);
    }

    public int getXpReward() {
        return XP_REWARD;
    }

    public int getDamage() {
        return DAMAGE;
    }

    public int getCoinReward() {
        return COIN_REWARD;
    }
}