package me.kajias.rocketwars.configs;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.gui.menus.player_utilities.ArmorShopMenu;
import me.kajias.rocketwars.misc.Serialization;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.SelectedColorType;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DataConfiguration
{
    private static final BaseConfiguration playersData = new BaseConfiguration("data");

    public static void initialize() { playersData.load(); }

    public static void savePlayerData(GamePlayer player) {
        playersData.getConfig().set("data." + player.getUniqueId() + ".name", player.getPlayerName());
        playersData.getConfig().set("data." + player.getUniqueId() + ".games-played", player.getGamesPlayed());
        playersData.getConfig().set("data." + player.getUniqueId() + ".games-won", player.getGamesWon());
        playersData.getConfig().set("data." + player.getUniqueId() + ".rockets-launched", player.getRocketsLaunched());
        playersData.getConfig().set("data." + player.getUniqueId() + ".rockets-destroyed", player.getRocketsDestroyed());
        playersData.getConfig().set("data." + player.getUniqueId() + ".boots", Serialization.itemStackToBase64(player.getBoots()));
        playersData.getConfig().set("data." + player.getUniqueId() + ".helmet", Serialization.itemStackToBase64(player.getHelmet()));
        playersData.getConfig().set("data." + player.getUniqueId() + ".glass-color", player.getGlassColor());
        playersData.getConfig().set("data." + player.getUniqueId() + ".selected-color-type-boots", player.getSelectedColorTypeBoots().toString());
        playersData.getConfig().set("data." + player.getUniqueId() + ".selected-color-type-helmet", player.getSelectedColorTypeHelmet().toString());
        playersData.getConfig().set("data." + player.getUniqueId() + ".selected-color-type-glass", player.getSelectedColorTypeGlass().toString());
        if (playersData.getConfig().getConfigurationSection("data." + player.getUniqueId() + ".bought-bonus-items") != null)
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-bonus-items", null);
        for (Map.Entry<String, Integer> pair : player.getBoughtAbilities().entrySet())
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-bonus-items." + pair.getKey(), pair.getValue());
        if (playersData.getConfig().getConfigurationSection("data." + player.getUniqueId() + ".bought-armor-items") != null)
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-armor-items", null);
        for (Map.Entry<ItemStack, Integer> pair : player.getBoughtArmorItems().entrySet())
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-armor-items." + Serialization.itemStackToBase64(pair.getKey()), pair.getValue());
        if (playersData.getConfig().getConfigurationSection("data." + player.getUniqueId() + ".bought-colors") != null)
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-colors", null);
        for (Map.Entry<String, Integer> pair : player.getBoughtColors().entrySet())
            playersData.getConfig().set("data." + player.getUniqueId() + ".bought-colors." + pair.getKey(), pair.getValue());
        if (playersData.getConfig().getConfigurationSection("data." + player.getUniqueId() + ".blocked-colors") != null)
            playersData.getConfig().set("data." + player.getUniqueId() + ".blocked-colors", null);
        for (String color : player.getBlockedColors())
            playersData.getConfig().set("data." + player.getUniqueId() + ".blocked-colors", player.getBlockedColors());
        playersData.getConfig().addDefault("data." + player.getUniqueId() + ".last-date", player.getLastDate().toString());

        Optional<GamePlayer> optPlayer = RocketWars.loadedPlayerData.stream()
                .filter(x -> x.getUniqueId().equals(player.getUniqueId())).findFirst();
        if (optPlayer.isPresent()) {
            RocketWars.loadedPlayerData.set(RocketWars.loadedPlayerData.indexOf(optPlayer.get()), player);
        } else RocketWars.loadedPlayerData.add(player);
        playersData.save();
    }

    public static GamePlayer getPlayerDataFromConfig(UUID uuid) {
        GamePlayer result = new GamePlayer(uuid);
        if(playersData.getConfig().getConfigurationSection("data." + uuid.toString()) != null) {
            result.setPlayerName(playersData.getConfig().getString("data." + uuid + ".name"));
            result.setGamesPlayed(playersData.getConfig().getInt("data." + uuid + ".games-played"));
            result.setGamesWon(playersData.getConfig().getInt("data." + uuid + ".games-won"));
            result.setRocketsLaunched(playersData.getConfig().getInt("data." + uuid + ".rockets-launched"));
            result.setRocketsDestroyed(playersData.getConfig().getInt("data." + uuid + ".rockets-destroyed"));
            try {
                result.setBoots(Serialization.itemStackFromBase64(playersData.getConfig().getString("data." + uuid + ".boots")));
            } catch (Exception ignored) {}
            try {
                result.setHelmet(Serialization.itemStackFromBase64(playersData.getConfig().getString("data." + uuid + ".helmet")));
            } catch (Exception ignored) {}
            result.setGlassColor(playersData.getConfig().getString("data." + uuid + ".glass-color"));
            if (playersData.getConfig().getString("data." + uuid + ".selected-color-type-boots") != null)
                result.setSelectedColorType("color-boots", SelectedColorType.valueOf(playersData.getConfig().getString("data." + uuid + ".selected-color-type-boots")));
            if (playersData.getConfig().getString("data." + uuid + ".selected-color-type-helmet") != null)
                result.setSelectedColorType("color-helmet", SelectedColorType.valueOf(playersData.getConfig().getString("data." + uuid + ".selected-color-type-helmet")));
            if (playersData.getConfig().getString("data." + uuid + ".selected-color-type-glass") != null)
                result.setSelectedColorType("color-glass", SelectedColorType.valueOf(playersData.getConfig().getString("data." + uuid + ".selected-color-type-glass")));
            if (playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-bonus-items") != null) {
                for (String bonusItemName : playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-bonus-items").getKeys(false))
                    result.getBoughtAbilities().put(bonusItemName, playersData.getConfig().getInt("data." + uuid + ".bought-bonus-items." + bonusItemName));
            }
            if (playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-armor-items") != null) {
                for (String armorItemBase64 : playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-armor-items").getKeys(false)) {
                    try {
                        ItemStack armorItem = Serialization.itemStackFromBase64(armorItemBase64);
                        result.getBoughtArmorItems().put(armorItem, playersData.getConfig().getInt("data." + uuid + ".bought-armor-items." + armorItemBase64));
                    } catch (IOException ignored) {}
                }
            }
            if (playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-colors") != null) {
                for (String color : playersData.getConfig().getConfigurationSection("data." + uuid + ".bought-colors").getKeys(false))
                    result.getBoughtColors().put(color, playersData.getConfig().getInt("data." + uuid + ".bought-colors." + color));
            }
            result.getBlockedColors().addAll(playersData.getConfig().getStringList("data." + uuid + ".blocked-colors"));
            if (playersData.getConfig().getString("data." + uuid + ".last-date") != null)
                result.setLastDate(LocalDate.parse(playersData.getConfig().getString("data." + uuid + ".last-date")));
            result.updateDateAndItems();
        }
        return result;
    }

    public static List<GamePlayer> getPlayersFromConfig() {
        List<GamePlayer> players = new ArrayList<>();
        for (String uuid : playersData.getConfig().getConfigurationSection("data").getKeys(false)) {
            players.add(getPlayerDataFromConfig(UUID.fromString(uuid)));
        }

        return players;
    }

    public static GamePlayer getPlayerData(UUID uuid) {
        return RocketWars.loadedPlayerData.stream().filter(x -> x.getUniqueId().equals(uuid)).findAny().orElse(null);
    }
}
