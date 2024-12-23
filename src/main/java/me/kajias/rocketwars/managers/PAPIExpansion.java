package me.kajias.rocketwars.managers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.Game;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

public class PAPIExpansion extends PlaceholderExpansion
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    @Override
    public @NotNull String getIdentifier() {
        return "rocketwars";
    }

    @Override
    public @NotNull String getAuthor() {
        return "KAJIAS";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(@NotNull OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            Arena arena = Arena.getPlayerArenaMap().get(player);
            GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());

            if (playerData != null) {
                switch (params.toLowerCase()) {
                    case "win_game_ratio":
                        if (playerData.getGamesWon() == 0) return "0";
                        return String.valueOf(new DecimalFormat("0.00").format((float) playerData.getGamesWon() / playerData.getGamesPlayed()));
                    case "games_played": return String.valueOf(playerData.getGamesPlayed());
                    case "games_won": return String.valueOf(playerData.getGamesWon());
                    case "rockets_launched": return String.valueOf(playerData.getRocketsLaunched());
                    case "rockets_destroyed": return String.valueOf(playerData.getRocketsDestroyed());
                }
            }

            if (arena != null) {
                switch (params.toLowerCase()) {
                    case "arena_players_total": return String.valueOf(arena.getAllowedPlayersAmount());
                    case "arena_players": return String.valueOf(arena.getPlayers().size());
                    case "arena_players_red": return String.valueOf(arena.getAllPlayersInTeam(TeamColor.RED).size());
                    case "arena_players_green": return String.valueOf(arena.getAllPlayersInTeam(TeamColor.GREEN).size());
                    case "arena_name": return arena.getName();
                    case "arena_type": return arena.getType().toString();
                }

                Game game = arena.getGame();
                if (game != null) {
                    switch (params.toLowerCase()) {
                        case "game_start_countdown": return String.valueOf(game.getStartCountDownValue());
                        case "game_event_label": return game.getEventLabel();
                        case "game_time_before_event": return Utils.convertToTime(game.getTimeBeforeEvent());
                        case "game_winner_team":
                            if (game.getWinnerTeamColor() == null)
                                return config.getString("messages.draw-title");

                            if (arena.getTeamColor(player.getPlayer()) == game.getWinnerTeamColor()) return config.getString("messages.victory-title");
                            else return config.getString("messages.defeat-title");
                    }
                }
            }
        }
        return "Not found";
    }

    public static void registerHook() {
        new PAPIExpansion().register();
    }
}
