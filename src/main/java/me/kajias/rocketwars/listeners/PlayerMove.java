package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.SelectedColorType;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMove implements Listener
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final List<UUID> playerDisableFlyList = new ArrayList<>();

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Arena arena = Arena.getPlayerArenaMap().get(player);
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

        if (arena != null) {
            if (arena.getState() == ArenaState.STARTED) {
                if (player.getLocation().getY() <= 0) {
                    arena.getGame().teleportToInGameSpawnPoint(player);
                }
            }

            if (playerData != null && playerData.getGlassColor() != null) {
                Block block = player.getLocation().subtract(0, 1, 0).getBlock();
                if (block.getType() == Material.STAINED_GLASS) {
                    if (playerData.getSelectedColorTypeGlass() == SelectedColorType.RAINBOW) {
                        try {
                            playerData.setGlassColor(String.valueOf(DyeColor.getByColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("glass"))))));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    if (!playerData.getAllowedBoughtColors("glass").contains(String.valueOf(DyeColor.getByWoolData(block.getData()).getColor().asRGB())))
                        block.setData(DyeColor.valueOf(playerData.getGlassColor()).getWoolData());
                }
            }
        }
    }

    @EventHandler
    public void setVelocity(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();
        Arena arena = Arena.getPlayerArenaMap().get(player);

        if (arena != null && arena.getState() == ArenaState.STARTED && player.getGameMode() != GameMode.CREATIVE) {
            GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

            e.setCancelled(true);
            if (playerData != null && !playerDisableFlyList.contains(player.getUniqueId()) &&
                    playerData.getBoughtAbilities().containsKey(Utils.stripColor(menusConfig.getString("menus.main-shop.bonus-items.double-jump.name")))) {
                playerDisableFlyList.add(player.getUniqueId());
                player.setAllowFlight(false);
                player.setVelocity(e.getPlayer().getLocation().getDirection().normalize().setY(1.1f));
                Sounds.ENDERDRAGON_WINGS.play(player);
                player.setFallDistance(0.0f);

                new BukkitRunnable() {
                    final int coolDownMax = config.getInt("game-config.double-jump-cool-down-ticks");
                    int coolDown = coolDownMax;
                    String coolDownBar;

                    @Override
                    public void run() {
                        coolDownBar = Utils.colorize(new String(new char[coolDownMax - coolDown]).replace("\0", "&6|") + new String(new char[coolDown]).replace("\0", "&8|"));
                        Utils.sendActionBar(player, coolDownBar + "        ");
                        coolDown--;
                        if (coolDown <= 0) {
                            coolDownBar = Utils.colorize(new String(new char[coolDownMax]).replace("\0", "&a|"));
                            Utils.sendActionBar(player, coolDownBar + "        ");
                            playerDisableFlyList.remove(player.getUniqueId());
                            this.cancel();
                            player.setAllowFlight(true);
                        }
                    }
                }.runTaskTimer(RocketWars.INSTANCE, 0L, 2L);
            }
        }
    }
}
