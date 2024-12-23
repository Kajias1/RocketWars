package me.kajias.rocketwars.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.fastboard.FastBoard;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.ArenaState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class Scoreboard implements Listener 
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();
    
    public static Map<UUID, FastBoard> boards = new HashMap<>();

    public Scoreboard() {
        RocketWars.INSTANCE.getServer().getScheduler().scheduleSyncRepeatingTask(RocketWars.INSTANCE, () -> {
            for (FastBoard board : boards.values()) {
                Player player = board.getPlayer();
                Arena arena = Arena.getPlayerArenaMap().get(player);
                List<String> lines = new ArrayList<>();

                if (arena != null) {
                    lines = PlaceholderAPI.setPlaceholders(player, config.getStringList("scoreboard." + arena.getState().toString().toLowerCase()));
                } else lines = PlaceholderAPI.setPlaceholders(player, config.getStringList("scoreboard.lobby"));

                updateBoard(board, lines.toArray(new String[0]));
            }
        }, 0L, 2L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        FastBoard board = new FastBoard(player);
        board.updateTitle(Utils.colorize(config.getString("scoreboard.title")));
        boards.put(player.getUniqueId(), board);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) board.delete();
    }

    private void updateBoard(FastBoard board, String ... lines) {
        for (int a = 0; a < lines.length; ++a)
            lines[a] = Utils.colorize(lines[a]);
        board.updateLines(lines);
    }
}
