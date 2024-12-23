package me.kajias.rocketwars.gui.menus.player_utilities.armor_visuals;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.gui.menus.player_utilities.ItemPurchaseMenu;
import me.kajias.rocketwars.gui.menus.player_utilities.SelectedColorTypePurchaseMenu;
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

public class ColorSelectMenu extends InventoryGUI
{
    private final static FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final static FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final ItemStack selectedArmorItem;
    private final String colorCategory;

    public ColorSelectMenu(ItemStack selectedArmorItem, String colorCategory) {
        super(false);

        this.selectedArmorItem = selectedArmorItem;
        this.colorCategory = colorCategory;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.color-select.size"), Utils.colorize(menusConfig.getString("menus.color-select.title")));
    }

    @Override
    public void decorate(Player player) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        for (String colorName : menusConfig.getConfigurationSection("menus.color-select.colors").getKeys(false)) {
            ItemStack glass = new ItemStack(Material.getMaterial(menusConfig.getString("menus.color-select.material")), 1,
                    (short) menusConfig.getInt("menus.color-select.colors." + colorName + ".data"));
            ItemMeta glassMeta = glass.getItemMeta();
            glassMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-select.hover-text")));
            glass.setItemMeta(glassMeta);
            InventoryButton colorSelectButton = new InventoryButton()
                    .creator(player1 -> glass)
                    .consumer(event -> {
                        if (playerData.getBoughtColors().containsKey(colorCategory + ":" + DyeColor.getByWoolData(glass.getData().getData()).getColor().asRGB())) {
                            switch (colorCategory) {
                                case "color-boots":
                                case "color-helmet":
                                    LeatherArmorMeta armorMeta = (LeatherArmorMeta) playerData.getBoughtArmorItem(selectedArmorItem).getItemMeta();
                                    armorMeta.setColor(Color.fromRGB(DyeColor.getByWoolData(glass.getData().getData()).getColor().asRGB()));
                                    playerData.getBoughtArmorItem(selectedArmorItem).setItemMeta(armorMeta);
                                    break;
                                case "color-glass":
                                    playerData.setGlassColor(String.valueOf(DyeColor.getByWoolData(glass.getData().getData())));
                                    break;
                            }
                            Utils.sendMessage(player, config.getString("messages.color-select"));
                            Sounds.ORB_PICKUP.play(player);
                            player.closeInventory();
                        } else {
                            RocketWars.guiManager.openGUI(new ItemPurchaseMenu(glass, selectedArmorItem,
                                    menusConfig.getIntegerList("menus.color-select.colors." + colorName + ".prices.anix"),
                                    menusConfig.getIntegerList("menus.color-select.colors." + colorName + ".prices.bonus"),
                                    menusConfig.getStringList("menus.color-select.colors." + colorName + ".prices.benefit"),
                                    colorCategory), player);
                        }
                    });
            this.setButton(menusConfig.getInt("menus.color-select.colors." + colorName + ".slot"), colorSelectButton);
        }

        for (String colorTypeToggleItemCategory : menusConfig.getConfigurationSection("menus.color-select.buttons").getKeys(false)) {
            ItemStack colorTypeToggleItem = Utils.getCustomTextureHead(menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".player-skull-id"));
            ItemMeta colorTypeToggleMeta = colorTypeToggleItem.getItemMeta();
            colorTypeToggleMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".name")));
            colorTypeToggleMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.color-select.buttons." + colorTypeToggleItemCategory + ".lore")));
            colorTypeToggleItem.setItemMeta(colorTypeToggleMeta);
            InventoryButton colorTypeToggleButton = new InventoryButton()
                    .creator(player1 -> colorTypeToggleItem)
                    .consumer(event -> {
                        if (!playerData.getBoughtAbilities().containsKey(menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".type") + ":" + colorCategory)) {
                            RocketWars.guiManager.openGUI(new SelectedColorTypePurchaseMenu(colorTypeToggleItem,
                                    menusConfig.getIntegerList("menus.color-select.buttons." + colorTypeToggleItemCategory + ".prices.anix"),
                                    menusConfig.getIntegerList("menus.color-select.buttons." + colorTypeToggleItemCategory + ".prices.bonus"),
                                    menusConfig.getStringList("menus.color-select.buttons." + colorTypeToggleItemCategory + ".prices.benefit"),
                                    SelectedColorType.valueOf(menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".type")),
                                    colorCategory), player);
                        } else {
                            playerData.setSelectedColorType(colorCategory, SelectedColorType.valueOf(menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".type")));
                            if (menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".type").equalsIgnoreCase("RANDOM_SELECTION"))
                                Utils.sendMessage(player, config.getString("messages.random-color-selection-select"));
                            if (menusConfig.getString("menus.color-select.buttons." + colorTypeToggleItemCategory + ".type").equalsIgnoreCase("RAINBOW"))
                                Utils.sendMessage(player, config.getString("messages.rainbow-color-select"));
                            Sounds.ORB_PICKUP.play(player);
                            player.closeInventory();
                        }
                    });
            this.setButton(menusConfig.getInt("menus.color-select.buttons." + colorTypeToggleItemCategory + ".slot"), colorTypeToggleButton);
        }

        ItemStack colorBlockListMenuItem = new ItemStack(Material.getMaterial(menusConfig.getString("menus.color-select.color-block-list-menu-button.material")));
        ItemMeta colorBlockListMenuMeta = colorBlockListMenuItem.getItemMeta();
        colorBlockListMenuMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.color-select.color-block-list-menu-button.name")));
        colorBlockListMenuMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.color-select.color-block-list-menu-button.lore")));
        colorBlockListMenuItem.setItemMeta(colorBlockListMenuMeta);
        InventoryButton colorBlockListMenuButton = new InventoryButton().creator(player1 -> colorBlockListMenuItem)
                .consumer(event -> {
                    if (!playerData.getBoughtColors().isEmpty()) {
                        RocketWars.guiManager.openGUI(new ColorBlockListMenu(colorCategory.split("-")[1]), player);
                    } else {
                        player.closeInventory();
                        Sounds.NOTE_BASS_GUITAR.play(player);
                        Utils.sendMessage(player, menusConfig.getString("menus.color-select.do-not-have-color-message"));
                    }
                });
        this.setButton(menusConfig.getInt("menus.color-select.color-block-list-menu-button.slot"), colorBlockListMenuButton);

        super.decorate(player);
    }
}
