package gui.PlayerMechanics;

import gui.PlayerMechanics.Upgrades.DamageUpgrade;
import gui.PlayerMechanics.Upgrades.FireRateUpgrade;
import gui.PlayerMechanics.Upgrades.SpeedUpgrade;
import gui.PlayerMechanics.Upgrades.Upgrade;

import java.util.HashMap;
import java.util.Map;

public class Shop {
    private final Map<ShopUpgradeType, Upgrade> upgrades;
    private final Map<ShopUpgradeType, Integer> upgradeLevels;
    private final Player player;

    public Shop(Player player) {
        this.player = player;
        this.upgrades = new HashMap<>();
        this.upgradeLevels = new HashMap<>();

        upgrades.put(ShopUpgradeType.DAMAGE, new DamageUpgrade());
        upgrades.put(ShopUpgradeType.FIRE_RATE, new FireRateUpgrade());
        upgrades.put(ShopUpgradeType.SPEED, new SpeedUpgrade());
        for (ShopUpgradeType type : ShopUpgradeType.values()) {
            upgradeLevels.put(type, 0);
        }
    }

    public void applyShopUpgrade(ShopUpgradeType upgradeType) {
        Upgrade upgrade = upgrades.get(upgradeType);
        int level = upgradeLevels.getOrDefault(upgradeType, 0);
        if (upgrade != null && upgrade.canUpgrade(level)) {
            upgrade.apply(player);
            upgradeLevels.put(upgradeType, level + 1);
            player.saveCoins();
        }
    }

    public boolean canAffordShopUpgrade(ShopUpgradeType upgradeType) {
        Upgrade upgrade = upgrades.get(upgradeType);
        int level = upgradeLevels.getOrDefault(upgradeType, 0);
        return upgrade != null && player.getCoins() >= upgrade.getCost(level);
    }

    public int getShopUpgradeCost(ShopUpgradeType upgradeType) {
        Upgrade upgrade = upgrades.get(upgradeType);
        int level = upgradeLevels.getOrDefault(upgradeType, 0);
        return upgrade != null ? upgrade.getCost(level) : Integer.MAX_VALUE;
    }

    public boolean canUpgrade(ShopUpgradeType upgradeType) {
        Upgrade upgrade = upgrades.get(upgradeType);
        int level = upgradeLevels.getOrDefault(upgradeType, 0);
        return upgrade != null && upgrade.canUpgrade(level);
    }

    public int getShopUpgradeLevel(ShopUpgradeType upgradeType) {
        return upgradeLevels.getOrDefault(upgradeType, 0);
    }

    public void purchaseUpgrade(ShopUpgradeType upgradeType) {
        if (canUpgrade(upgradeType) && canAffordShopUpgrade(upgradeType)) {
            int cost = getShopUpgradeCost(upgradeType);
            player.addCoins(-cost);
            applyShopUpgrade(upgradeType);
        }
    }

    public void reset() {
        for (ShopUpgradeType type : ShopUpgradeType.values()) {
            upgradeLevels.put(type, 0);
        }
    }

    public Map<ShopUpgradeType, Upgrade> getUpgrades() {
        return upgrades;
    }
}