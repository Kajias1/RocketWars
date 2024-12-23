package me.kajias.rocketwars.gui.menus.player_utilities.armor_visuals;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.gui.menus.player_utilities.ArmorShopMenu;
import me.kajias.rocketwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ArmorItemSelectMenu extends InventoryGUI
{
    private final static FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final static FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final String armorCategory;

    public ArmorItemSelectMenu(String armorCategory) {
        super(false);
        this.armorCategory = armorCategory;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.armor-shop.size"), Utils.colorize(menusConfig.getString("menus.armor-shop.title")));
    }

    @Override
    public void decorate(Player player) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        for (String armorItemId : menusConfig.getConfigurationSection("menus.armor-shop." + armorCategory + ".entries").getKeys(false)) {
            ItemStack armorItem = ArmorShopMenu.createArmorItem(armorCategory, armorItemId);
            if (playerData.getBoughtArmorItem(armorItem) == null) {
                armorItem.setType(Material.getMaterial(menusConfig.getInt("menus.armor-shop.fillers.not-bought.material")));
                armorItem.setDurability((short) menusConfig.getInt("menus.armor-shop.fillers.not-bought.data"));
                ItemMeta armorMeta = armorItem.getItemMeta();
                armorMeta.removeEnchant(Enchantment.DURABILITY);
                armorMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.armor-shop.fillers.not-bought.lore")));
                armorItem.setItemMeta(armorMeta);
                this.getInventory().setItem(menusConfig.getInt("menus.armor-shop." + armorCategory + ".entries." + armorItemId + ".slot"), armorItem);
            } else {
                LeatherArmorMeta armorMeta = (LeatherArmorMeta) armorItem.getItemMeta();
                armorMeta.setColor(((LeatherArmorMeta) playerData.getBoughtArmorItem(armorItem).getItemMeta()).getColor());
                armorItem.setItemMeta(armorMeta);
                InventoryButton buyArmorItemButton = new InventoryButton()
                        .creator(player1 -> armorItem)
                        .consumer(event -> {
                            RocketWars.guiManager.openGUI(new ArmorVisualsMenu(armorItem), player);
                        });
                this.setButton(menusConfig.getInt("menus.armor-shop." + armorCategory + ".entries." + armorItemId + ".slot"), buyArmorItemButton);
            }
        }

        super.decorate(player);
    }
}
