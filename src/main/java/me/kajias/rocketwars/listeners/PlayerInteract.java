package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.gui.menus.ArenaSelectMenu;
import me.kajias.rocketwars.gui.menus.player_utilities.MainShopMenu;
import me.kajias.rocketwars.gui.menus.TeamSelectMenu;
import me.kajias.rocketwars.managers.ArenaManager;
import me.kajias.rocketwars.misc.GameItems;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.misc.Structures;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PlayerInteract implements Listener
{
    private static final FileConfiguration menuConfig = MenuConfiguration.getMenuConfig().getConfig();
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private static final List<String> respawnItemCoolDownMessageDisable = new ArrayList<>();
    private static final HashMap<String, Integer> respawnItemCoolDownMap = new HashMap<>();

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getItemDrop() == null) return;
        ItemStack item = e.getItemDrop().getItemStack();

        if (item.getType() == Material.LEATHER_BOOTS || item.getType() == Material.LEATHER_HELMET) e.setCancelled(true);
        if (Utils.isHotBarItem(item)) e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerSwapItems(PlayerSwapHandItemsEvent e) {
        if (e.getOffHandItem() == null) return;
        ItemStack item = e.getOffHandItem();
        GamePlayer playerData = DataConfiguration.getPlayerData(e.getPlayer().getUniqueId());

        if (Utils.isHotBarItem(item)) e.setCancelled(true);
        else if (!playerData.getBoughtAbilities().containsKey(Utils.stripColor(menuConfig.getString("menus.main-shop.bonus-items.second-hand.name"))) &&
        Arena.getPlayerArenaMap().get(e.getPlayer()) != null) {
            Utils.sendMessage(e.getPlayer(), config.getString("messages.cant-swap-items"));
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;
        if (!(e.getWhoClicked() instanceof Player)) return;

        ItemStack clickedItem = e.getCurrentItem();
        Player player = ((Player) e.getWhoClicked()).getPlayer();
        Arena arena = Arena.getPlayerArenaMap().get(player);
        if (e.getHotbarButton() != -1) {
            ItemStack item = player.getInventory().getItem(e.getHotbarButton());
            try {
                if (Utils.isHotBarItem(item)) {
                    e.setCancelled(true);
                    return;
                }
            } catch (NullPointerException ignored) {}
        }

        if (Utils.isHotBarItem(e.getCurrentItem())) {
            if (doAction(e.getCurrentItem(), player, null)) e.setCancelled(true);
        }

        if (arena != null) {
            if (clickedItem.getType() == Material.LEATHER_BOOTS || clickedItem.getType() == Material.LEATHER_HELMET) e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRightClickMinecartChest(PlayerInteractEntityEvent e) {
        Player player = e.getPlayer();
        Arena arena = Arena.getPlayerArenaMap().get(player);
        if (arena != null && arena.getState() == ArenaState.STARTED) {
            if (e.getRightClicked().getType() == EntityType.MINECART_CHEST) {
                arena.getGame().getGameItems().giveRandomShipLoot(player);
                e.getRightClicked().remove();
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeftClickMinecartChest(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.MINECART_CHEST && e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            Arena arena = Arena.getPlayerArenaMap().get(player);
            if (arena != null && arena.getState() == ArenaState.STARTED) {
                arena.getGame().getGameItems().giveRandomShipLoot(player);
                e.getEntity().remove();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null) return;

        Player player = e.getPlayer();
        if (Utils.isHotBarItem(e.getItem())) {
            if (doAction(e.getItem(), player, e.getAction())) {
                e.setCancelled(true);
                return;
            }
        }

        Arena arena = Arena.getPlayerArenaMap().get(player);

        if (arena != null && arena.getState() == ArenaState.STARTED) {
            if (Utils.isInGameItem(e.getItem())) {
                if (doAction(e.getItem(), player, e.getAction())) {
                    e.setCancelled(true);
                    return;
                }
            }

            if (e.getItem().getType() == Material.MONSTER_EGG && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Utils.spawnRocket(e.getPlayer(), e.getClickedBlock().getLocation(), e.getItem().getItemMeta().getDisplayName());
                e.setCancelled(true);
            }
        }
    }

    private boolean doAction(ItemStack item, Player player, Action action) {
        String displayName = item.getItemMeta().getDisplayName();
        if (displayName.equals(Utils.colorize(config.getString("hot-bar-items.fast-join-item.name"))) || displayName.equals(Utils.colorize(config.getString("hot-bar-items.new-game-item.name")))) {
            Arena arena = ArenaManager.findBestArena();
            if (arena != null) {
                arena.addPlayer(player);
            } else Utils.sendMessage(player, config.getString("messages.no-available-arenas"));
            return true;
        }

        if (displayName.equals(Utils.colorize(config.getString("hot-bar-items.select-arena-item.name")))) {
            RocketWars.guiManager.openGUI(new ArenaSelectMenu(), player);
            return true;
        }

        if (displayName.equals(Utils.colorize(config.getString("hot-bar-items.shop-item.name")))) {
            RocketWars.guiManager.openGUI(new MainShopMenu(), player);
            return true;
        }

        if (displayName.equals(Utils.colorize(config.getString("hot-bar-items.leave-game-item.name")))) {
            if (Arena.getPlayerArenaMap().containsKey(player)) {
                Arena.getPlayerArenaMap().get(player).removePlayer(player);
            } else Utils.sendMessage(player, config.getString("messages.not-in-game"));
            return true;
        }

        if (displayName.equals(Utils.colorize(config.getString("hot-bar-items.team-select-item.name")))) {
            RocketWars.guiManager.openGUI(new TeamSelectMenu(), player);
            return true;
        }

        if (displayName.equals(Utils.colorize(config.getString("game-config.game-kit.respawn-item.name")))) {
            if (action != null) {
                if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                    GamePlayer playerData = DataConfiguration.getPlayerData(player.getUniqueId());
                    if (!respawnItemCoolDownMap.containsKey(player.getName())) {
                        Arena.getPlayerArenaMap().get(player).getGame().teleportToInGameSpawnPoint(player);
                        Sounds.ENDERMAN_TELEPORT.play(player);
                        if (playerData != null && !playerData.getBoughtAbilities()
                                .containsKey(Utils.stripColor(menuConfig.getString("menus.main-shop.bonus-items.base-teleport-cool-down.name")))) {
                            new BukkitRunnable() {
                                int n = 11;
                                @Override
                                public void run() {
                                    respawnItemCoolDownMap.put(player.getName(), n);
                                    if (n <= 0) {
                                        respawnItemCoolDownMap.remove(player.getName());
                                        cancel();
                                    }
                                    n--;
                                }
                            }.runTaskTimer(RocketWars.INSTANCE, 0L, 20L);
                        }
                    } else {
                        if (!respawnItemCoolDownMessageDisable.contains(player.getName())) {
                            Utils.sendMessage(player, config.getString("messages.base-teleport-cool-down-message")
                                    .replace("%time%", String.valueOf(respawnItemCoolDownMap.get(player.getName()))));
                            respawnItemCoolDownMessageDisable.add(player.getName());
                            Bukkit.getScheduler().runTaskLater(RocketWars.INSTANCE, () -> respawnItemCoolDownMessageDisable.remove(player.getName()), 5 * 20L);
                        }

                        Sounds.NOTE_BASS.play(player);
                    }
                }
            }
            return true;
        }

        if (action != null) {
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.regular-items.items.shield.name")))) {
                    Snowball snowball = player.launchProjectile(Snowball.class);
                    Sounds.ITEM_PICKUP.play(player);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!snowball.isDead()) {
                                Location location = snowball.getLocation();
                                location.getWorld().spawnParticle(Particle.CLOUD, location, 2, 0.1, 0.1, 0.1, 0);
                                if (location.getZ() > 15 || location.getZ() < -105) {
                                    player.sendTitle("", Utils.colorize(config.getString("messages.cant-use-shield-near-wall")), 1, 50, 1);
                                    Sounds.NOTE_BASS_GUITAR.play(player);
                                    snowball.remove();
                                    cancel();
                                }
                            }
                            if (snowball.getTicksLived() >= 20 || snowball.isDead() && !isCancelled()) {
                                Location location = snowball.getLocation().add(3, -3, 0);
                                location.getWorld().spawnParticle(Particle.CLOUD, location, 2, 0, 0, 0, 0);
                                Structures.spawnShieldStructure(location, Utils.stripColor(config.getString("game-config.game-drops.regular-items.items.shield.name")));
                                snowball.remove();
                                cancel();
                            }
                        }
                    }.runTaskTimer(RocketWars.INSTANCE, 0L, 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.regular-items.items.fireball.name"))) ||
                        displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.enhanced-fireball.name")))) {
                    if (player.getLocation().getZ() > 13 || player.getLocation().getZ() < -102) {
                        player.sendTitle("", Utils.colorize(config.getString("messages.cant-use-fireball-against-wall")), 10, 70, 20);
                        Sounds.NOTE_BASS_GUITAR.play(player);
                        return true;
                    }
                    launchFireBall(player, displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.enhanced-fireball.name"))));
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.tnt.name")))) {
                    TNTPrimed tnt = (TNTPrimed) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.PRIMED_TNT);
                    tnt.setFuseTicks(20);
                    tnt.setYield(5.0F);
                    tnt.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.5));

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Location tntLocation = tnt.getLocation();
                            if (tnt.getLocation().getZ() > 15 && tnt.getLocation().getZ() < -105) {
                                player.sendTitle("", Utils.colorize(config.getString("messages.cant-use-tnt-against-wall")), 1, 50, 1);
                                tnt.remove();
                                cancel();
                            }
                            tntLocation.getWorld().spawnParticle(Particle.SMOKE_NORMAL, tntLocation, 50, 0.2, 0.2, 0.2, 0);
                            if (tnt.isDead()) cancel();
                        }
                    }.runTaskTimer(RocketWars.INSTANCE, 0L, 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.grass-bomb.name")))) {
                    FallingBlock grassBomb = Arena.getPlayerArenaMap().get(player).getWorld().spawnFallingBlock(player.getEyeLocation(), Material.GRASS, (byte) 0);
                    grassBomb.setDropItem(false);
                    grassBomb.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(1.5));
                    Sounds.ENDERDRAGON_WINGS.play(player);

                    new BukkitRunnable() {
                        int n = 20;
                        @Override
                        public void run() {
                            Location loc = grassBomb.getLocation();
                            n--;
                            if (loc.getZ() > 15 && loc.getZ() < -105) {
                                player.sendTitle("", Utils.colorize(config.getString("messages.cant-use-grass-bomb-against-wall")), 10, 60, 20);
                                grassBomb.remove();
                                cancel();
                            }
                            if (n <= 0 || grassBomb.isDead()) {
                                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);
                                loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 2, 0, 0, 0, 0);
                                for (Block block : Utils.sphereAround(loc, 9)) {
                                    if (block.getType() != Material.OBSIDIAN && block.getType() != Material.AIR) {
                                        switch(new Random().nextInt(3)) {
                                            case 0: block.setType(Material.MELON_BLOCK); break;
                                            case 1: block.setType(Material.LEAVES); break;
                                            case 2: block.setType(Material.MOSSY_COBBLESTONE); break;
                                        }
                                        new BukkitRunnable() {
                                            int m = 5 + new Random().nextInt(10);
                                            @Override
                                            public void run() {
                                                m--;
                                                if (m <= 0) {
                                                    cancel();
                                                    block.breakNaturally(new ItemStack(Material.AIR));
                                                    block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation(), 5, 0.2, 0.2, 0.2, 0);
                                                }
                                            }
                                        }.runTaskTimer(RocketWars.INSTANCE, 0L, 20L);
                                    }
                                }
                                grassBomb.remove();
                                cancel();
                            }
                        }
                    }.runTaskTimer(RocketWars.INSTANCE, 0L, 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.feather.name")))) {
                    player.setVelocity(player.getEyeLocation().getDirection().multiply(2.0));
                    player.setFallDistance(0.0f);
                    Sounds.ENDERDRAGON_WINGS.play(player);
                    player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 50, 0.3, 0.3, 0.3, 0.03);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.bonus-items.items.drop-boost.name")))) {
                    Arena.getPlayerArenaMap().get(player).getGame().getPlayersWithDropBoost().add(player);
                    player.sendTitle("", Utils.colorize(config.getString("messages.drop-boost-activated")
                            .replace("%duration%", String.valueOf(config.getInt("game-config.drop-boost-duration")))), 10, 70, 20);
                    Sounds.ORB_PICKUP.play(player);

                    new BukkitRunnable() {
                        int dropBoostTime = config.getInt("game-config.drop-boost-duration") * 20;
                        @Override
                        public void run() {
                            dropBoostTime--;
                            player.setLevel(dropBoostTime / 20);
                            Utils.createParticleHalo(player.getLocation(), Particle.REDSTONE, 2, 32);
                            if (!Arena.getPlayerArenaMap().containsKey(player)) cancel();
                            if (dropBoostTime <= 0) {
                                try {
                                    Arena.getPlayerArenaMap().get(player).getGame().getPlayersWithDropBoost().remove(player);
                                } catch (NullPointerException ignored) {}
                                player.setLevel(0);
                                cancel();
                            }
                        }
                    }.runTaskTimer(RocketWars.INSTANCE, 0L, 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.gadgets.items.detonator.name")))) {
                    Location location = player.getLocation();
                    if (action == Action.RIGHT_CLICK_BLOCK) {
                        Vector direction = player.getEyeLocation().getDirection();
                        Vector playerLocation = player.getLocation().toVector();
                        location = playerLocation.add(direction.multiply(3)).toLocation(location.getWorld());
                    }
                    if (location.getZ() > 15 || location.getZ() < -105) {
                        player.sendTitle("", Utils.colorize(config.getString("messages.cant-use-detonator-against-wall")), 10, 50, 20);
                        Sounds.NOTE_BASS_GUITAR.play(player);
                        return false;
                    }
                    TNTPrimed tntPrimed = player.getWorld().spawn(location, TNTPrimed.class);
                    tntPrimed.setFuseTicks(0);
                    tntPrimed.setYield(4.5F);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }

                if (displayName.equals(Utils.colorize(config.getString("game-config.game-drops.gadgets.items.time-back.name")))) {
                    Location location = player.getLocation();

                    new BukkitRunnable() {
                        int time = 3;

                        @Override
                        public void run() {
                            player.sendTitle("", Utils.colorize(config.getString("messages.time-back").replace("%time%", String.valueOf(time))), 10, 30, 20);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 1));

                            if (!Arena.getPlayerArenaMap().containsKey(player)) cancel();
                            if (time <= 0) {
                                player.setFallDistance(0.0f);
                                player.teleport(location);
                                player.setHealth(player.getMaxHealth());
                                Sounds.ENDERMAN_TELEPORT.play(player);
                                for (int i = 0; i < 4; ++i) {
                                    Utils.createParticleHalo(player.getLocation().add(0.0, -1.0 + (float) i * 0.5, 0.0), Particle.PORTAL, 1.5, 100);
                                }
                                cancel();
                            }

                            time--;
                        }
                    }.runTaskTimer(RocketWars.INSTANCE, 0L, 20L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE))
                        item.setAmount(item.getAmount() - 1);
                    return true;
                }
            }
        }

        return false;
    }

    @EventHandler()
    public void onPlayerClickBlock(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player player = e.getPlayer();

        if (Arena.getPlayerArenaMap().containsKey(player) && Arena.getPlayerArenaMap().get(player).getGame() != null) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && !block.isEmpty() && block.getType() == Material.CHEST) {
                e.setCancelled(true);
                List<Entity> nearbyEntities = new ArrayList<>(player.getWorld().getNearbyEntities(player.getLocation(), 5, 5, 5));
                for (Entity entity : nearbyEntities) {
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        entity.remove();

                        Arena.getPlayerArenaMap().get(player).getGame().getGameItems().giveRandomItemDrop(player, "bonus-items", true);
                        Arena.getPlayerArenaMap().get(player).getGame().teleportToInGameSpawnPoint(player);

                        Sounds.ORB_PICKUP.play(player);

                        TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation(), EntityType.PRIMED_TNT);
                        tnt.setFuseTicks(0);
                        tnt.setYield(5.0F);
                        return;
                    }
                }
            }
        }
    }

    private void launchFireBall(Player player, boolean isEnhanced) {
        FileConfiguration config = RocketWars.INSTANCE.getConfig();

        Fireball fireball = player.launchProjectile(Fireball.class);
        fireball.setVelocity(player.getLocation().getDirection().multiply(2));
        fireball.setFireTicks(100);
        fireball.setYield(3.0F);
        if (isEnhanced) fireball.setYield(6.0F);
        fireball.setShooter(player);
        Sounds.GHAST_FIREBALL.play(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!fireball.isDead()) {
                    if (isEnhanced) {
                        Arena.getPlayerArenaMap().get(player).getWorld().spawnParticle(Particle.SMOKE_LARGE, fireball.getLocation(), 20, 0.5, 0.5, 0.5, 0);
                    } else {
                        Arena.getPlayerArenaMap().get(player).getWorld().spawnParticle(Particle.FLAME, fireball.getLocation(), 20, 0.3, 0.3, 0.3, 0);
                    }

                    if (fireball.getLocation().getZ() > 15 || fireball.getLocation().getZ() < -105) {
                        fireball.remove();
                        Utils.sendMessage(player, config.getString("messages.cant-use-fireball-against-wall"));
                        Sounds.NOTE_BASS_GUITAR.play(player);
                        cancel();
                    }
                }
                if (fireball.getTicksLived() >= 200 || fireball.isDead()) {
                    if (isEnhanced) {
                        Location fireballLocation = fireball.getLocation();
                        fireballLocation.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, fireballLocation, 2, 0, 0, 0, 0);
                    }
                    fireball.remove();
                    cancel();
                }
            }
        }.runTaskTimer(RocketWars.INSTANCE, 0L, 1L);
    }
}
