package gui.PlayerMechanics;

public enum UpgradeType {
    SPEED("speedUpgradeDescription"),
    DAMAGE("damageUpgradeDescription"),
    FIRE_RATE("fireRateUpgradeDescription");

    private final String descriptionKey;

    UpgradeType(String descriptionKey) {
        this.descriptionKey = descriptionKey;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }
}