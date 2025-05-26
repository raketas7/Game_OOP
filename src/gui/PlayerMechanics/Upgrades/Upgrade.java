package gui.PlayerMechanics.Upgrades;

import gui.PlayerMechanics.Player;
import gui.PlayerMechanics.ShopUpgradeType;

public interface Upgrade {
    void apply(Player player); // Применяет апгрейд к игроку
    int getCost(int level);   // Возвращает стоимость апгрейда для текущего уровня
    boolean canUpgrade(int level); // Проверяет, можно ли улучшить
    int getMaxLevel();        // Максимальный уровень апгрейда
    String getDescriptionKey(); // Ключ для локализации
    ShopUpgradeType getType(); // Тип апгрейда
}