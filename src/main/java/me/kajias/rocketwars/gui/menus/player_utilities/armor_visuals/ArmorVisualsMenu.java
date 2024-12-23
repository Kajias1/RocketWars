package me.kajias.rocketwars.gui.menus.player_utilities.armor_visuals;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Iterator;
import java.util.Random;

public class ArmorVisualsMenu extends InventoryGUI
{
    private final static FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final static FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final ItemStack selectedArmor;
    private final String armorType;

    public ArmorVisualsMenu(ItemStack selectedArmor) {
        super(true);
        this.selectedArmor = selectedArmor;
        if (selectedArmor.getType() == Material.LEATHER_BOOTS) armorType = "boots";
        else if (selectedArmor.getType() == Material.LEATHER_HELMET) armorType = "helmet";
        else armorType = null;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.armor-visuals-shop.size"), Utils.colorize(menusConfig.getString("menus.armor-visuals-shop.title")));
    }

    @Override
    public void decorate(Player player) {
        this.getInventory().setItem(menusConfig.getInt("menus.armor-visuals-shop.selected-armor-slot"), selectedArmor);

        for (String fillerItemCategory : menusConfig.getConfigurationSection("menus.armor-visuals-shop.filler-items").getKeys(false)) {
            ItemStack fillerItem = new ItemStack(Material.getMaterial(menusConfig.getString("menus.armor-visuals-shop.filler-items." + fillerItemCategory + ".material")), 1,
                    (short) menusConfig.getInt("menus.armor-visuals-shop.filler-items." + fillerItemCategory + ".data"));
            ItemMeta fillerMeta = fillerItem.getItemMeta();
            fillerMeta.setDisplayName(" ");
            fillerItem.setItemMeta(fillerMeta);

            for (int index : menusConfig.getIntegerList("menus.armor-visuals-shop.filler-items." + fillerItemCategory + ".slots"))
                this.getInventory().setItem(index, fillerItem);
        }

        for (String visualsSelectCategory : menusConfig.getConfigurationSection("menus.armor-visuals-shop.visuals." + armorType).getKeys(false)) {
            Material itemMaterial = Material.getMaterial(menusConfig.getString("menus.armor-visuals-shop.visuals." + armorType + "." + visualsSelectCategory + ".material"));
            ItemStack visualsSelect = new ItemStack(itemMaterial, 1, itemMaterial == Material.STAINED_GLASS ? Utils.getRandom(menusConfig.getIntegerList("menus.colors")).shortValue() : 0);
            ItemMeta visualsSelectMeta = visualsSelect.getItemMeta();
            visualsSelectMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.armor-visuals-shop.visuals." + armorType + "." + visualsSelectCategory + ".name")));
            visualsSelectMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.armor-visuals-shop.visuals." + armorType + "." + visualsSelectCategory + ".lore")));
            try {
                ((LeatherArmorMeta) visualsSelectMeta).setColor(((LeatherArmorMeta) selectedArmor.getItemMeta()).getColor());
            } catch (ClassCastException ignored) {}
            visualsSelectMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            visualsSelectMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            visualsSelectMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            visualsSelect.setItemMeta(visualsSelectMeta);
            InventoryButton visualsSelectButton = new InventoryButton()
                    .creator(player1 -> visualsSelect)
                    .consumer(event -> {
                        // this was hardcoded deliberately, don't change pls
                        if (!visualsSelectCategory.equalsIgnoreCase("fireworks")) {
                            RocketWars.guiManager.openGUI(new ColorSelectMenu(selectedArmor, visualsSelectCategory), player);
                        }
                    });
            this.setButton(menusConfig.getInt("menus.armor-visuals-shop.visuals." + armorType + "." + visualsSelectCategory + ".slot"), visualsSelectButton);
        }

        super.decorate(player);
    }
}
