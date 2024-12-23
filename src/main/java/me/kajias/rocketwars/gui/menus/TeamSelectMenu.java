package me.kajias.rocketwars.gui.menus;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class TeamSelectMenu extends InventoryGUI
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    public TeamSelectMenu() {
        super(true);
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, menusConfig.getInt("menus.team-select.size"), Utils.colorize(menusConfig.getString("menus.team-select.title")));
    }

    @Override
    public void decorate(Player player) {
        Arena arena = Arena.getPlayerArenaMap().get(player);

        for (String itemName : menusConfig.getConfigurationSection("menus.team-select.items").getKeys(false)) {
            ItemStack teamSelect = new ItemStack(Material.getMaterial(menusConfig.getString("menus.team-select.items." + itemName + ".material")), 1,
                    (short) menusConfig.getInt("menus.team-select.items." + itemName + ".data"));
            ItemMeta teamSelectMeta = teamSelect.getItemMeta();
            teamSelectMeta.setDisplayName(Utils.colorize(menusConfig.getString("menus.team-select.items." + itemName + ".name")
                    .replace("%players_now%", String.valueOf(arena.getTeam(TeamColor.valueOf(menusConfig.getString("menus.team-select.items." + itemName + ".team-color"))).size()))
                    .replace("%players_max%", String.valueOf(arena.getAllowedPlayersAmount()))));
            List<String> teamSelectLore;
            if (!arena.getTeam(TeamColor.valueOf(menusConfig.getString("menus.team-select.items." + itemName + ".team-color"))).isEmpty()) {
                teamSelectLore = Utils.colorize(menusConfig.getStringList("menus.team-select.team-select-button-lore.has-players"));
                for (String playerName : arena.getTeam(TeamColor.valueOf(menusConfig.getString("menus.team-select.items." + itemName + ".team-color")))) {
                    teamSelectLore.add(Utils.colorize(menusConfig.getString("menus.team-select.player-list-row").replace("%player_name%", playerName)));
                }
            } else teamSelectLore = Utils.colorize(menusConfig.getStringList("menus.team-select.team-select-button-lore.empty"));
            teamSelectMeta.setLore(teamSelectLore);
            teamSelect.setItemMeta(teamSelectMeta);

            InventoryButton button = new InventoryButton()
                    .creator(player1 -> teamSelect)
                    .consumer(event -> {
                        if (arena.getTeam(TeamColor.valueOf(menusConfig.getString("menus.team-select.items." + itemName + ".team-color"))).size() >= arena.getAllowedPlayersAmount() / 2) {
                            Utils.sendMessage(player, config.getString("messages.team-is-full"));
                        } else {
                            arena.addToTeam(TeamColor.valueOf(menusConfig.getString("menus.team-select.items." + itemName + ".team-color")), player);
                            player.getInventory().getItem(config.getInt("hot-bar-items.team-select-item.slot") - 1).setDurability(teamSelect.getDurability());
                        }
                        player.closeInventory();
                    });
            this.setButton(menusConfig.getInt("menus.team-select.items." + itemName + ".slot") - 1, button);
        }

        super.decorate(player);
    }
}
