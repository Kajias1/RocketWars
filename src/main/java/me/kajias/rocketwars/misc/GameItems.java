package me.kajias.rocketwars.misc;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.objects.Game;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.RandomCollection;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.List;
import java.util.Random;

public class GameItems
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final Game game;

    public GameItems(Game game) {
        this.game = game;
    }

    public void giveRandomRocketDrop(Player player, String forcedCategoryName) {
        RandomCollection<String> randomCollection = new RandomCollection<>();
            for (String categoryName : config.getConfigurationSection("game-config.game-drops.rockets.categories").getKeys(false)) {
                int weight = config.getInt("game-config.game-drops.rockets.categories." + categoryName + ".weight");
                if (config.getConfigurationSection("game-config.game-drops.rockets.categories." + categoryName + ".weight-change") != null) {
                    for (String weightChangeNum : config.getConfigurationSection("game-config.game-drops.rockets.categories." + categoryName + ".weight-change").getKeys(false)) {
                        if (game.getTimePassed() >= config.getInt("game-config.game-drops.rockets.categories." + categoryName + ".weight-change." + weightChangeNum + ".time")) {
                            weight = config.getInt("game-config.game-drops.rockets.categories." + categoryName + ".weight-change." + weightChangeNum + ".weight");
                        }
                    }
                }
                randomCollection.add(weight, categoryName);
            }
        String randomCategorySelection = randomCollection.next();
        if (forcedCategoryName != null) randomCategorySelection = forcedCategoryName;

        List<String> rocketsList = config.getStringList("game-config.game-drops.rockets.categories." + randomCategorySelection + ".entry");
        int randomIndex = new Random().nextInt(rocketsList.size() / 2) * 2;

        ItemStack item = new ItemStack(Material.MONSTER_EGG, 1);
        SpawnEggMeta eggMeta = (SpawnEggMeta) item.getItemMeta();
        eggMeta.setDisplayName(Utils.colorize(rocketsList.get(randomIndex)));
        eggMeta.setSpawnedType(EntityType.valueOf(rocketsList.get(randomIndex + 1)));
        item.setItemMeta(eggMeta);

        player.getInventory().addItem(item);
    }

    public void giveRandomItemDrop(Player player, String itemDropCategory, boolean sendPlayerTitle) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        RandomCollection<String> randomCollection = new RandomCollection<>();
        for (String itemName : config.getConfigurationSection("game-config.game-drops." + itemDropCategory + ".items").getKeys(false)) {
            if (playerData != null && isPurchaseable(itemName) && !playerData.getBoughtAbilities()
                    .containsKey(Utils.stripColor(menusConfig.getString("menus.main-shop.bonus-items." + itemName + ".name"))))
                continue;
            randomCollection.add(config.getInt("game-config.game-drops." + itemDropCategory + ".items." + itemName + ".weight"), itemName);
        }
        if (randomCollection.size() > 0) {
            String randomItemNameSelection = randomCollection.next();
            if (randomItemNameSelection.equalsIgnoreCase("heavy-rockets")) {
                for (int i = 0; i < 3; i++) giveRandomRocketDrop(player, "heavy");
                if (sendPlayerTitle) {
                    player.sendTitle(Utils.colorize(config.getString("messages.player-take-bonus-title")),
                            Utils.colorize(config.getString("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".name")), 10, 50, 20);
                    Sounds.ORB_PICKUP.play(player);
                }
            } else {
                ItemStack item = new ItemStack(Material.getMaterial(config.getString("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".material")));
                if (config.getInt("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".amount") != 0)
                    item.setAmount(config.getInt("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".amount"));
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName(Utils.colorize(config.getString("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".name")));
                itemMeta.setLore(Utils.colorize(config.getStringList("game-config.game-drops." + itemDropCategory + ".items." + randomItemNameSelection + ".lore")));
                item.setItemMeta(itemMeta);

                if (sendPlayerTitle) {
                    player.sendTitle(Utils.colorize(config.getString("messages.player-take-bonus-title")), Utils.colorize(item.getItemMeta().getDisplayName()), 10, 50, 20);
                    Sounds.ORB_PICKUP.play(player);
                }
                player.getInventory().addItem(item);
            }
        }
    }

    public void giveRandomShipLoot(Player player) {
        for (int i = 0; i < 2; ++i) giveRandomRocketDrop(player, null);
        giveRandomItemDrop(player, "gadgets", false);
        giveRandomItemDrop(player, "bonus-items", false);
        Sounds.ORB_PICKUP.play(player);
    }

    private boolean isPurchaseable(String str) {
        return menusConfig.getConfigurationSection("menus.main-shop.bonus-items").getKeys(false).contains(str);
    }
}
