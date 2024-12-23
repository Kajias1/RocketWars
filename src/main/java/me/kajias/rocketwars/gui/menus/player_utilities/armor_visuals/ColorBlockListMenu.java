package me.kajias.rocketwars.gui.menus.player_utilities.armor_visuals;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ColorBlockListMenu extends InventoryGUI
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final List<Integer> blockedColorSlots = menusConfig.getIntegerList("menus.color-black-list.blocked-color-slots");
    private static final List<Integer> allowedColorSlots = menusConfig.getIntegerList("menus.color-black-list.allowed-color-slots");

    private final String colorType;

    public ColorBlockListMenu(String colorType) {
        super(false);
        this.colorType = colorType;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.color-black-list.size"), Utils.colorize(menusConfig.getString("menus.color-black-list.title")));
    }

    @Override
    public void decorate(Player player) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        for (String fillerItemCategory : menusConfig.getConfigurationSection("menus.color-black-list.filler-items").getKeys(false)) {
            ItemStack fillerItem = new ItemStack(Material.getMaterial(menusConfig.getString("menus.color-black-list.filler-items." + fillerItemCategory + ".material")), 1,
                    (short) menusConfig.getInt("menus.color-black-list.filler-items." + fillerItemCategory + ".data"));
            ItemMeta fillerMeta = fillerItem.getItemMeta();
            fillerMeta.setDisplayName(" ");
            fillerItem.setItemMeta(fillerMeta);

            for (int index : menusConfig.getIntegerList("menus.color-black-list.filler-items." + fillerItemCategory + ".slots"))
                this.getInventory().setItem(index, fillerItem);
        }

        ItemStack explainer = Utils.getCustomTextureHead(menusConfig.getString("menus.color-black-list.explainer.player-skull-id"));
        ItemMeta explainerMeta = explainer.getItemMeta();
        explainerMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-black-list.explainer.name")));
        explainerMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.color-black-list.explainer.lore")));
        explainer.setItemMeta(explainerMeta);
        this.getInventory().setItem(menusConfig.getInt("menus.color-black-list.explainer.slot"), explainer);

        if (playerData != null) {
            int allowedColorIndex = 0;
            for (String boughtColor : playerData.getAllowedBoughtColors(colorType)) {
                ItemStack allowedColorItem = new ItemStack(Material.STAINED_GLASS, 1, DyeColor.getByColor(Color.fromRGB(Integer.parseInt(boughtColor))).getWoolData());
                ItemMeta allowedColorMeta = allowedColorItem.getItemMeta();
                allowedColorMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-black-list.move-to-blocked-list-hover-message")));
                allowedColorItem.setItemMeta(allowedColorMeta);
                InventoryButton moveToBlockedListButton = new InventoryButton()
                        .creator(player1 -> allowedColorItem)
                        .consumer(event -> {
                            if (playerData.getAllowedBoughtColors(colorType).size() > 1) {
                                playerData.getBlockedColors().add("color-" + colorType + ":" + boughtColor);
                                RocketWars.guiManager.openGUI(new ColorBlockListMenu(colorType), player);
                            } else {
                                player.closeInventory();
                                Sounds.NOTE_BASS_GUITAR.play(player);
                                Utils.sendMessage(player, menusConfig.getString("menus.color-black-list.cant-move-to-blocked-list-all-colors"));
                            }
                        });
                this.setButton(allowedColorSlots.get(allowedColorIndex), moveToBlockedListButton);
                allowedColorIndex++;
            }

            int blockColorIndex = 0;
            for (String blockedColor : playerData.getBlockedColors()) {
                if (blockedColor.split(":")[0].split("-")[1].equalsIgnoreCase(colorType)) {
                    ItemStack blockedColorItem = new ItemStack(Material.STAINED_GLASS, 1, DyeColor.getByColor(Color.fromRGB(Integer.parseInt(blockedColor.split(":")[1]))).getWoolData());
                    ItemMeta blockedColorMeta = blockedColorItem.getItemMeta();
                    blockedColorMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-black-list.move-to-allowed-list-hover-message")));
                    blockedColorItem.setItemMeta(blockedColorMeta);
                    InventoryButton moveToAllowedListButton = new InventoryButton()
                            .creator(player1 -> blockedColorItem)
                            .consumer(event -> {
                                playerData.getBlockedColors().remove(blockedColor);
                                RocketWars.guiManager.openGUI(new ColorBlockListMenu(colorType), player);
                            });
                    this.setButton(blockedColorSlots.get(blockColorIndex), moveToAllowedListButton);
                    blockColorIndex++;
                }
            }
        }

        super.decorate(player);
    }
}
