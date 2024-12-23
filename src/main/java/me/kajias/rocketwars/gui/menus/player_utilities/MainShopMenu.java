package me.kajias.rocketwars.gui.menus.player_utilities;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.gui.menus.player_utilities.armor_visuals.ArmorItemSelectMenu;
import me.kajias.rocketwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class MainShopMenu extends InventoryGUI
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    public MainShopMenu() { super(true); }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.main-shop.size"), Utils.colorize(menusConfig.getString("menus.main-shop.title")));
    }

    @Override
    public void decorate(Player player) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        if (playerData != null) {
            for (String fillerItemCategory : menusConfig.getConfigurationSection("menus.main-shop.filler-items").getKeys(false)) {
                ItemStack fillerItem = new ItemStack(Material.getMaterial(menusConfig.getString("menus.main-shop.filler-items." + fillerItemCategory + ".material")), 1,
                        (short) menusConfig.getInt("menus.main-shop.filler-items." + fillerItemCategory + ".data"));
                ItemMeta fillerMeta = fillerItem.getItemMeta();
                fillerMeta.setDisplayName(" ");
                fillerItem.setItemMeta(fillerMeta);

                for (int index : menusConfig.getIntegerList("menus.main-shop.filler-items." + fillerItemCategory + ".slots"))
                    this.getInventory().setItem(index, fillerItem);
            }

            for (String bonusCategory : menusConfig.getConfigurationSection("menus.main-shop.bonus-items").getKeys(false)) {
                ItemStack bonusItem = menusConfig.get("menus.main-shop.bonus-items." + bonusCategory + ".type-id") != null ?
                        new ItemStack(menusConfig.getInt("menus.main-shop.bonus-items." + bonusCategory + ".type-id"), 1, (short) menusConfig.getInt("menus.main-shop.bonus-items." + bonusCategory + ".data")) :
                        new ItemStack(Material.getMaterial(menusConfig.getString("menus.main-shop.bonus-items." + bonusCategory + ".material")));
                ItemMeta bonusMeta = bonusItem.getItemMeta();
                bonusMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.main-shop.bonus-items." + bonusCategory + ".name")));
                bonusMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.main-shop.bonus-items." + bonusCategory + ".lore")));
                bonusItem.setItemMeta(bonusMeta);
                InventoryButton bonusPurchaseButton = new InventoryButton()
                        .creator(player1 -> bonusItem)
                        .consumer(event -> {
                            if (!playerData.getBoughtAbilities().containsKey(Utils.stripColor(menusConfig.getString("menus.main-shop.bonus-items." + bonusCategory + ".name")))) {
                                RocketWars.guiManager.openGUI(new ItemPurchaseMenu(bonusItem, null,
                                        menusConfig.getIntegerList("menus.main-shop.bonus-items." + bonusCategory + ".prices.anix"),
                                        menusConfig.getIntegerList("menus.main-shop.bonus-items." + bonusCategory + ".prices.bonus"),
                                        menusConfig.getStringList("menus.main-shop.bonus-items." + bonusCategory + ".prices.benefit"),
                                "bonus"), player);
                            } else {
                                Utils.sendMessage(player, config.getString("messages.item-was-already-bought")
                                        .replace("%period%", String.valueOf(playerData.getBoughtAbilities().get(Utils.stripColor(menusConfig.getString("menus.main-shop.bonus-items." + bonusCategory + ".name"))))));
                                player.closeInventory();
                            }
                        });
                this.setButton(menusConfig.getInt("menus.main-shop.bonus-items." + bonusCategory + ".slot"), bonusPurchaseButton);
            }

            for (String armorMenuCategory : menusConfig.getConfigurationSection("menus.main-shop.armor-menus").getKeys(false)) {
                ItemStack armorMenu = new ItemStack(Material.getMaterial(menusConfig.getString("menus.main-shop.armor-menus." + armorMenuCategory + ".material")));
                LeatherArmorMeta armorMenuMeta = (LeatherArmorMeta) armorMenu.getItemMeta();
                armorMenuMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.main-shop.armor-menus." + armorMenuCategory + ".name")));
                armorMenuMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.main-shop.armor-menus." + armorMenuCategory + ".lore")));
                armorMenuMeta.setColor(Color.WHITE);
                armorMenuMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                armorMenuMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                armorMenuMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                armorMenuMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                armorMenu.setItemMeta(armorMenuMeta);
                InventoryButton armorMenuOpenButton = new InventoryButton()
                        .creator(player1 -> armorMenu)
                        .consumer(event -> {
                            RocketWars.guiManager.openGUI(new ArmorShopMenu(armorMenuCategory), player);
                        });
                this.setButton(menusConfig.getInt("menus.main-shop.armor-menus." + armorMenuCategory + ".slot"), armorMenuOpenButton);
            }

            for (String armorVisualsMenuCategory : menusConfig.getConfigurationSection("menus.main-shop.armor-visuals-menus").getKeys(false)) {
                ItemStack armorVisualsMenu = new ItemStack(Material.getMaterial(
                        menusConfig.getString("menus.main-shop.armor-visuals-menus." + armorVisualsMenuCategory + ".material")), 1, Utils.getRandom(menusConfig.getIntegerList("menus.colors")).shortValue());
                ItemMeta armorVisualsMenuMeta = armorVisualsMenu.getItemMeta();
                armorVisualsMenuMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.main-shop.armor-visuals-menus." + armorVisualsMenuCategory + ".name")));
                armorVisualsMenuMeta.setLore(Utils.colorize(menusConfig.getStringList("menus.main-shop.armor-visuals-menus." + armorVisualsMenuCategory + ".lore")));
                armorVisualsMenu.setItemMeta(armorVisualsMenuMeta);
                InventoryButton armorVisualsMenuOpenButton = new InventoryButton()
                        .creator(player1 -> armorVisualsMenu)
                        .consumer(event -> {
                            RocketWars.guiManager.openGUI(new ArmorItemSelectMenu(armorVisualsMenuCategory), player);
                        });
                this.setButton(menusConfig.getInt("menus.main-shop.armor-visuals-menus." + armorVisualsMenuCategory + ".slot"), armorVisualsMenuOpenButton);
            }
        }

        super.decorate(player);
    }
}
