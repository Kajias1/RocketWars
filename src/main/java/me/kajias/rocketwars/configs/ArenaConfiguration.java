package me.kajias.rocketwars.configs;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.ArenaType;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ArenaConfiguration
{
    private static final BaseConfiguration arenaConfig = new BaseConfiguration("arenas");

    public static void initialize() { arenaConfig.load(); }

    public static void saveArena(Arena arena) {
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".world-name", arena.getWorldName());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".template-name", arena.getTemplateName());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".arena-type", arena.getType().toString());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.world", arena.getSpawnLocWaiting().getWorld().getName());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.waiting-x", arena.getSpawnLocWaiting().getX());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.waiting-y", arena.getSpawnLocWaiting().getY());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.waiting-z", arena.getSpawnLocWaiting().getZ());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.waiting-yaw", arena.getSpawnLocWaiting().getYaw());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.red-x", arena.getSpawnLoc(TeamColor.RED).getX());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.red-y", arena.getSpawnLoc(TeamColor.RED).getY());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.red-z", arena.getSpawnLoc(TeamColor.RED).getZ());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.red-yaw", arena.getSpawnLoc(TeamColor.RED).getYaw());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.green-x", arena.getSpawnLoc(TeamColor.GREEN).getX());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.green-y", arena.getSpawnLoc(TeamColor.GREEN).getY());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.green-z", arena.getSpawnLoc(TeamColor.GREEN).getZ());
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-points.green-yaw", arena.getSpawnLoc(TeamColor.GREEN).getYaw());
        arenaConfig.save();
    }

    public static void addArena(Arena arena) {
        arenaConfig.getConfig().set("arenas." + arena.getName() + ".spawn-point.world", arena.getWorldName());
        arenaConfig.save();
    }

    public static void removeArena(String name) {
        arenaConfig.getConfig().set("arenas." + name, null);
        arenaConfig.save();
    }
    
    public static List<Arena> getArenasFromConfig() {
        List<Arena> result = new ArrayList<>();
        Arena arena;
        if(!arenaConfig.getConfig().getConfigurationSection("arenas").getKeys(false).isEmpty()) {
            for(String arenaName : arenaConfig.getConfig().getConfigurationSection("arenas").getKeys(false)) {
                arena = new Arena(arenaName, arenaConfig.getConfig().getString("arenas." + arenaName + ".world-name"),
                        arenaConfig.getConfig().getString("arenas." + arenaName + ".template-name"));
                arena.setType(ArenaType.valueOf(arenaConfig.getConfig().getString("arenas." + arenaName + ".arena-type")));
                arena.setSpawnLocWaiting(new Location(Bukkit.getWorld(arena.getWorldName()),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.waiting-x"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.waiting-y"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.waiting-z"),
                        (float) arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.waiting-yaw"),
                        0.0f
                ));
                arena.setSpawnLoc(TeamColor.RED, new Location(Bukkit.getWorld(arena.getWorldName()),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.red-x"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.red-y"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.red-z"),
                        (float) arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.red-yaw"),
                        0.0f
                ));
                arena.setSpawnLoc(TeamColor.GREEN, new Location(Bukkit.getWorld(arena.getWorldName()),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.green-x"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + ".spawn-points.green-y"),
                        arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.green-z"),
                        (float) arenaConfig.getConfig().getDouble("arenas." + arenaName + "..spawn-points.green-yaw"),
                        0.0f
                ));
                result.add(arena);
            }
        }

        return result;
    }
}
