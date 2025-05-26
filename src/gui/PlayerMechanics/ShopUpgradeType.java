package gui.PlayerMechanics;

public enum ShopUpgradeType {
    DAMAGE("shopDamageUpgrade"),
    FIRE_RATE("shopFireRateUpgrade"),
    SPEED("shopSpeedUpgrade");

    private final String descriptionKey;

    ShopUpgradeType(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }
}