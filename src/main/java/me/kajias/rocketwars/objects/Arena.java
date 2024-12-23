package me.kajias.rocketwars.objects;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.managers.ArenaManager;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.ArenaType;
import me.kajias.rocketwars.objects.enums.SelectedColorType;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Arena
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();
    private static final HashMap<Player, Arena> playerArenaMap = new HashMap<>();

    private final String name;
    private final String worldName;
    private final String templateName;

    private Location spawnLocWaiting;
    private Location spawnLocRedTeam;
    private Location spawnLocGreenTeam;
    private ArenaState state;
    private ArenaType type;
    private int allowedPlayersAmount;
    private World world;
    private Game game;

    private final List<String> redTeam;
    private final List<String> greenTeam;
    private final List<Player> players;

    public Arena(String name, String worldName, String templateName) {
        this.name = name;
        this.worldName = worldName;
        this.templateName = templateName;
        spawnLocWaiting = null;
        spawnLocRedTeam = null;
        spawnLocGreenTeam = null;
        state = ArenaState.SETUP;
        type = ArenaType.SOLO;
        allowedPlayersAmount = 2;
        redTeam = new ArrayList<>();
        greenTeam = new ArrayList<>();
        players = new ArrayList<>();

        ArenaManager.copyWorldFolderToDestination(
                new File(RocketWars.INSTANCE.getServer().getWorldContainer().getAbsolutePath() + "/" + this.templateName),
                new File(RocketWars.INSTANCE.getServer().getWorldContainer().getAbsolutePath() + "/" + this.worldName)
        );
        createWorld(this.worldName);
    }

    public void resetWorld() {
        ArenaManager.deleteWorldFolder(this.worldName, true);
        ArenaManager.copyWorldFolderToDestination(
                new File(RocketWars.INSTANCE.getServer().getWorldContainer().getAbsolutePath() + "/" + this.templateName),
                new File(RocketWars.INSTANCE.getServer().getWorldContainer().getAbsolutePath() + "/" + this.worldName)
        );
        createWorld(this.worldName);
    }

    public void createWorld(String worldName) {
        WorldCreator worldCreator = new WorldCreator(worldName);
        world = Bukkit.getServer().createWorld(worldCreator);
        world.setAutoSave(false);
        world.setDifficulty(Difficulty.EASY);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doTileDrops", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("commandBlockOutput", "false");
        if (spawnLocWaiting != null) spawnLocWaiting.setWorld(world);
        if (spawnLocRedTeam != null) spawnLocRedTeam.setWorld(world);
        if (spawnLocGreenTeam != null) spawnLocGreenTeam.setWorld(world);
    }

    public void addPlayer(Player player) {
        int playersToJoin = 1;

        if (state == ArenaState.WAITING || state == ArenaState.STARTING) {
            if (players.size() + playersToJoin <= allowedPlayersAmount) {
                playerArenaMap.putIfAbsent(player, this);
                players.add(player);
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.setExp(0.0f);
                player.setLevel(0);
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                player.setFoodLevel(20);
                Utils.removePotionEffects(player);
                player.teleport(spawnLocWaiting, PlayerTeleportEvent.TeleportCause.PLUGIN);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(false);
                player.setFlying(false);

                GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());
                if (playerData != null) {
                    ItemStack boots = playerData.getBoughtArmorItem(playerData.getBoots()).clone();
                    ItemStack helmet = playerData.getBoughtArmorItem(playerData.getHelmet()).clone();

                    if (playerData.getSelectedColorTypeGlass() == SelectedColorType.RANDOM_SELECTION) {
                        try {
                            playerData.setGlassColor(String.valueOf(DyeColor.getByColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("glass"))))));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    if (playerData.getSelectedColorTypeBoots() == SelectedColorType.RANDOM_SELECTION) {
                        try {
                            LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                            bootsMeta.setColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("boots"))));
                            boots.setItemMeta(bootsMeta);
                        } catch (IllegalArgumentException ignored) {}
                    }
                    if (playerData.getSelectedColorTypeHelmet() == SelectedColorType.RANDOM_SELECTION) {
                        try {
                            LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
                            helmetMeta.setColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("helmet"))));
                            helmet.setItemMeta(helmetMeta);
                        } catch (IllegalArgumentException ignored) {}
                    }
                    player.getInventory().setBoots(boots);
                    player.getInventory().setHelmet(helmet);

                    if (playerData.getSelectedColorTypeBoots() == SelectedColorType.RAINBOW) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!players.contains(player)) cancel();
                                player.getInventory().setBoots(boots);
                                LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
                                try {
                                    bootsMeta.setColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("boots"))));
                                } catch (NumberFormatException ignored ) {}
                                boots.setItemMeta(bootsMeta);
                            }
                        }.runTaskTimer(RocketWars.INSTANCE, 0L, 5L);
                    }
                    if (playerData.getSelectedColorTypeHelmet() == SelectedColorType.RAINBOW) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!players.contains(player)) cancel();
                                player.getInventory().setHelmet(helmet);
                                LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
                                try {
                                    helmetMeta.setColor(Color.fromRGB(Integer.parseInt(playerData.getRandomBoughtColor("helmet"))));
                                } catch (NumberFormatException ignored ) {}
                                helmet.setItemMeta(helmetMeta);
                            }
                        }.runTaskTimer(RocketWars.INSTANCE, 0L, 5L);
                    }
                }

                ItemStack teamSelectItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.team-select-item.material")));
                ItemMeta teamSelectItemMeta = teamSelectItem.getItemMeta();
                teamSelectItemMeta.setDisplayName(Utils.colorize(config.getString("hot-bar-items.team-select-item.name")));
                teamSelectItem.setItemMeta(teamSelectItemMeta);
                player.getInventory().setItem(config.getInt("hot-bar-items.team-select-item.slot") - 1, teamSelectItem);

                ItemStack leaveGameItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.leave-game-item.material")));
                ItemMeta leaveGameItemMeta = leaveGameItem.getItemMeta();
                leaveGameItemMeta.setDisplayName(Utils.colorize(config.getString("hot-bar-items.leave-game-item.name")));
                leaveGameItem.setItemMeta(leaveGameItemMeta);
                player.getInventory().setItem(config.getInt("hot-bar-items.leave-game-item.slot") - 1, leaveGameItem);

                for(Player p : players) Utils.sendMessage(p, config.getString("messages.player-joined")
                        .replace("%player%", player.getName())
                        .replace("%current%", String.valueOf(players.size()))
                        .replace("%max%", String.valueOf(allowedPlayersAmount)));

                if (type != ArenaType.SOLO) {
                    if (players.size() >= allowedPlayersAmount - 1) {
                        if (game == null) {
                            game = new Game(this);
                            game.startCountDownTimer(config.getInt("game-config.long-start-countdown"));
                        } else if (players.size() == allowedPlayersAmount) game.shortenCountDownTimer(config.getInt("game-config.start-countdown"));
                    }
                } else {
                    if (players.size() == allowedPlayersAmount) {
                        game = new Game(this);
                        game.startCountDownTimer(config.getInt("game-config.start-countdown"));
                    }
                }
            } else {
                Utils.sendMessage(player, config.getString("messages.arena-is-full"));
            }
        }
    }

    public void removePlayer(Player player) {
        if (!players.contains(player)) return;

        playerArenaMap.remove(player);
        players.remove(player);
        removeFromTeam(player);
        Utils.teleportToLobby(player);

        for (Player p : players) {
            if (state != ArenaState.STARTED)
                Utils.sendMessage(p, config.getString("messages.player-left")
                        .replace("%player%", player.getName())
                        .replace("%current%", String.valueOf(players.size()))
                        .replace("%max%", String.valueOf(allowedPlayersAmount)));
            else Utils.sendMessage(p, config.getString("messages.player-left-in-game").replace("%player%", player.getName()));
        }

        if (game != null && state == ArenaState.STARTING && players.size() < allowedPlayersAmount) {
            if (type != ArenaType.SOLO) {
                if (players.size() < allowedPlayersAmount - 1) {
                    game.stopCountDownTimer();
                    game = null;
                } else if (game.getStartCountDownValue() > config.getInt("game-config.long-start-countdown") / 2) game.startCountDownTimer(config.getInt("game-config.long-start-countdown") / 2);
            } else {
                game.stopCountDownTimer();
                game = null;
            }
        }

        if (state == ArenaState.STARTED && players.size() <= 1) {
            if (!players.isEmpty()) players.forEach(p -> {Utils.sendMessage(p, config.getString("messages.no-players-left"));});
            restart();
        }
    }

    public boolean isInTeam(Player player) {
        try {
            return redTeam.contains(player.getName()) || greenTeam.contains(player.getName());
        } catch (NullPointerException e) {
            return false;
        }
    }

    public void addToTeam(TeamColor type, Player player) {
        if (isInTeam(player)) removeFromTeam(player);
        if (type == TeamColor.RED) {
            redTeam.add(player.getName());
            player.setPlayerListName(Utils.colorize("&c" + player.getName()));
            Utils.sendMessage(player, config.getString("messages.joined-team-red"));
        } else {
            greenTeam.add(player.getName());
            player.setPlayerListName(Utils.colorize("&a" + player.getName()));
            Utils.sendMessage(player, config.getString("messages.joined-team-green"));
        }
    }

    public void removeFromTeam(Player player) {
        if (!isInTeam(player)) return;
        redTeam.remove(player.getName());
        greenTeam.remove(player.getName());
        player.setPlayerListName(player.getName());
    }

    public List<String> getAllPlayersInTeam(TeamColor teamType) {
        List<String> teamMembers = new ArrayList<>();
        if (teamType == TeamColor.RED && redTeam != null) {
            teamMembers.addAll(redTeam);
        } else if (teamType == TeamColor.GREEN && greenTeam != null) {
            teamMembers.addAll(greenTeam);
        }
        return teamMembers;
    }

    public void restart() {
        stop();
        resetWorld();

        Bukkit.getScheduler().runTaskLater(RocketWars.INSTANCE, () -> {
            setState(ArenaState.WAITING);
        }, 3 * 20L);
    }
    
    public void stop() {
        if (!world.getPlayers().isEmpty()) {
            for (Player p : world.getPlayers()) {
                removePlayer(p);
            }
        }

        if (game != null) {
            game.destroy();
            game = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public ArenaState getState() {
        return state;
    }

    public ArenaType getType() {
        return type;
    }

    public World getWorld() {
        return world;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getAllowedPlayersAmount() {
        return allowedPlayersAmount;
    }

    public Location getSpawnLocWaiting() {
        return spawnLocWaiting;
    }

    public Location getSpawnLoc(TeamColor teamType) {
        if (teamType == TeamColor.RED) return spawnLocRedTeam;
        else return spawnLocGreenTeam;
    }

    public List<String> getTeam(TeamColor teamType) {
        return teamType == TeamColor.RED ? redTeam : greenTeam;
    }

    public TeamColor getTeamColor(Player player) {
        if (!isInTeam(player)) return null;
        return (redTeam.contains(player.getName()) ? TeamColor.RED : TeamColor.GREEN);
    }

    public boolean haveSetupProperly() {
        return this.spawnLocWaiting != null && this.spawnLocRedTeam != null && this.spawnLocGreenTeam != null;
    }

    public void setSpawnLocWaiting(Location location) {
        this.spawnLocWaiting = location;
    }

    public void setSpawnLoc(TeamColor teamType, Location spawnLocation) {
        if (teamType == TeamColor.RED) this.spawnLocRedTeam = spawnLocation;
        else this.spawnLocGreenTeam = spawnLocation;
    }

    public void setState(ArenaState arenaState) {
        this.state = arenaState;
    }

    public void setType(ArenaType arenaType) {
        this.type = arenaType;
        switch (arenaType) {
            case SOLO: allowedPlayersAmount = 2; break;
            case DUO: allowedPlayersAmount = 4; break;
            case TRIO: allowedPlayersAmount = 6; break;
            case QUADRO: allowedPlayersAmount = 8; break;
            case PENTA: allowedPlayersAmount = 10; break;
        }
    }

    public boolean setArenaTypeFromString(String str) {
        try {
            setType(ArenaType.valueOf(str));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static HashMap<Player, Arena> getPlayerArenaMap() {
        return playerArenaMap;
    }

    public Game getGame() {
        return game;
    }
}
