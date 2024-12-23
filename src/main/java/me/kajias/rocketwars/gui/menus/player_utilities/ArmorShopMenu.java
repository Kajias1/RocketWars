package me.kajias.rocketwars.gui.menus.player_utilities;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.objects.GamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArmorShopMenu extends InventoryGUI
{
    private final static FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final static FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final String armorCategory;

    public ArmorShopMenu(String armorCategory) {
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
            ItemStack armorItem = createArmorItem(armorCategory, armorItemId);
            InventoryButton buyArmorItemButton = new InventoryButton()
                    .creator(player1 -> armorItem)
                    .consumer(event -> {
                        if (playerData.getBoughtArmorItem(armorItem) == null) {
                            RocketWars.guiManager.openGUI(new ItemPurchaseMenu(armorItem, null,
                                    menusConfig.getIntegerList("menus.armor-shop." + armorCategory + ".entries." + armorItemId + ".prices.anix"),
                                    menusConfig.getIntegerList("menus.armor-shop." + armorCategory + ".entries." + armorItemId + ".prices.bonus"),
                                    Arrays.asList("", "", "", "", "", ""),
                                    armorItem.getType() == Material.LEATHER_BOOTS ? "armor-boots" : "armor-helmet"), player);
                        } else {
                            Utils.sendMessage(player, config.getString("messages.item-select"));
                            if (armorItem.getType() == Material.LEATHER_BOOTS) playerData.setBoots(playerData.getBoughtArmorItem(armorItem));
                            else if (armorItem.getType() == Material.LEATHER_HELMET) playerData.setHelmet(playerData.getBoughtArmorItem(armorItem));
                            player.closeInventory();
                        }
                    });
            this.setButton(menusConfig.getInt("menus.armor-shop." + armorCategory + ".entries." + armorItemId + ".slot"), buyArmorItemButton);
        }

        super.decorate(player);
    }

    public static ItemStack createArmorItem(String armorCategory, String armorItemId) {
        String armorItemPath = "menus.armor-shop." + armorCategory + ".entries." + armorItemId;

        ItemStack armorItem = new ItemStack(Material.getMaterial(menusConfig.getString("menus.armor-shop." + armorCategory + ".material")));
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) armorItem.getItemMeta();
        armorMeta.setDisplayName(Utils.colorize(menusConfig.getString(armorItemPath + ".name")));
        List<String> armorLore = new ArrayList<>();
        if (!menusConfig.getStringList(armorItemPath + ".lore.enchantments").isEmpty()) {
            armorLore.addAll(Utils.colorize(menusConfig.getStringList("menus.armor-shop." + armorCategory + ".description-format.enchantments-list")));
            for (String enchantmentName : menusConfig.getStringList(armorItemPath + ".lore.enchantments"))
                armorLore.add(Utils.colorize(ChatColor.getLastColors(armorMeta.getDisplayName()) + " - " + Utils.colorize(enchantmentName)));
        }
        if (!menusConfig.getStringList(armorItemPath + ".lore.effects").isEmpty()) {
            armorLore.addAll(Utils.colorize(menusConfig.getStringList("menus.armor-shop." + armorCategory + ".description-format.effects-list")));
            for (String enchantmentName : menusConfig.getStringList(armorItemPath + ".lore.effects"))
                armorLore.add(Utils.colorize(ChatColor.getLastColors(armorMeta.getDisplayName()) + " - "  + Utils.colorize(enchantmentName)));
        }
        armorMeta.setLore(armorLore);
        armorMeta.setColor(Color.fromRGB(menusConfig.getInt(armorItemPath + ".color")));
        armorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        armorMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        armorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        if (menusConfig.getConfigurationSection(armorItemPath + ".enchantments") != null) {
            for (String enchantmentName : menusConfig.getConfigurationSection(armorItemPath + ".enchantments").getKeys(false))
                armorMeta.addEnchant(Enchantment.getByName(enchantmentName), menusConfig.getInt(armorItemPath + ".enchantments." + enchantmentName), true);
        }
        armorItem.setItemMeta(armorMeta);

        return armorItem;
    }
}
