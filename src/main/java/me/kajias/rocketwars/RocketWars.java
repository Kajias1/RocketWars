package me.kajias.rocketwars;

import me.kajias.rocketwars.commands.AdminCommand;
import me.kajias.rocketwars.commands.LeaveCommand;
import me.kajias.rocketwars.configs.ArenaConfiguration;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.GUIListener;
import me.kajias.rocketwars.gui.GUIManager;
import me.kajias.rocketwars.listeners.*;
import me.kajias.rocketwars.managers.ArenaManager;
import me.kajias.rocketwars.managers.PAPIExpansion;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class RocketWars extends JavaPlugin
{
    public static Plugin INSTANCE = null;
    public static Location lobbyLocation = null;
    public static List<GamePlayer> loadedPlayerData = new ArrayList<>();
    public static GUIManager guiManager = null;
    public static GUIListener guiListener = null;
    private static PlayerPoints playerPoints = null;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        INSTANCE = this;
        ArenaConfiguration.initialize();
        DataConfiguration.initialize();
        MenuConfiguration.initialize();
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            printLog(Level.WARNING, "Для работы плагина требуется PlaceholderAP");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            printLog(Level.WARNING, "Для работы плагина требуется PlayerPoints");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            playerPoints = (PlayerPoints) this.getServer().getPluginManager().getPlugin("PlayerPoints");
        }

        if (!setupEconomy() ) {
            printLog(Level.WARNING, "Для работы плагина требуется Vault!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().getConfigurationSection("lobby-spawn-point") != null) {
            lobbyLocation = new Location(
                    Bukkit.getWorld(getConfig().getString("lobby-spawn-point.world")),
                    getConfig().getDouble("lobby-spawn-point.x"),
                    getConfig().getDouble("lobby-spawn-point.y"),
                    getConfig().getDouble("lobby-spawn-point.z"));
            lobbyLocation.setYaw((float) getConfig().getDouble("lobby-spawn-point.yaw"));
        } else printLog(Level.WARNING, getConfig().getString("messages.log.no-lobby"));

        printLog(Level.FINE, "Загружаем данные игроков...");
        loadedPlayerData.addAll(DataConfiguration.getPlayersFromConfig());
        printLog(Level.FINE, "Загружено данных " + loadedPlayerData.size() + " игроков");
        loadedPlayerData.forEach(DataConfiguration::savePlayerData);

        try {
            for (Arena arena : ArenaConfiguration.getArenasFromConfig()) {
                if (ArenaManager.loadArena(arena)) {
                    printLog(Level.FINE, "Арена с именем \"" + arena.getName() + "\" была успешно создана.");
                }
            }
        } catch (Exception ignored) {}

        guiManager = new GUIManager();
        guiListener = new GUIListener(guiManager);

        getServer().getPluginManager().registerEvents(guiListener, this);
        getServer().getPluginManager().registerEvents(new BlockBreakPlace(), this);
        getServer().getPluginManager().registerEvents(new DisabledEvents(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamage(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteract(), this);
        getServer().getPluginManager().registerEvents(new PlayerMove(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitJoin(), this);
        getServer().getPluginManager().registerEvents(new PlayerUseChat(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHit(), this);
        getServer().getPluginManager().registerEvents(new Scoreboard(), this);

        getCommand("rw").setExecutor(new AdminCommand());
        getCommand("rocketwars").setExecutor(new AdminCommand());
        getCommand("leave").setExecutor(new LeaveCommand());

        PAPIExpansion.registerHook();
    }

    @Override
    public void onDisable() {
        loadedPlayerData.forEach(DataConfiguration::savePlayerData);
    }

    public static PlayerPoints getPlayerPoints() {
        return playerPoints;
    }

    public static Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void printLog(Level level, String message) {
        Bukkit.getLogger().log(level, Utils.colorize(this.getConfig().getString("messages.log.prefix") + message));
    }
}