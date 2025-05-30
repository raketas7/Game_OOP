package GamePlay;

import gui.GameMechanics.Player;
import gui.GameMechanics.Shop;
import gui.GameMechanics.ShopUpgradeType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ShopTest {
    private Player player;
    private Shop shop;

    @Before
    public void setUp() {
        player = new Player(0, 0, new ArrayList<>());
        shop = player.getShop();
    }

    @Test
    public void testInitialShopState() {
        for (ShopUpgradeType type : ShopUpgradeType.values()) {
            assertEquals(0, shop.getShopUpgradeLevel(type));
            assertTrue(shop.canUpgrade(type));
        }
    }

    @Test
    public void testPurchaseUpgrade() {
        player.addCoins(1000);

        int initialDamage = player.getBulletDamage();
        shop.purchaseUpgrade(ShopUpgradeType.DAMAGE);
        assertEquals(1, shop.getShopUpgradeLevel(ShopUpgradeType.DAMAGE));
        assertEquals(initialDamage + 1, player.getBulletDamage());

        double initialSpeed = Player.getSpeed();
        shop.purchaseUpgrade(ShopUpgradeType.SPEED);
        assertEquals(1, shop.getShopUpgradeLevel(ShopUpgradeType.SPEED));
        assertTrue(Player.getSpeed() > initialSpeed);

        long initialFireRate = 150;
        shop.purchaseUpgrade(ShopUpgradeType.FIRE_RATE);
        assertEquals(1, shop.getShopUpgradeLevel(ShopUpgradeType.FIRE_RATE));
        assertTrue(player.getFireRate() < initialFireRate);
    }

    @Test
    public void testUpgradeCostIncreases() {
        player.addCoins(1000);
        int firstLevelCost = shop.getShopUpgradeCost(ShopUpgradeType.DAMAGE);
        shop.purchaseUpgrade(ShopUpgradeType.DAMAGE);
        int secondLevelCost = shop.getShopUpgradeCost(ShopUpgradeType.DAMAGE);
        assertTrue(secondLevelCost > firstLevelCost);
    }

    @Test
    public void testShopReset() {
        player.addCoins(1000);
        shop.purchaseUpgrade(ShopUpgradeType.DAMAGE);
        shop.purchaseUpgrade(ShopUpgradeType.SPEED);

        shop.reset();

        for (ShopUpgradeType type : ShopUpgradeType.values()) {
            assertEquals(0, shop.getShopUpgradeLevel(type));
        }
    }
}