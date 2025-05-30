package gui.GameMechanics;

public class Achievement {
    private final String name;
    private final String description;
    private final int targetKills;
    private boolean isUnlocked;
    private final int damageBonus;


    public Achievement(String name, String description, int targetKills, int damageBonus) {
        this.name = name;
        this.description = description;
        this.targetKills = targetKills;
        this.damageBonus = damageBonus;
        this.isUnlocked = false;
    }

    public void updateStatus(int playerKills) {
        if (playerKills >= targetKills) {
            isUnlocked = true;
        }
    }

    public void reset() {
        isUnlocked = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public int getDamageBonus() {
        return damageBonus;
    }

    public int getTargetKills() {
        return targetKills;
    }
}