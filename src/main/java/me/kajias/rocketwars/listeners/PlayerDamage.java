package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.configs.MenuConfiguration;
import me.kajias.rocketwars.misc.Sounds;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class PlayerDamage implements Listener
{
    private final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    @EventHandler
    public void onPickUpArrow(PlayerPickupArrowEvent e) {
        ItemStack arrow = e.getItem().getItemStack();
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.setDisplayName(Utils.colorize(config.getString("game-config.game-drops.regular-items.items.arrow.name")));
        arrow.setItemMeta(arrowMeta);
    }

    @EventHandler
    public void damage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = ((Player) e.getEntity()).getPlayer();
            Arena arena = Arena.getPlayerArenaMap().get(player);

            if (arena != null && arena.getState() == ArenaState.STARTED) {
                if (player.getHealth() - e.getFinalDamage() <= 0) {
                    player.setHealth(player.getMaxHealth());
                    arena.getGame().teleportToInGameSpawnPoint(player);
                    e.setCancelled(true);
                }
                return;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerShootPlayer(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player attacker = ((Player) e.getDamager()).getPlayer();
            Player victim = (Player) e.getEntity();

            Arena arena = Arena.getPlayerArenaMap().get(attacker);
            if (arena != null) {
                if ((arena.getState() == ArenaState.STARTED || arena.getState() == ArenaState.ENDING) && arena.getTeamColor(attacker) == arena.getTeamColor(victim))
                    e.setCancelled(false);
                return;
            }

            e.setCancelled(true);
        }

        if (e.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) e.getDamager();

            if (projectile.getType() == EntityType.SPECTRAL_ARROW) {
                if (projectile.getShooter() instanceof Player && e.getEntity() instanceof Player) {
                    Player shooter = (Player) projectile.getShooter();
                    Player target = (Player) e.getEntity();
                    if (!(Arena.getPlayerArenaMap().containsKey(shooter) && Arena.getPlayerArenaMap().containsKey(target)))
                        return;
                    Location shooter_location = shooter.getLocation();
                    Location target_location = target.getLocation();
                    shooter.teleport(target_location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    target.teleport(shooter_location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    Sounds.ENDERMAN_TELEPORT.play(shooter);
                    Sounds.ORB_PICKUP.play(shooter);
                    Sounds.ENDERMAN_TELEPORT.play(target);
                    shooter_location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, shooter_location.add(0, 1, 0), 100, 0.0, 1.0, 0.0);
                    target_location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, target_location.add(0, 1, 0), 100, 0.0, 1.0, 0.0);
                    target.sendTitle(ChatColor.translateAlternateColorCodes('&', RocketWars.INSTANCE.getConfig().getString("messages.got-hit-by-spectral-arrow")), "", 10, 50, 10);
                    e.setCancelled(true);
                    return;
                }
            }

            if (projectile.getType() == EntityType.ARROW) {
                Player shooter = (Player) projectile.getShooter();
                Player target = (Player) e.getEntity();

                Arena arena = Arena.getPlayerArenaMap().get(shooter);
                if (arena != null) {
                    if (arena.getTeamColor(target) == arena.getTeamColor(shooter)) e.setDamage(0.0);
                }

                GamePlayer shooterData = DataConfiguration.getPlayerData(shooter.getUniqueId());
                if (shooterData != null && shooterData.getBoughtAbilities().containsKey(
                        Utils.stripColor(MenuConfiguration.getMenuConfig().getConfig().getString("menus.main-shop.bonus-items.explosive-arrow.name")))) {
                    if (!(target.getLocation().getZ() > 13 || target.getLocation().getZ() < -102)) {
                        if (new Random().nextInt(2) == 0) {
                            TNTPrimed tnt = (TNTPrimed) target.getWorld().spawnEntity(target.getLocation(), EntityType.PRIMED_TNT);
                            tnt.setFuseTicks(25);
                            tnt.setYield(2.0f);
                        }
                    }
                }
            }
        }
    }
}
