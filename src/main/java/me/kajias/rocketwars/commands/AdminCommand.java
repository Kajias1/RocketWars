package me.kajias.rocketwars.commands;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.ArenaConfiguration;
import me.kajias.rocketwars.managers.ArenaManager;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.ArenaType;
import me.kajias.rocketwars.objects.enums.TeamColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AdminCommand implements CommandExecutor
{
    public static final Map<Player, Arena> setupMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = RocketWars.INSTANCE.getConfig();
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();

            if (player.hasPermission("rw.admin")) {
                if (args.length == 0) {
                    showHelp(player);
                } else {
                    switch (args[0].toLowerCase()) {
                        case "adminhelp":
                            showHelp(player);
                            break;
                        case "setlobby":
                            config.set("lobby-spawn-point.world", player.getWorld().getName());
                            config.set("lobby-spawn-point.x", player.getLocation().getX());
                            config.set("lobby-spawn-point.y", player.getLocation().getY());
                            config.set("lobby-spawn-point.z", player.getLocation().getZ());
                            config.set("lobby-spawn-point.yaw", player.getLocation().getYaw());
                            RocketWars.INSTANCE.saveConfig();
                            RocketWars.lobbyLocation = player.getLocation();
                            Utils.sendMessage(player, config.getString("messages.lobby-was-set"));
                            break;
                        case "create":
                            Arena createdArena;
                            if (args.length == 4) {
                                if (ArenaManager.getArenaByName(args[1]) == null) {
                                    if (new File(args[3]).listFiles() != null) {
                                        createdArena = new Arena(args[1], args[2], args[3]);
                                        ArenaConfiguration.addArena(createdArena);
                                        Utils.sendMessage(player, config.getString("messages.arena-created").replace("%arena_name%", args[1]));
                                        player.teleport(new Location(createdArena.getWorld(), 0, 60, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                        player.setGameMode(GameMode.CREATIVE);
                                        setupMap.put(player, createdArena);
                                        showSetupStatus(createdArena, player);
                                    } else Utils.sendMessage(player, config.getString("messages.template-world-not-found").replace("%world_name%", args[2]));
                                } else Utils.sendMessage(player, config.getString("messages.arena-exists").replace("%arena_name%", args[1]));
                            } else showHelp(player);
                            break;
                        case "setspawn":
                            if (args.length == 1) {
                                Utils.sendMessage(player, config.getString("messages.team-color-not-defined"));
                            } else {
                                if (setupMap.containsKey(player)) {
                                    Arena arena = setupMap.get(player);
                                    if (arena.getWorldName().equals(player.getWorld().getName())) {
                                        boolean success = false;
                                        switch (args[1].toLowerCase()) {
                                            case "red":
                                                arena.setSpawnLoc(TeamColor.RED, player.getLocation());
                                                success = true;
                                                break;
                                            case "green":
                                                arena.setSpawnLoc(TeamColor.GREEN, player.getLocation());
                                                success = true;
                                                break;
                                            case "waiting":
                                                arena.setSpawnLocWaiting(player.getLocation());
                                                success = true;
                                                break;
                                        }
                                        if (success) {
                                            Utils.sendMessage(player, config.getString("messages.spawn-set"));
                                            showSetupStatus(arena, player);
                                        }
                                    } else Utils.sendMessage(player, config.getString("messages.not-in-arena-world"));
                                } else Utils.sendMessage(player, config.getString("messages.arena-not-in-setup"));
                            }
                            break;
                        case "setarenatype":
                            if (args.length == 2) {
                                if (setupMap.containsKey(player)) {
                                    Arena arena = setupMap.get(player);
                                    if (arena.setArenaTypeFromString(args[1])) {
                                        showSetupStatus(arena, player);
                                    } else {
                                        Utils.sendMessage(player, config.getString("messages.arena-type-unknown"));
                                    }
                                } else Utils.sendMessage(player, config.getString("messages.arena-not-in-setup"));
                            } else showHelp(player);
                            break;
                        case "disable":
                            if (args.length == 2) {
                                if (ArenaManager.getArenaByName(args[1]) != null) {
                                    Arena arena = ArenaManager.getArenaByName(args[1]);
                                    if (arena.getState() != ArenaState.DISABLED) {
                                        ArenaManager.disableArena(arena);
                                        Utils.sendMessage(player, config.getString("messages.arena-has-been-disabled").replace("%name%", args[1]));
                                    } else Utils.sendMessage(player, config.getString("messages.failed-to-disable-arena").replace("%name%", args[1]));
                                } else Utils.sendMessage(player, config.getString("messages.arena-does-not-exist").replace("%name%%", args[1]));
                            } else Utils.sendMessage(player, config.getString("messages.arena-name-is-not-specified"));
                            break;
                        case "enable":
                            if (args.length == 2) {
                                if (ArenaManager.getArenaByName(args[1]) != null) {
                                    Arena arena = ArenaManager.getArenaByName(args[1]);
                                    if (arena.getState() == ArenaState.DISABLED) {
                                        ArenaManager.enableArena(arena);
                                        Utils.sendMessage(player, config.getString("messages.arena-has-been-enabled").replace("%name%", args[1]));
                                    } else Utils.sendMessage(player, config.getString("messages.arena-is-enabled").replace("%name%", args[1]));
                                } else Utils.sendMessage(player, config.getString("messages.arena-does-not-exist").replace("%name%", args[1]));
                            } else Utils.sendMessage(player, config.getString("messages.arena-name-is-not-specified"));
                            break;
                        case "setup":
                            if (args.length == 2) {
                                if (ArenaManager.getArenaByName(args[1]) != null) {
                                    Arena modifiableArena = ArenaManager.getArenaByName(args[1]);
                                    ArenaManager.disableArena(modifiableArena);
                                    if (!setupMap.containsKey(player)) {
                                        Bukkit.getScheduler().runTaskLater(RocketWars.INSTANCE, () -> {
                                            Utils.sendMessage(player, config.getString("messages.entering-setup"));
                                            modifiableArena.setState(ArenaState.SETUP);
                                            player.teleport(new Location(modifiableArena.getWorld(), 0, 60, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                                            player.getInventory().clear();
                                            player.getInventory().setArmorContents(null);
                                            player.setGameMode(GameMode.CREATIVE);
                                            setupMap.put(player, modifiableArena);
                                            showSetupStatus(modifiableArena, player);
                                        }, 40L);
                                    } else Utils.sendMessage(player, config.getString("Strings.arena-is-already-in-setup"));
                                } else Utils.sendMessage(player, config.getString("messages.arena-does-not-exist").replace("%arena_name%", args[1]));
                            } else Utils.sendMessage(player, config.getString("messages.arena-name-is-not-specified"));
                            break;
                        case "finish":
                            if (setupMap.containsKey(player)) {
                                if (setupMap.get(player).haveSetupProperly()) {
                                    Utils.sendMessage(player, config.getString("messages.exiting-setup"));
                                    setupMap.get(player).getWorld().save();
                                    setupMap.get(player).setState(ArenaState.WAITING);
                                    ArenaConfiguration.saveArena(setupMap.get(player));
                                    ArenaManager.loadArena(setupMap.get(player));
                                    setupMap.remove(player);
                                    Utils.teleportToLobby(player);
                                } else Utils.sendMessage(player, config.getString("messages.failed-to-finish-setup"));
                            } else Utils.sendMessage(player, config.getString("messages.arena-not-in-setup"));
                            break;
                        case "list":
                            listArenas(player);
                            break;
                        default:
                            Utils.sendMessage(player, config.getString("messages.unknown-subcommand"));
                            break;
                    }
                }
                return true;
            } else Utils.sendMessage(player, config.getString("messages.no-permission"));
            return false;
        }
        Bukkit.getLogger().warning(config.getString("messages.log.must-be-player-to-execute-command"));
        return false;
    }

    private void showHelp(Player player) {
        FileConfiguration config = RocketWars.INSTANCE.getConfig();
        for (String s : config.getStringList("messages.admin-help")) {
            player.sendMessage(Utils.colorize(s));
        }
    }

    private void showSetupStatus(Arena arena, Player player) {
        FileConfiguration config = RocketWars.INSTANCE.getConfig();
        TextComponent setupStatusTextComponent = new TextComponent();
        TextComponent spawnPointWaitingTextComponent = new TextComponent();
        TextComponent spawnPointRedTextComponent = new TextComponent();
        TextComponent spawnPointGreenTextComponent = new TextComponent();
        TextComponent arenaTypeTextComponent = new TextComponent();
        TextComponent arenaTypeSOLO = new TextComponent();
        TextComponent arenaTypeDUO = new TextComponent();
        TextComponent arenaTypeTRIO = new TextComponent();
        TextComponent arenaTypeQUADRO = new TextComponent();
        TextComponent arenaTypePENTA = new TextComponent();
        TextComponent finishSetupTextComponent = new TextComponent();

        TextComponent lineTextComponent = new TextComponent(Utils.colorize("&6&m------------------------------------------------"));

        setupStatusTextComponent.setText(Utils.colorize(config.getString("messages.setup-status")));

        StringBuilder builder = new StringBuilder();
        if(arena.getSpawnLocWaiting() != null) {
            builder.append("&7[&a✔&7] ").append(config.getString("messages.spawn-point-waiting")).append(" ")
                    .append(config.getString("messages.xyz")
                            .replace("%x%", String.valueOf(arena.getSpawnLocWaiting().getBlockX()))
                            .replace("%y%", String.valueOf(arena.getSpawnLocWaiting().getBlockY()))
                            .replace("%z%", String.valueOf(arena.getSpawnLocWaiting().getBlockZ())));
        } else builder.append("&7[&c✕&7] ").append(config.getString("messages.spawn-point-waiting"));

        spawnPointWaitingTextComponent.setText(Utils.colorize(builder.toString()));
        spawnPointWaitingTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-spawn-point-hover-message"))).create()));
        spawnPointWaitingTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setspawn waiting"));

        builder = new StringBuilder();
        if(arena.getSpawnLoc(TeamColor.RED) != null) {
            builder.append("&7[&a✔&7] ").append(config.getString("messages.spawn-point-red")).append(" ")
                    .append(config.getString("messages.xyz")
                            .replace("%x%", String.valueOf(arena.getSpawnLoc(TeamColor.RED).getBlockX()))
                            .replace("%y%", String.valueOf(arena.getSpawnLoc(TeamColor.RED).getBlockY()))
                            .replace("%z%", String.valueOf(arena.getSpawnLoc(TeamColor.RED).getBlockZ())));
        } else builder.append("&7[&c✕&7] ").append(config.getString("messages.spawn-point-red"));

        spawnPointRedTextComponent.setText(Utils.colorize(builder.toString()));
        spawnPointRedTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-spawn-point-hover-message"))).create()));
        spawnPointRedTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setspawn red"));

        builder = new StringBuilder();
        if(arena.getSpawnLoc(TeamColor.GREEN) != null) {
            builder.append("&7[&a✔&7] ").append(config.getString("messages.spawn-point-green")).append(" ")
                    .append(config.getString("messages.xyz")
                            .replace("%x%", String.valueOf(arena.getSpawnLoc(TeamColor.GREEN).getBlockX()))
                            .replace("%y%", String.valueOf(arena.getSpawnLoc(TeamColor.GREEN).getBlockY()))
                            .replace("%z%", String.valueOf(arena.getSpawnLoc(TeamColor.GREEN).getBlockZ())));
        } else builder.append("&7[&c✕&7] ").append(config.getString("messages.spawn-point-green"));

        spawnPointGreenTextComponent.setText(Utils.colorize(builder.toString()));
        spawnPointGreenTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-spawn-point-hover-message"))).create()));
        spawnPointGreenTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setspawn green"));

        arenaTypeTextComponent.setText(Utils.colorize(config.getString("messages.arena-type-choice")));

        arenaTypeSOLO.setText(Utils.colorize(arena.getType() == ArenaType.SOLO ? "&a     1  VS  1":"&7     1  VS  1"));
        arenaTypeSOLO.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-hover-message"))).create()));
        arenaTypeSOLO.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setarenatype SOLO"));

        arenaTypeDUO.setText(Utils.colorize(arena.getType() == ArenaType.DUO ? "&a     2  VS  2":"&7     2  VS  2"));
        arenaTypeDUO.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-hover-message"))).create()));
        arenaTypeDUO.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setarenatype DUO"));

        arenaTypeTRIO.setText(Utils.colorize(arena.getType() == ArenaType.TRIO ? "&a     3  VS  3":"&7     3  VS  3"));
        arenaTypeTRIO.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-hover-message"))).create()));
        arenaTypeTRIO.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setarenatype TRIO"));

        arenaTypeQUADRO.setText(Utils.colorize(arena.getType() == ArenaType.QUADRO ? "&a     4  VS  4":"&7     4  VS  4"));
        arenaTypeQUADRO.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-hover-message"))).create()));
        arenaTypeQUADRO.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setarenatype QUADRO"));

        arenaTypePENTA.setText(Utils.colorize(arena.getType() == ArenaType.PENTA ? "&a     5  VS  5":"&7     5  VS  5"));
        arenaTypePENTA.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.modify-hover-message"))).create()));
        arenaTypePENTA.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw setarenatype PENTA"));

        finishSetupTextComponent.setText(Utils.colorize(config.getString("messages.finish-setup-button-text")));
        finishSetupTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Utils.colorize(config.getString("messages.finish-setup-button-hover-message"))).create()));
        finishSetupTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rw finish"));

        player.spigot().sendMessage(lineTextComponent);
        player.spigot().sendMessage(setupStatusTextComponent);
        player.spigot().sendMessage(spawnPointWaitingTextComponent);
        player.spigot().sendMessage(spawnPointRedTextComponent);
        player.spigot().sendMessage(spawnPointGreenTextComponent);
        player.spigot().sendMessage(arenaTypeTextComponent);
        player.spigot().sendMessage(arenaTypeSOLO);
        player.spigot().sendMessage(arenaTypeDUO);
        player.spigot().sendMessage(arenaTypeTRIO);
        player.spigot().sendMessage(arenaTypeQUADRO);
        player.spigot().sendMessage(arenaTypePENTA);
        player.spigot().sendMessage(finishSetupTextComponent);
        player.spigot().sendMessage(lineTextComponent);
    }

    private void listArenas(Player player) {
        FileConfiguration config = RocketWars.INSTANCE.getConfig();
        if (ArenaManager.getLoadedArenas().isEmpty()) {
            Utils.sendMessage(player, config.getString("messages.arena-list-empty"));
            return;
        }
        for (String s : config.getStringList("messages.arena-list")) {
            player.sendMessage(Utils.colorize(s));
        }
        for(Arena arena : ArenaManager.getLoadedArenas()) {
            String state;
            switch (arena.getState()) {
                case SETUP:
                    state = "&e●";
                    break;
                case DISABLED:
                    state = "&c●";
                    break;
                case WAITING:
                case STARTING:
                    state = "&a●";
                    break;
                case ENDING:
                case STARTED:
                    state = "&8●";
                    break;
                default:
                    state = "";
                    break;
            }
            player.sendMessage(Utils.colorize(
                    config.getString("messages.arena-list-format")
                            .replace("%name%", arena.getName())
                            .replace("%world%", arena.getWorldName())
                            .replace("%state%", state)));
        }
    }
}
