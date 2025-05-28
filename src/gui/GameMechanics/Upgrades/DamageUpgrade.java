package gui.GameMechanics.Upgrades;

import gui.GameMechanics.Player;
import gui.GameMechanics.ShopUpgradeType;

public class DamageUpgrade implements Upgrade {
    private static final int BASE_COST = 10;
    private static final int COST_INCREMENT = 10;
    private static final int MAX_LEVEL = 5;

    @Override
    public void apply(Player player) {
        player.increaseBulletDamage(1);
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
        return "shopDamageUpgrade";
    }

    @Override
    public ShopUpgradeType getType() {
        return ShopUpgradeType.DAMAGE;
    }
}