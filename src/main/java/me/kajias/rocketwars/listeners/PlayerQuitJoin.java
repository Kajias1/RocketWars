package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitJoin implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Utils.teleportToLobby(e.getPlayer());

        GamePlayer gamePlayer = DataConfiguration.getPlayerDataFromConfig(e.getPlayer().getUniqueId());
        gamePlayer.setPlayerName(e.getPlayer().getName());
        if (RocketWars.loadedPlayerData.stream().noneMatch(x -> x.getUniqueId().equals(e.getPlayer().getUniqueId()))) {
            RocketWars.loadedPlayerData.add(gamePlayer);
            DataConfiguration.savePlayerData(gamePlayer);
        }

        e.setJoinMessage("");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Arena arena = Arena.getPlayerArenaMap().get(e.getPlayer());
        if (arena != null) {
            arena.removePlayer(e.getPlayer());
        }

        e.setQuitMessage("");
    }
}
