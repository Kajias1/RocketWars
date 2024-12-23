package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.Game;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockBreakPlace implements Listener
{
    private final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    @EventHandler
    public void onBlocksExplode(EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            if (block != null) {
                List<Entity> nearbyEntities = new ArrayList<>(block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2));
                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        entity.remove();
                    }
                }
                if (checkForGlass(block, false)) break;
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (block != null)
                if (checkForGlass(block, false)) break;
        }
    }

    @EventHandler
    public void onPlayerBreakBlock(BlockBreakEvent e) {
        if (e.getBlock() != null)
            if (checkForGlass(e.getBlock(), true)) e.setCancelled(true);

        Player player = e.getPlayer();
        if (player != null) {
            Arena arena = Arena.getPlayerArenaMap().get(player);
            if (arena != null) {
                Location location = e.getBlock().getLocation();
                List<Entity> nearbyEntities = new ArrayList<>(location.getWorld().getNearbyEntities(location, 2, 2, 2));
                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        entity.remove();
                    }
                }
                if (arena.getTeamColor(player) == TeamColor.RED && location.getZ() > -1) {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes('&', config.getString("messages.cant-break-enemy-base-blocks")), 1, 50, 1);
                    Sounds.NOTE_BASS_GUITAR.play(player);
                    e.setCancelled(true);
                } else if (arena.getTeamColor(player) == TeamColor.GREEN && location.getZ() < -88) {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes('&', config.getString("messages.cant-break-enemy-base-blocks")), 1, 50, 1);
                    Sounds.NOTE_BASS_GUITAR.play(player);
                    e.setCancelled(true);
                }
                if (e.getBlock().getType() == Material.TNT) {
                    if (arena.getGame() != null) {
                       arena.getGame().getPlayersDataForGame().stream().filter(x -> x.getUniqueId().equals(player.getUniqueId())).findAny()
                               .ifPresent(playerDataForGame -> playerDataForGame.setRocketsDestroyed(playerDataForGame.getRocketsDestroyed() + 1));
                    }
                }
            }
        }
    }

    private boolean checkForGlass(Block block, boolean dummy) {
        Arena arena = Arena.getPlayerArenaMap().values().stream().filter(x -> block.getWorld().equals(x.getWorld())).findAny().orElse(null);

        if (arena != null && arena.getState() == ArenaState.STARTED) {
            Game game = arena.getGame();
            if (block.getType() == Material.STAINED_GLASS) {
                if (block.getX() >= -18 && block.getX() <= 72 && block.getY() >= 30 && block.getY() <= 60) {
                    if (block.getZ() == 27) {
                        if (!dummy) game.endGame(TeamColor.RED);
                        return true;
                    }
                    if (block.getZ() == -117) {
                        if (!dummy) game.endGame(TeamColor.GREEN);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
