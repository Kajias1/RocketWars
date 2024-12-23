package me.kajias.rocketwars.commands;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.objects.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            FileConfiguration config = RocketWars.INSTANCE.getConfig();
            Player player = (Player) sender;

            if (args.length == 0) {
                if (Arena.getPlayerArenaMap().containsKey(player)) {
                    Arena.getPlayerArenaMap().get(player).removePlayer(player);
                } else Utils.sendMessage(player, config.getString("messages.not-in-game"));
            }
        }

        return true;
    }
}
