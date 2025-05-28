package gui.Enemies;

import java.awt.*;

public class BasicEnemy extends Enemy {
    private static final int XP_REWARD = 20;
    private static final int DAMAGE = 20;
    private static final int COIN_REWARD = 2;

    public BasicEnemy(double x, double y) {
        super(x, y, 30, 3, Color.BLACK, 30, 20);
    }
    public int getXpReward () {
        return XP_REWARD;
    }

    public int getDamage () {
        return DAMAGE;
    }

    public int getCoinReward () {
        return COIN_REWARD;
    }
}