package gui.PlayerMechanics.Upgrades;

import gui.PlayerMechanics.Player;
import gui.PlayerMechanics.ShopUpgradeType;

public class SpeedUpgrade implements Upgrade {
    private static final int BASE_COST = 12;
    private static final int COST_INCREMENT = 10;
    private static final int MAX_LEVEL = 5;

    @Override
    public void apply(Player player) {
        player.increaseSpeed(1.03);
    }

    @Override
    public int getCost(int level) {
        return BASE_COST + level * COST_INCREMENT;
    }

    @Override
    public boolean canUpgrade(int level) {
        return level < MAX_LEVEL;
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    @Override
    public String getDescriptionKey() {
        return "shopSpeedUpgrade";
    }

    @Override
    public ShopUpgradeType getType() {
        return ShopUpgradeType.SPEED;
    }
}