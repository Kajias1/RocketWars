package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerUseChat implements Listener
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    @EventHandler
    public void onCommandExecution(PlayerCommandPreprocessEvent e) {
        if (!e.getPlayer().isOp()) {
            if (Arena.getPlayerArenaMap().get(e.getPlayer()) != null && !e.getMessage().equals("/leave")) {
                Utils.sendMessage(e.getPlayer(), config.getString("messages.no-commands-in-game"));
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String format = config.getString("messages.game-chat-format.default");
        Arena arena = Arena.getPlayerArenaMap().get(player);
        if (arena != null && (arena.getState() == ArenaState.STARTED || arena.getState() == ArenaState.ENDING)) {
            format = config.getString("messages.game-chat-format.team");
            if (!e.getMessage().isEmpty() && e.getMessage().toCharArray()[0] == '!') {
                e.setMessage(e.getMessage().replace("!", ""));
                format = config.getString("messages.game-chat-format.global");
            } else {
                e.getRecipients().removeIf(recipient -> arena.getTeamColor(recipient) != arena.getTeamColor(e.getPlayer()));
            }
            if (arena.getTeamColor(player) == TeamColor.RED) format = format.replace("%team_color%", "&c");
            else if (arena.getTeamColor(player) == TeamColor.GREEN) format = format.replace("%team_color%", "&a");
        }
        e.getRecipients().removeIf(recipient -> recipient.getWorld() != e.getPlayer().getWorld());
        e.setFormat(Utils.colorize(format));
    }
}
