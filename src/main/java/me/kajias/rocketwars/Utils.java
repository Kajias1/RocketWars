package me.kajias.rocketwars;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.misc.Structures;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import me.kajias.rocketwars.objects.enums.TeamColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Pattern;

public class Utils
{
    private static final FileConfiguration menusConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '&' + "[0-9A-FK-OR]");

    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static List<String> colorize(List<String> stringList) {
        List<String> result = new ArrayList<>();
        for (String str : stringList) {
            result.add(Utils.colorize(str));
        }
        return result;
    }

    public static String stripColor(String input) {
        return ChatColor.stripColor(input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll(""));
    }

    public static void sendMessage(Player player, String message) {
        player.sendMessage(colorize(RocketWars.INSTANCE.getConfig().getString("messages.prefix") + message));
    }

    public static String getColorCode(Player player) {
        Arena arena = Arena.getPlayerArenaMap().get(player);
        if (arena != null && arena.isInTeam(player))
            return config.getString("messages.team-color." + arena.getTeamColor(player));
        return "&f";
    }

    public static String getColorCode(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            Arena arena = Arena.getPlayerArenaMap().get(player);
            if (arena != null && arena.isInTeam(player))
                return config.getString("messages.team-color." + arena.getTeamColor(player));
        }
        return "&f";
    }

    public static boolean isHotBarItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;

        List<String> itemsNameList = new ArrayList<>();
        for (String itemName : config.getConfigurationSection("hot-bar-items").getKeys(false))
            itemsNameList.add(stripColor(config.getString("hot-bar-items." + itemName + ".name")));
        for (String itemName : config.getConfigurationSection("game-config.game-kit").getKeys(false))
            itemsNameList.add(stripColor(config.getString("game-config.game-kit." + itemName + ".name")));

        return itemsNameList.contains(stripColor(item.getItemMeta().getDisplayName()));
    }

    public static boolean isInGameItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;

        List<String> itemsNameList = new ArrayList<>();
        for (String itemName : config.getConfigurationSection("game-config.game-drops.regular-items.items").getKeys(false))
            itemsNameList.add(stripColor(config.getString("game-config.game-drops.regular-items.items." + itemName + ".name")));
        for (String itemName : config.getConfigurationSection("game-config.game-drops.gadgets.items").getKeys(false))
            itemsNameList.add(stripColor(config.getString("game-config.game-drops.gadgets.items." + itemName + ".name")));
        for (String itemName : config.getConfigurationSection("game-config.game-drops.bonus-items.items.").getKeys(false))
            itemsNameList.add(stripColor(config.getString("game-config.game-drops.bonus-items.items." + itemName + ".name")));

        return itemsNameList.contains(stripColor(item.getItemMeta().getDisplayName()));
    }

    public static void teleportToLobby(Player player) {
        if (Arena.getPlayerArenaMap().containsKey(player)) Arena.getPlayerArenaMap().get(player).removePlayer(player);

        Utils.removePotionEffects(player);
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getInventory().clear();
        player.setExp(0.0f);
        player.setLevel(0);
        player.setMaxHealth(20.0f);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.getInventory().setArmorContents(null);

        ItemStack fastJoinItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.fast-join-item.material")));
        ItemMeta fastJoinItemMeta = fastJoinItem.getItemMeta();
        fastJoinItemMeta.setDisplayName(colorize(config.getString("hot-bar-items.fast-join-item.name")));
        fastJoinItem.setItemMeta(fastJoinItemMeta);
        player.getInventory().setItem(config.getInt("hot-bar-items.fast-join-item.slot") - 1, fastJoinItem);

        ItemStack selectArenaItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.select-arena-item.material")));
        ItemMeta selectArenaItemMeta = selectArenaItem.getItemMeta();
        selectArenaItemMeta.setDisplayName(Utils.colorize(config.getString("hot-bar-items.select-arena-item.name")));
        selectArenaItem.setItemMeta(selectArenaItemMeta);
        player.getInventory().setItem(config.getInt("hot-bar-items.select-arena-item.slot") - 1, selectArenaItem);

        ItemStack shopItem = new ItemStack(Material.getMaterial(config.getString("hot-bar-items.shop-item.material")));
        ItemMeta shopItemMeta = shopItem.getItemMeta();
        shopItemMeta.setDisplayName(colorize(
                config.getString("hot-bar-items.shop-item.name")));
        shopItem.setItemMeta(shopItemMeta);
        player.getInventory().setItem(config.getInt("hot-bar-items.shop-item.slot") - 1, shopItem);

        player.setFallDistance(0.0f);
        if (RocketWars.lobbyLocation != null) {
            player.teleport(RocketWars.lobbyLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
        } else {
            sendMessage(player, config.getString("messages.lobby-was-not-set"));
            player.teleport(new Location(Bukkit.getWorld("world"), 0, Bukkit.getWorld("world").getHighestBlockYAt(0, 0), 0));
        }
    }

    public static void removePotionEffects(Player p) {
        for(PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
    }

    // This was deliberately hardcoded pls don't change it
    public static void spawnRocket(Player player, Location location, String structureName) {
        Arena arena = Arena.getPlayerArenaMap().get(player);
        if (arena.getGame() != null) {
            structureName = Utils.stripColor(structureName);

            if (location.getZ() > 15 || location.getZ() < -105) {
                player.sendTitle(colorize( config.getString("messages.too-dangerous-to-place")), "", 15, 50, 15);
                Sounds.NOTE_BASS_GUITAR.play(player);
                return;
            }

            if (!(location.getX() >= -20 && location.getY() >= 24 && location.getZ() >= -119 && location.getX() <= 74 && location.getY() <= 71 && location.getZ() <= 29)) {
                player.sendTitle(colorize( config.getString("messages.not-allowed-to-place-outside-arena")), "", 15, 50, 15);
                Sounds.NOTE_BASS_GUITAR.play(player);
                return;
            }

            if (arena.getTeamColor(player) == TeamColor.RED && location.getZ() > -26) {
                if (arena.getTeamColor(player) == TeamColor.RED && location.getZ() > -1) {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes(
                            '&', config.getString("messages.not-allowed-to-place-on-enemy-base")), 1, 50, 1);
                } else {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes(
                            '&', config.getString("messages.not-allowed-to-place-close-to-enemy-base")), 1, 50, 1);
                }
                Sounds.NOTE_BASS_GUITAR.play(player);
                return;
            } else if (arena.getTeamColor(player) == TeamColor.GREEN && location.getZ() < -64) {
                if (arena.getTeamColor(player) == TeamColor.GREEN && location.getZ() < -88) {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes(
                            '&', config.getString("messages.not-allowed-to-place-on-enemy-base")), 1, 50, 1);
                } else {
                    player.sendTitle("", ChatColor.translateAlternateColorCodes(
                            '&', config.getString("messages.not-allowed-to-place-close-to-enemy-base")), 1, 50, 1);
                }
                Sounds.NOTE_BASS_GUITAR.play(player);
                return;
            }

            Structures.spawnRocketStructure(location, structureName, arena.getTeamColor(player));
            location.getWorld().spawnParticle(Particle.CLOUD, location, 20, 0, 0, 0, 0.1);

            if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            }

            arena.getGame().getPlayersDataForGame().stream().filter(x -> x.getUniqueId().equals(player.getUniqueId())).findAny()
                    .ifPresent(playerDataInGame -> playerDataInGame.setRocketsLaunched(playerDataInGame.getRocketsLaunched() + 1));
        }
    }

    public static void createParticleHalo(Location location, Particle particle, double radius, int particleCount) {
        double angleIncrement = 2 * Math.PI / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleIncrement;
            double x = location.getX() + radius * Math.cos(angle);
            double y = location.getY() + 1;
            double z = location.getZ() + radius * Math.sin(angle);

            location.getWorld().spawnParticle(particle, x, y, z, 0);
        }
    }

    public static Set<Block> sphereAround(Location location, int radius) {
        Set<Block> sphere = new HashSet<Block>();
        Block center = location.getBlock();
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    Block b = center.getRelative(x, y, z);
                    if(center.getLocation().distance(b.getLocation()) <= radius) {
                        sphere.add(b);
                    }
                }
            }
        }
        return sphere;
    }

    public static String convertToTime(int n) {
        int minutes = (n % 3600) / 60;
        int seconds = n % 60;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    public static void applyArmorEffects(Player player) {
        GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());
        if (playerData != null) {
            String bootsName = Utils.stripColor(playerData.getBoots().getItemMeta().getDisplayName());
            String helmetName = Utils.stripColor(playerData.getHelmet().getItemMeta().getDisplayName());

            for (String armorType : Arrays.asList("boots", "helmets")) {
                String entriesPath = "menus.armor-shop." + armorType + ".entries";
                for (String armorItemId : menusConfig.getConfigurationSection(entriesPath).getKeys(false)) {
                    String armorItemPath = entriesPath + "." + armorItemId;
                    if (bootsName.equalsIgnoreCase(Utils.stripColor(menusConfig.getString(armorItemPath + ".name"))) ||
                            helmetName.equalsIgnoreCase(Utils.stripColor(menusConfig.getString(armorItemPath + ".name")))) {
                        try {
                            for (String potionEffectType : menusConfig.getConfigurationSection(armorItemPath + ".effects").getKeys(false)) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(potionEffectType), Integer.MAX_VALUE,
                                        menusConfig.getInt(armorItemPath + ".effects." + potionEffectType)), false);
                            }
                        } catch (NullPointerException ignored) {}
                        break;
                    }
                }
            }
        }
    }

    public static ItemStack getCustomTextureHead(String value) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    public static <T> T getRandom(Collection<T> coll) {
        int num = (int) (Math.random() * coll.size());
        for(T t: coll) if (--num < 0) return t;
        throw new AssertionError();
    }
}
