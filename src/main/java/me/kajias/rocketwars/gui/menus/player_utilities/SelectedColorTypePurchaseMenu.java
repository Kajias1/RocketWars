package me.kajias.rocketwars.gui.menus.player_utilities;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.SelectedColorType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectedColorTypePurchaseMenu extends InventoryGUI
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final static FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final static List<Integer> yellowFillerSlots = Arrays.asList(0, 1, 2, 3, 4, 5, 9, 18, 19, 20, 21, 22, 23);
    private final static List<Integer> purpleFillerSlots = Arrays.asList(6, 7, 8, 15, 17, 24, 25, 26);
    private final static List<Integer> sellingItemSlots = Arrays.asList(10, 11, 12, 13, 14, 16);
    private final static List<Integer> sellingItemPeriods = Arrays.asList(15, 31, 60, 90, 170, 365);

    private final ItemStack sellingItem;
    private final List<Integer> pricesAnix;
    private final List<Integer> pricesBonus;
    private final List<String> benefit;
    private final SelectedColorType type;
    private final String itemType;

    public SelectedColorTypePurchaseMenu(ItemStack sellingItem, List<Integer> pricesAnix, List<Integer> pricesBonus, List<String> benefit, SelectedColorType type, String itemType) {
        super(false);
        this.sellingItem = sellingItem;
        this.pricesAnix = pricesAnix;
        this.pricesBonus = pricesBonus;
        this.benefit = benefit;
        this.type = type;
        this.itemType = itemType;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.item-purchase.size"), Utils.colorize(menusConfig.getString("menus.item-purchase.title")));
    }

    @Override
    public void decorate(Player player) {
        // This was hardcoded deliberately, do not change
        ItemStack fillerYellow = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
        ItemMeta fillerYellowMeta = fillerYellow.getItemMeta();
        fillerYellowMeta.setDisplayName(" ");
        fillerYellow.setItemMeta(fillerYellowMeta);
        ItemStack fillerPurple = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
        ItemMeta fillerPurpleMeta = fillerPurple.getItemMeta();
        fillerPurpleMeta.setDisplayName(" ");
        fillerPurple.setItemMeta(fillerPurpleMeta);

        for (int i : yellowFillerSlots) {
            this.setButton(i, new InventoryButton()
                    .creator(player1 -> fillerYellow)
                    .consumer(event -> {}));
        }
        for (int i : purpleFillerSlots) {
            this.setButton(i, new InventoryButton()
                    .creator(player1 -> fillerPurple)
                    .consumer(event -> {}));
        }

        int index = 0;
        for (int i : sellingItemSlots) {
            ArrayList<String> sellingItemDescription = new ArrayList<>();
            ItemStack sellingItem = this.sellingItem.clone();
            ItemMeta sellingItemMeta = sellingItem.getItemMeta();

            List<String> descriptionFormat = menusConfig.getStringList("menus.item-purchase.item-description-format.both-prices");
            int period = sellingItemPeriods.get(index);
            if (period == sellingItemPeriods.get(sellingItemPeriods.size() - 1)) descriptionFormat = menusConfig.getStringList("menus.item-purchase.item-description-format.anix-prices");
            int priceAnix = pricesAnix.get(index);
            int priceBonus;
            try {
                priceBonus = pricesBonus.get(index);
            } catch (IndexOutOfBoundsException e) {
                priceBonus = 0;
            }
            for (String s : descriptionFormat) {
                s = s.replace("%price_anix%", String.valueOf(priceAnix));
                if (priceBonus >= 0)
                    s = s.replace("%price_bonus%", String.valueOf(priceBonus));
                try {
                    s = s.replace("%color%", menusConfig.getStringList("menus.item-purchase.item-description-format.colors").get(index));
                } catch (IndexOutOfBoundsException e) {
                    s = s.replace("%color%", "");
                };
                s = s.replace("%benefit%", benefit.get(index));
                s = s.replace("%period%", String.valueOf(period));
                sellingItemDescription.add(Utils.colorize( s));
            }

            sellingItemMeta.setLore(sellingItemDescription);
            sellingItem.setItemMeta(sellingItemMeta);

            GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

            int finalPriceBonus = priceBonus;
            this.setButton(i, new InventoryButton()
                    .creator(player1 -> sellingItem)
                    .consumer(event -> {
                        if (playerData != null) {
                            if (event.getClick().isRightClick() && event.getSlot() == sellingItemSlots.get(sellingItemSlots.size() - 1)) {
                                Utils.sendMessage(player, config.getString("messages.cant-buy-for-bonuses"));
                            } else {
                                boolean sufficientMoney = false;
                                if (event.getClick().isLeftClick()) {
                                    if (RocketWars.getPlayerPoints().getAPI().look(player.getUniqueId()) >= priceAnix) {
                                        sufficientMoney = true;
                                        RocketWars.getPlayerPoints().getAPI().take(player.getUniqueId(), priceAnix);
                                    }
                                }
                                if (event.getClick().isRightClick() && event.getSlot() != sellingItemSlots.get(sellingItemSlots.size() - 1)) {
                                    if (RocketWars.getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getUniqueId())) >= finalPriceBonus) {
                                        sufficientMoney = true;
                                        RocketWars.getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), priceAnix);
                                    }
                                }

                                if (!sufficientMoney) {
                                    Utils.sendMessage(player, config.getString("messages.insufficient-money"));
                                } else {
                                    playerData.getBoughtAbilities().put(type.toString() + ":" + itemType, period);
                                    playerData.setSelectedColorType(itemType, type);
                                    if (type == SelectedColorType.RANDOM_SELECTION) Utils.sendMessage(player, config.getString("messages.random-color-selection-purchase-successful"));
                                    if (type == SelectedColorType.RAINBOW) Utils.sendMessage(player, config.getString("messages.rainbow-color-purchase-successful"));
                                    DataConfiguration.savePlayerData(playerData);
                                    Sounds.ORB_PICKUP.play(player);
                                }
                            }
                        }
                        player.closeInventory();
                    }));
            index++;
        }

        super.decorate(player);
    }
}
