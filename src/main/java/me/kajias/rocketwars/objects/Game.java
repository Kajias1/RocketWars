package me.kajias.rocketwars.objects;

import com.github.shynixn.structureblocklib.api.enumeration.StructureRotation;
import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.misc.GameItems;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.misc.Structures;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.ArenaType;
import me.kajias.rocketwars.objects.enums.TeamColor;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftMinecartChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class Game
{
    private final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private final FileConfiguration config = RocketWars.INSTANCE.getConfig();
    private final int defaultStartCountDown = config.getInt("game-config.start-countdown");
    private final int gameDuration = config.getInt("game-config.game-duration");

    private final Arena arena;
    private Events events;

    private GameItems gameItems;

    private boolean isStarted;
    private int countDown;
    private int gameTime;
    private int timeBeforeEvent;
    private int timePassed;
    private int rocketsDropPeriod;
    private int regularItemsDropPeriod;
    private int gadgetsDropPeriod;
    private BukkitTask startCountDownTask;
    private BukkitTask gameTimerTask;
    private TeamColor winnerTeamColor;
    private String eventLabel;

    private final List<Player> playersWithDropBoost;
    private final List<GamePlayer> playersDataForGame;

    public Game(Arena arena) {
        this.arena = arena;
        events = null;
        gameItems = new GameItems(this);
        isStarted = false;
        gameTime = 0;
        timeBeforeEvent = 0;
        timePassed = 0;
        rocketsDropPeriod = config.getInt("game-config.game-drops.rockets.period");
        regularItemsDropPeriod = config.getInt("game-config.game-drops.regular-items.period");
        gadgetsDropPeriod = config.getInt("game-config.game-drops.gadgets.period");
        winnerTeamColor = null;
        eventLabel = null;
        playersWithDropBoost = new ArrayList<>();
        playersDataForGame = new ArrayList<>();
    }

    public void startCountDownTimer(int startCountDown) {
        countDown = startCountDown;
        arena.setState(ArenaState.STARTING);

        startCountDownTask = Bukkit.getScheduler().runTaskTimer(RocketWars.INSTANCE, () -> {
            countDown--;

            for(Player player : arena.getPlayers()) player.setLevel(countDown);

            if (countDown == startCountDown || countDown == 15 || countDown == 10 || countDown >= 1 && countDown <= 5) {
                for (Player player : arena.getPlayers()) {
                    Sounds.CLICK.play(player);
                    Utils.sendMessage(player, config.getString("messages.start-countdown").replace("%time%", String.valueOf(countDown)));
                }
            } else if(countDown == 0) {
                startGame();
            }
        }, 0L,  20L);
    }

    public void shortenCountDownTimer(int n) {
        if (countDown > n) countDown = n;
    }

    public void stopCountDownTimer() {
        arena.setState(ArenaState.WAITING);
        countDown = defaultStartCountDown;
        if (startCountDownTask != null) startCountDownTask.cancel();
        for (Player p : arena.getPlayers()) p.setLevel(0);
    }

    public void startGame() {
        if (!isStarted) {
            isStarted = true;
            arena.setState(ArenaState.STARTED);
            if (startCountDownTask != null) startCountDownTask.cancel();
            events = new Events(this);
            setTimeBeforeEvent(config.getInt("game-config.events.event1.time"));
            setNextEventLabel(config.getString("game-config.events.event1.label"));

            ItemStack respawnButton = new ItemStack(config.getInt("game-config.game-kit.respawn-item.material"), 1, (short) 1);
            ItemMeta respawnButtonItemMeta = respawnButton.getItemMeta();
            respawnButtonItemMeta.setDisplayName(Utils.colorize(config.getString("game-config.game-kit.respawn-item.name")));
            respawnButton.setItemMeta(respawnButtonItemMeta);

            ItemStack bow = new ItemStack(Material.getMaterial(config.getString("game-config.game-kit.bow-item.material")));
            ItemMeta bowMeta = bow.getItemMeta();
            bowMeta.setUnbreakable(true);
            bowMeta.setDisplayName(Utils.colorize(config.getString("game-config.game-kit.bow-item.name")));
            bowMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            bowMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            bowMeta.addEnchant(Enchantment.ARROW_FIRE, 1, false);
            bow.setItemMeta(bowMeta);

            arena.getPlayers().forEach(player -> {
                GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());
                GamePlayer playerDataForGame = new GamePlayer(player.getUniqueId());
                playerDataForGame.setPlayerName(player.getDisplayName());
                playersDataForGame.add(playerDataForGame);

                Utils.removePotionEffects(player);
                player.setMaxHealth(20);
                player.setHealth(player.getMaxHealth());
                player.setGameMode(GameMode.SURVIVAL);
                if (!arena.isInTeam(player)) {
                    if (arena.getTeam(TeamColor.GREEN).size() >= arena.getTeam(TeamColor.RED).size()) arena.addToTeam(TeamColor.RED, player);
                    else arena.addToTeam(TeamColor.GREEN, player);
                }

                ItemStack boots = player.getInventory().getBoots();
                ItemMeta bootsMeta = boots.getItemMeta();
                bootsMeta.setUnbreakable(true);
                bootsMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                boots.setItemMeta(bootsMeta);
                ItemStack helmet = player.getInventory().getHelmet();
                ItemMeta helmetMeta = helmet.getItemMeta();
                helmetMeta.setUnbreakable(true);
                helmetMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                helmet.setItemMeta(helmetMeta);

                player.getInventory().clear();
                player.teleport(arena.getSpawnLoc(arena.getTeamColor(player)), PlayerTeleportEvent.TeleportCause.PLUGIN);
                player.getInventory().setItem(config.getInt("game-config.game-kit.respawn-item.slot") - 1, respawnButton);
                player.getInventory().setItem(config.getInt("game-config.game-kit.bow-item.slot") - 1, bow);

                if (playerData != null) {
                    player.getInventory().setBoots(boots);
                    player.getInventory().setHelmet(helmet);
                    player.setAllowFlight(playerData.getBoughtAbilities().containsKey(Utils.stripColor(menusConfig.getString("menus.main-shop.bonus-items.double-jump.name"))));
                    Utils.applyArmorEffects(player);
                }
                player.setFlying(false);

                gameItems.giveRandomRocketDrop(player, "medium");

                Sounds.ORB_PICKUP.play(player);
                player.sendTitle(Utils.colorize(config.getString("messages.game-start-title")), "", 10, 70, 20);
                for (String str : config.getStringList("messages.game-start-info-message")) {
                    player.sendMessage(Utils.colorize(str));
                }
            });

            gameTime = gameDuration;
            gameTimerTask = Bukkit.getScheduler().runTaskTimer(RocketWars.INSTANCE, () -> {
                gameTime--;
                timeBeforeEvent--;
                timePassed = gameDuration - gameTime;

                events.checkForEvent();

                for (Player player : arena.getPlayers()) {
                    if (gameTime % rocketsDropPeriod == 0) {
                        if (new Random().nextInt(config.getInt("game-config.game-drops.rockets.chance")) / (playersWithDropBoost.contains(player) ? 2 : 1) == 0) {
                            if (playersWithDropBoost.contains(player)) Sounds.ITEM_PICKUP.play(player);
                            gameItems.giveRandomRocketDrop(player, null);
                        }
                    }
                    if (gameTime % regularItemsDropPeriod == 0) {
                        if (new Random().nextInt(config.getInt("game-config.game-drops.regular-items.chance")) / (playersWithDropBoost.contains(player) ? 2 : 1) == 0) {
                            if (playersWithDropBoost.contains(player)) Sounds.ITEM_PICKUP.play(player);
                            gameItems.giveRandomItemDrop(player, "regular-items", false);
                        }
                    }
                    if (gameTime % gadgetsDropPeriod == 0) {
                        if (new Random().nextInt(config.getInt("game-config.game-drops.gadgets.chance")) / (playersWithDropBoost.contains(player) ? 2 : 1) == 0) {
                            if (playersWithDropBoost.contains(player)) Sounds.ITEM_PICKUP.play(player);
                            gameItems.giveRandomItemDrop(player, "gadgets", false);
                        }
                    }
                }

                for (int x = -18; x < 72; ++x) {
                    Block greenBlock = this.arena.getWorld().getBlockAt(new Location(this.arena.getWorld(), x, 60, 15));
                    if (greenBlock.getType() == Material.AIR) {
                        greenBlock.setType(Material.STAINED_GLASS);
                        greenBlock.setData(DyeColor.LIME.getWoolData());
                    }

                    Block redBlock = this.arena.getWorld().getBlockAt(new Location(this.arena.getWorld(), x, 60, -105));
                    if (redBlock.getType() == Material.AIR) {
                        redBlock.setType(Material.STAINED_GLASS);
                        redBlock.setData(DyeColor.RED.getWoolData());
                    }
                }

                if (gameTime % config.getInt("game-config.bonus-cube.spawn-period") == 0) {
                    if (new Random().nextInt(config.getInt("game-config.bonus-cube.chance") + 1) == 0) {
                        spawnBonusCube();
                    }
                }

                if (gameTime % config.getInt("game-config.ship.spawn-period") == 0) {
                    if (new Random().nextInt(config.getInt("game-config.ship.chance") + 1) == 0) {
                        spawnShipWithLoot();
                    }
                }

                if (gameTime <= 0) {
                    endGame(null);
                }
            }, 0L, 20L);
        }
    }

    public void destroy() {
        isStarted = false;
        if (startCountDownTask != null) startCountDownTask.cancel();
        if (gameTimerTask != null) gameTimerTask.cancel();
    }

    public void endGame(TeamColor winnerTeamColor) {
        this.winnerTeamColor = winnerTeamColor;

        arena.setState(ArenaState.ENDING);
        if (gameTimerTask != null) gameTimerTask.cancel();

        String subtitle = Utils.colorize(config.getString("messages.game-end-subtitle.draw"));
        if (winnerTeamColor == TeamColor.RED) {
            subtitle = Utils.colorize(config.getString("messages.game-end-subtitle.red-team-victory"));
        } else if (winnerTeamColor == TeamColor.GREEN) {
            subtitle = Utils.colorize(config.getString("messages.game-end-subtitle.green-team-victory"));
        }

        ItemStack newGameItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.new-game-item.material")));
        ItemMeta newGameItemMeta = newGameItem.getItemMeta();
        newGameItemMeta.setDisplayName(Utils.colorize(
                config.getString("hot-bar-items.new-game-item.name")));
        newGameItem.setItemMeta(newGameItemMeta);

        ItemStack leaveGameItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.leave-game-item.material")));
        ItemMeta leaveGameItemMeta = leaveGameItem.getItemMeta();
        leaveGameItemMeta.setDisplayName(Utils.colorize(
                config.getString("hot-bar-items.leave-game-item.name")));
        leaveGameItem.setItemMeta(leaveGameItemMeta);

        List<GamePlayer> attackerPlayersTop = null;
        List<GamePlayer> defenderPlayersTop = null;

        List<String> topMessage = new ArrayList<>();
        if (arena.getPlayers().size() >= 3) {
            attackerPlayersTop = playersDataForGame.stream().sorted(Comparator.comparing(GamePlayer::getRocketsLaunched)).collect(Collectors.toList());
            defenderPlayersTop = playersDataForGame.stream().sorted(Comparator.comparing(GamePlayer::getRocketsDestroyed)).collect(Collectors.toList());
            Collections.reverse(attackerPlayersTop);
            Collections.reverse(defenderPlayersTop);

            int nicknameMaxLength = attackerPlayersTop.get(0).getPlayerName().length();
            for (GamePlayer x : attackerPlayersTop) {
                if (nicknameMaxLength <= x.getPlayerName().length())
                    nicknameMaxLength = x.getPlayerName().length();
            }
            for (GamePlayer x : defenderPlayersTop) {
                if (nicknameMaxLength <= x.getPlayerName().length())
                    nicknameMaxLength = x.getPlayerName().length();
            }

            for (String str : config.getStringList("messages.game-end-top-players")) {
                topMessage.add(str
                        .replace("%top_1_attacker%", Utils.getColorCode(attackerPlayersTop.get(0).getPlayerName())
                                + attackerPlayersTop.get(0).getPlayerName() + new String(new char[nicknameMaxLength - attackerPlayersTop.get(0).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_1_attacker_rockets_launched%", String.valueOf(attackerPlayersTop.get(0).getRocketsLaunched()))

                        .replace("%top_2_attacker%", Utils.getColorCode(attackerPlayersTop.get(1).getPlayerName())
                                + attackerPlayersTop.get(1).getPlayerName() + new String(new char[nicknameMaxLength - attackerPlayersTop.get(1).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_2_attacker_rockets_launched%", String.valueOf(attackerPlayersTop.get(1).getRocketsLaunched()))

                        .replace("%top_3_attacker%", Utils.getColorCode(attackerPlayersTop.get(2).getPlayerName())
                                + attackerPlayersTop.get(2).getPlayerName() + new String(new char[nicknameMaxLength - attackerPlayersTop.get(2).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_3_attacker_rockets_launched%", String.valueOf(attackerPlayersTop.get(2).getRocketsLaunched()))

                        .replace("%top_1_defender%", Utils.getColorCode(defenderPlayersTop.get(0).getPlayerName())
                                + defenderPlayersTop.get(0).getPlayerName() + new String(new char[nicknameMaxLength - defenderPlayersTop.get(0).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_1_defender_rockets_destroyed%", String.valueOf(defenderPlayersTop.get(0).getRocketsDestroyed()))

                        .replace("%top_2_defender%", Utils.getColorCode(defenderPlayersTop.get(1).getPlayerName())
                                + defenderPlayersTop.get(1).getPlayerName() + new String(new char[nicknameMaxLength - defenderPlayersTop.get(1).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_2_defender_rockets_destroyed%", String.valueOf(defenderPlayersTop.get(1).getRocketsDestroyed()))

                        .replace("%top_3_defender%", Utils.getColorCode(defenderPlayersTop.get(2).getPlayerName())
                                + defenderPlayersTop.get(2).getPlayerName() + new String(new char[nicknameMaxLength - defenderPlayersTop.get(2).getPlayerName().length()]).replace("\0", " "))
                        .replace("%top_3_defender_rockets_destroyed%", String.valueOf(defenderPlayersTop.get(2).getRocketsDestroyed()))
                );
            }
        }

        for (Player player : arena.getPlayers()) {
            GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());
            GamePlayer playerDataForGame = playersDataForGame.stream().filter(x -> x.getUniqueId().equals(player.getUniqueId())).findAny().orElse(new GamePlayer(player.getUniqueId()));
            playerData.setRocketsLaunched(playerData.getRocketsLaunched() + playerDataForGame.getRocketsLaunched());
            playerData.setRocketsDestroyed(playerData.getRocketsDestroyed() + playerDataForGame.getRocketsDestroyed());
            playerData.setGamesPlayed(playerData.getGamesPlayed() + 1);

            if (!topMessage.isEmpty()) {
                topMessage.forEach(line -> player.sendMessage(Utils.colorize(line)));
            }

            player.getInventory().clear();
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
            player.setHealth(20);
            player.setLevel(0);
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            player.setFlying(true);
            Utils.removePotionEffects(player);

            player.getInventory().setItem(config.getInt("hot-bar-items.new-game-item.slot") - 1, newGameItem);
            player.getInventory().setItem(config.getInt("hot-bar-items.leave-game-item.slot") - 1, leaveGameItem);

            int bonusReward = 0;
            bonusReward += (playerDataForGame.getRocketsDestroyed() / config.getInt("game-config.bonus-reward-for-victory." + arena.getType() + ".rockets-destroyed.amount"))
                * config.getInt("game-config.bonus-reward-for-victory." + arena.getType() + ".rockets-destroyed.reward");;
            bonusReward += (playerDataForGame.getRocketsLaunched() / config.getInt("game-config.bonus-reward-for-victory." + arena.getType() + ".rockets-launched.amount"))
                * config.getInt("game-config.bonus-reward-for-victory." + arena.getType() + ".rockets-destroyed.reward");;

            String title = Utils.colorize(config.getString("messages.draw-title"));
            if (winnerTeamColor != null) {
                if (arena.getTeamColor(player) == winnerTeamColor) bonusReward += config.getInt("game-config.bonus-reward-for-victory." + arena.getType() +  ".reward");

                if (attackerPlayersTop != null && attackerPlayersTop.size() >= 3) {
                    int bonusRewardForPlace = 0;
                    for (int i = 0; i < 3; i++) if (player.getName().equals(attackerPlayersTop.get(2 - i).getPlayerName()))
                        bonusRewardForPlace = config.getIntegerList("game-config.bonus-reward-for-victory." + arena.getType() + ".reward-for-place").get(i);
                    bonusReward += bonusRewardForPlace;
                }
                if (defenderPlayersTop != null && defenderPlayersTop.size() >= 3) {
                    int bonusRewardForPlace = 0;
                    for (int i = 0; i < 3; i++) if (player.getName().equals(defenderPlayersTop.get(2 - i).getPlayerName()))
                        bonusRewardForPlace = config.getIntegerList("game-config.bonus-reward-for-victory." + arena.getType() + ".reward-for-place").get(i);
                    bonusReward += bonusRewardForPlace;
                }

                if (arena.getTeamColor(player) == winnerTeamColor) {
                    title = Utils.colorize(config.getString("messages.victory-title"));
                    launchFireWork(player);
                    playerData.setGamesWon(playerData.getGamesWon() + 1);
                } else title = Utils.colorize(config.getString("messages.defeat-title"));
            }
            if (bonusReward > 0) {
                Utils.sendMessage(player, config.getString("messages.reward-message").replace("%bonus%", String.valueOf(bonusReward)));
                RocketWars.getEconomy().depositPlayer(player, bonusReward);
            }
            player.sendTitle(title, subtitle, 10, 70, 20);
        }

        if (isStarted) Bukkit.getScheduler().scheduleSyncDelayedTask(RocketWars.INSTANCE, () -> {
            isStarted = false;
            arena.restart();
        }, 160L);
    }

    public void teleportToInGameSpawnPoint(Player player) {
        player.setFallDistance(0.0f);
        player.teleport(arena.getSpawnLoc(arena.getTeamColor(player)), PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    public void launchFireWork(Player player) {
        final Firework f = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fm = f.getFireworkMeta();
        fm.addEffect(FireworkEffect.builder()
                .flicker(true)
                .trail(true)
                .with(FireworkEffect.Type.STAR)
                .with(FireworkEffect.Type.BALL)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(Color.AQUA)
                .withColor(Color.YELLOW)
                .withColor(Color.RED)
                .withColor(Color.WHITE)
                .build());
        fm.setPower(0);
        f.setFireworkMeta(fm);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RocketWars.INSTANCE, () -> Sounds.FIREWORK_LARGE_BLAST.play(player), 25L);
    }

    public int getTimePassed() {
        return timePassed;
    }

    public int getStartCountDownValue() {
        return countDown;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public int getTimeBeforeEvent() {
        return timeBeforeEvent;
    }

    public TeamColor getWinnerTeamColor() {
        return winnerTeamColor;
    }

    public List<Player> getPlayersWithDropBoost() {
        return playersWithDropBoost;
    }

    public Arena getArena() {
        return arena;
    }

    public void multiplyItemDropsPeriod(double dropPeriod) {
        rocketsDropPeriod *= dropPeriod;
        regularItemsDropPeriod *= dropPeriod;
        gadgetsDropPeriod *= dropPeriod;
    }

    public void setTimeBeforeEvent(int timeBeforeEvent) {
        this.timeBeforeEvent = timeBeforeEvent;
    }

    public void setNextEventLabel(String eventLabel) {
        this.eventLabel = Utils.colorize(eventLabel);
    }

    private void spawnShipWithLoot() {
        int shipX = new Random().nextBoolean() ? -50 : 90;
        int shipY = 33 + new Random().nextInt(20);
        int shipZ = -80 + new Random().nextInt(65);


        Location shipSpawnLocation = new Location(
                this.getArena().getWorld(), shipX, shipY, shipZ
        );

        Structures.spawnShipStructure(shipSpawnLocation, config.getString("game-config.ship.structure-name"), shipX == -50 ? StructureRotation.ROTATION_270 : StructureRotation.ROTATION_90);

        Bukkit.getScheduler().runTaskLater(RocketWars.INSTANCE, () -> {
            for (Entity entity : arena.getWorld().getNearbyEntities(shipSpawnLocation, 30.0, 30.0, 30.0).stream().filter(x -> x.getType() == EntityType.MINECART_CHEST).collect(Collectors.toList())) {
                CraftMinecartChest minecartChest = (CraftMinecartChest) entity;

                if (minecartChest != null && minecartChest.getCustomName() == null) {
                    minecartChest.setCustomName(Utils.colorize(config.getString("game-config.ship.hologram")));
                    minecartChest.setCustomNameVisible(true);
                }
            }
        }, 20L);

        for (Player p : this.arena.getWorld().getPlayers()) {
            p.sendTitle(Utils.colorize(config.getString("game-config.ship.appear-message.title")),Utils.colorize(config.getString("game-config.ship.appear-message.subtitle")), 10, 80, 20);
            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }
    }

    private void spawnBonusCube() {
        int bonusX = -10 + new Random().nextInt(65);
        int bonusY = 30 + new Random().nextInt(25);
        int bonusZ = -76 + new Random().nextInt(54);

        Location bonusSpawnLocation = new Location(arena.getWorld(), bonusX, bonusY, bonusZ);

        Structures.spawnBonusCubeStructure(bonusSpawnLocation, config.getString("game-config.bonus-cube.structure-name"));

        ArmorStand as = bonusSpawnLocation.getWorld().spawn(new Location(
                bonusSpawnLocation.getWorld(), bonusX + 2.5, bonusY + 1.8, bonusZ + 2.5
        ), ArmorStand.class);

        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setCustomName(Utils.colorize(config.getString("game-config.bonus-cube.hologram")));
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setSmall(true);

        for (Player player : this.arena.getWorld().getPlayers()) {
            player.sendTitle(Utils.colorize(config.getString("game-config.bonus-cube.appear-message.title")),
                    Utils.colorize(config.getString("game-config.bonus-cube.appear-message.subtitle")), 10, 80, 10);
            Sounds.ORB_PICKUP.play(player);
        }
    }

    public List<GamePlayer> getPlayersDataForGame() {
        return playersDataForGame;
    }

    public GameItems getGameItems() {
        return gameItems;
    }
}
