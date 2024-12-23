package me.kajias.rocketwars.objects;

import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.gui.menus.player_utilities.ArmorShopMenu;
import me.kajias.rocketwars.objects.enums.SelectedColorType;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class GamePlayer
{
    private final UUID uuid;
    private String playerName;
    private int gamesPlayed;
    private int gamesWon;
    private int rocketsLaunched;
    private int rocketsDestroyed;
    private ItemStack boots;
    private ItemStack helmet;
    private HashMap<String, Integer> boughtAbilities;
    private HashMap<ItemStack, Integer> boughtArmorItems;
    private HashMap<String, Integer> boughtColors;
    private List<String> blockedColors;
    private String glassColor;
    private SelectedColorType selectedColorTypeBoots;
    private SelectedColorType selectedColorTypeHelmet;
    private SelectedColorType selectedColorTypeGlass;
    private LocalDate lastDate;

    public GamePlayer(UUID uuid) {
        this.uuid = uuid;
        playerName = null;
        gamesPlayed = 0;
        gamesWon = 0;
        boots = null;
        helmet = null; // TODO
        boughtAbilities = new HashMap<>();
        boughtArmorItems = new HashMap<>();
        boughtColors = new HashMap<>();
        blockedColors = new ArrayList<>();
        glassColor = null;
        selectedColorTypeBoots = SelectedColorType.ONE;
        selectedColorTypeHelmet = SelectedColorType.ONE;
        selectedColorTypeGlass = SelectedColorType.ONE;
        lastDate = LocalDate.now();

        boots = ArmorShopMenu.createArmorItem("boots", "1");
        boughtArmorItems.putIfAbsent(boots, 365);
        helmet = ArmorShopMenu.createArmorItem("helmets", "1");
        boughtArmorItems.putIfAbsent(helmet, 365);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public HashMap<String, Integer> getBoughtAbilities() {
        return boughtAbilities;
    }

    public HashMap<ItemStack, Integer> getBoughtArmorItems() {
        return boughtArmorItems;
    }

    public HashMap<String, Integer> getBoughtColors () {
        return boughtColors;
    }

    public List<String> getBlockedColors () {
        return blockedColors;
    }

    public String getGlassColor() {
        return glassColor;
    }

    public SelectedColorType getSelectedColorTypeBoots() {
        return selectedColorTypeBoots;
    }

    public SelectedColorType getSelectedColorTypeHelmet() {
        return selectedColorTypeHelmet;
    }

    public SelectedColorType getSelectedColorTypeGlass() {
        return selectedColorTypeGlass;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setGlassColor(String glassColor) {
        if (glassColor != null) this.glassColor = glassColor;
    }

    public String getRandomBoughtColor(String colorType) {
        List<String> filtered = getAllowedBoughtColors(colorType);
        if (!filtered.isEmpty())
            return Utils.getRandom(filtered);
        else return null;
    }

    public List<String> getAllowedBoughtColors(String colorType) {
        List<String> filtered = new ArrayList<>();
        for (String boughtColor : boughtColors.keySet()) {
            if (boughtColor.split(":")[0].split("-")[1].equalsIgnoreCase(colorType) && !blockedColors.contains(boughtColor))
                filtered.add(boughtColor.split(":")[1]);
        }
        return filtered;
    }

    public void setSelectedColorType(String itemColorType, SelectedColorType selectedColorType) {
        switch (itemColorType) {
            case "color-boots":
                selectedColorTypeBoots = selectedColorType;
                break;
            case "color-helmet":
                selectedColorTypeHelmet = selectedColorType;
                break;
            case "color-glass":
                selectedColorTypeGlass = selectedColorType;
                break;
        }
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public void updateDateAndItems() {
        if (this.lastDate.isBefore(LocalDate.now())) {
            if (!this.boughtAbilities.isEmpty()) {
                HashMap<String, Integer> updatedBoughtBonusItems = new HashMap<>();
                for (Map.Entry<String, Integer> entry : this.boughtAbilities.entrySet()) {
                    if (entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()) > 1)
                        updatedBoughtBonusItems.put(entry.getKey(), entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()));
                }
                this.boughtAbilities.clear();
                this.boughtAbilities = updatedBoughtBonusItems;
            }
            if (!this.boughtArmorItems.isEmpty()) {
                HashMap<ItemStack, Integer> updatedArmorItems = new HashMap<>();
                for (Map.Entry<ItemStack, Integer> entry : this.boughtArmorItems.entrySet()) {
                    if (entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()) > 1)
                        updatedArmorItems.put(entry.getKey(), entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()));
                }
                this.boughtArmorItems.clear();
                this.boughtArmorItems = updatedArmorItems;
            }
            if (!this.boughtColors.isEmpty()) {
                HashMap<String, Integer> updatedBoughtColors = new HashMap<>();
                for (Map.Entry<String, Integer> entry : this.boughtColors.entrySet()) {
                    if (entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()) > 1)
                        updatedBoughtColors.put(entry.getKey(), entry.getValue() - (int) ChronoUnit.DAYS.between(this.lastDate, LocalDate.now()));
                }
                this.boughtColors.clear();
                this.boughtColors = updatedBoughtColors;
            }
        }
        this.lastDate = LocalDate.now();
    }

    public ItemStack getBoughtArmorItem(ItemStack armorItem) {
        if (!boughtArmorItems.isEmpty()) {
            for (ItemStack boughtArmorItem : boughtArmorItems.keySet()) {
                if (Utils.stripColor(armorItem.getItemMeta().getDisplayName()).equalsIgnoreCase(Utils.stripColor(boughtArmorItem.getItemMeta().getDisplayName())))
                    return boughtArmorItem;
            }
        }
        return null;
    }

    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }

    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }

    public int getRocketsLaunched() {
        return rocketsLaunched;
    }

    public void setRocketsLaunched(int rocketsLaunched) {
        this.rocketsLaunched = rocketsLaunched;
    }

    public int getRocketsDestroyed() {
        return rocketsDestroyed;
    }

    public void setRocketsDestroyed(int rocketsDestroyed) {
        this.rocketsDestroyed = rocketsDestroyed;
    }
}
