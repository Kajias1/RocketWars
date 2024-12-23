package me.kajias.rocketwars.gui.menus;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.gui.InventoryButton;
import me.kajias.rocketwars.gui.InventoryGUI;
import me.kajias.rocketwars.managers.ArenaManager;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaSelectMenu extends InventoryGUI
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    public ArenaSelectMenu() { super(true); }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, config.getInt("arena-description-format.size"), Utils.colorize(config.getString("arena-description-format.menu-title")));
    }

    @Override
    public void decorate(Player player) {
        List<Integer> arenaSlots = config.getIntegerList("arena-description-format.arena-slots");

        ItemStack fillerItem = new ItemStack(Material.STAINED_GLASS, 1, (short) 15);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.setDisplayName(" ");
        fillerItem.setItemMeta(fillerItemMeta);
        for (int i : arenaSlots) {
            this.getInventory().setItem(i, fillerItem);
        }

        List<Arena> arenas = ArenaManager.getLoadedArenas().stream().filter(a -> a.getState() == ArenaState.WAITING)
                .sorted(Comparator.comparing(arena -> arena.getPlayers().size())).collect(Collectors.toList());
        Collections.reverse(arenas);

        for (int i = 0; i < arenaSlots.size() + 1; i++) {
            Arena arena;

            try {
                arena = arenas.get(i);

                ItemStack arenaIcon = new ItemStack(Material.STAINED_GLASS, Integer.max(1, arena.getPlayers().size()), (short) (arena.getPlayers().isEmpty() ? 5 : 1));
                ItemMeta arenaIconMeta = arenaIcon.getItemMeta();
                arenaIconMeta.setDisplayName(Utils.colorize(config.getString("arena-description-format.display-name").replace("%arena_name%", arena.getName())));
                List<String> arenaIconLore = new ArrayList<>();
                for (String s : config.getStringList("arena-description-format.lore")) {
                    arenaIconLore.add(Utils.colorize(s.replace("%arena_players%", String.valueOf(arena.getPlayers().size()))
                            .replace("%arena_players_total%", String.valueOf(arena.getAllowedPlayersAmount())).replace("%arena_name%", arena.getName())));
                }
                arenaIconMeta.setLore(arenaIconLore);
                arenaIcon.setItemMeta(arenaIconMeta);
                InventoryButton arenaJoinButton = new InventoryButton()
                        .creator(player1 -> arenaIcon)
                        .consumer(event -> {arena.addPlayer(player);});
                int slot = arenaSlots.get(i + 1);
                this.setButton(slot - 1, arenaJoinButton);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        super.decorate(player);
    }
}
