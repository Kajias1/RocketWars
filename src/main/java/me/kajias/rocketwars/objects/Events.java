package me.kajias.rocketwars.objects;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.Utils;
import me.kajias.rocketwars.misc.Sounds;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Events
{
    private static final FileConfiguration config = RocketWars.INSTANCE.getConfig();

    private final Game game;

    public Events(Game game) {
        this.game = game;
    }

    public void checkForEvent() {
        Arena arena = game.getArena();

        if (game.getTimePassed() == config.getInt("game-config.events.event1.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event1.message"));
                Sounds.ORB_PICKUP.play(p);
            });
            game.multiplyItemDropsPeriod(0.8);
            game.setTimeBeforeEvent(config.getInt("game-config.events.event2.time") - config.getInt("game-config.events.event1.time"));
            game.setNextEventLabel(config.getString("game-config.events.event2.label"));
        }
        if (game.getTimePassed() == config.getInt("game-config.events.event2.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event2.message"));
                Sounds.ORB_PICKUP.play(p);
            });
            for (int x = -38; x < 92; ++x) {
                for (int y = 24; y < 60; ++y) {
                    for (int z = -105; z < 15; ++z) {
                        Block block = arena.getWorld().getBlockAt(x, y, z);

                        byte data = 0;
                        if (block.getType() == Material.STAINED_GLASS) {
                            data = block.getData();
                        }

                        if (block.getType() != Material.OBSIDIAN && block.getType() != Material.AIR && data == 0) {
                            int n = new Random().nextInt(3);
                            if (n == 0) block.setType(Material.LEAVES);
                            if (n == 1) block.setType(Material.MELON_BLOCK);
                            if (n == 2) {
                                block.setType(Material.CONCRETE);
                                block.setData(DyeColor.GREEN.getWoolData());
                            }
                            new BukkitRunnable() {
                                int m = 10 + new Random().nextInt(5);

                                @Override
                                public void run() {
                                    m--;
                                    if (m <= 0) {
                                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 1);
                                        block.getWorld().spawnParticle(Particle.CLOUD, block.getLocation(), 4, 0.5, 0.5, 0.5, 0);
                                        block.breakNaturally(new ItemStack(Material.AIR));
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(RocketWars.INSTANCE, 0L, 20L);
                        }

                    }
                }
            }
            game.setTimeBeforeEvent(config.getInt("game-config.events.event3.time") - config.getInt("game-config.events.event2.time"));
            game.setNextEventLabel(config.getString("game-config.events.event3.label"));
        }
        if (game.getTimePassed() == config.getInt("game-config.events.event3.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event3.message"));
                Sounds.ORB_PICKUP.play(p);
            });
            for (int x = -38; x < 92; ++x) {
                for (int y = 23; y < 61; ++y) {
                    for (int z = -98; z < -87; ++z) {
                        Block block1 = arena.getWorld().getBlockAt(x, y, z);
                        Block block2 = arena.getWorld().getBlockAt(x, y, z + 97);

                        if (block1.getType() == Material.STAINED_GLASS) {
                            block1.setType(Material.AIR);
                        }

                        if (block2.getType() == Material.STAINED_GLASS) {
                            block2.setType(Material.AIR);
                        }
                    }
                }
            }
            game.setTimeBeforeEvent(config.getInt("game-config.events.event4.time") - config.getInt("game-config.events.event3.time"));
            game.setNextEventLabel(config.getString("game-config.events.event4.label"));
        }
        if (game.getTimePassed() == config.getInt("game-config.events.event4.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event4.message"));
                Sounds.ORB_PICKUP.play(p);
            });
            for (int x = -38; x < 92; ++x) {
                for (int y = 24; y < 60; ++y) {
                    for (int z = -105; z < 15; ++z) {
                        Block block = arena.getWorld().getBlockAt(x, y, z);

                        byte data = 0;
                        if (block.getType() == Material.STAINED_GLASS) {
                            data = block.getData();
                        }

                        if (block.getType() != Material.OBSIDIAN && block.getType() != Material.AIR && data == 0) {
                            int n = new Random().nextInt(5);
                            if (n == 0) {
                                TNTPrimed tnt = arena.getWorld().spawn(block.getLocation(), TNTPrimed.class);
                                tnt.setFuseTicks(0);
                            }
                        }
                    }
                }
            }
            game.setTimeBeforeEvent(config.getInt("game-config.events.event5.time") - config.getInt("game-config.events.event4.time"));
            game.setNextEventLabel(config.getString("game-config.events.event5.label"));
        }
        if (game.getTimePassed() == config.getInt("game-config.events.event5.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event5.message"));
                Sounds.ORB_PICKUP.play(p);
            });
            for (int x = -38; x < 92; ++x) {
                for (int y = 24; y < 61; ++y) {
                    for (int z = -105; z < -97; ++z) {
                        Block block1 = arena.getWorld().getBlockAt(x, y, z);
                        Block block2 = arena.getWorld().getBlockAt(x, y, z + 113);

                        if (block1.getType() == Material.STAINED_GLASS) {
                            block1.setType(Material.AIR);
                        }

                        if (block2.getType() == Material.STAINED_GLASS) {
                            block2.setType(Material.AIR);
                        }
                    }
                }
            }
            game.setTimeBeforeEvent(config.getInt("game-config.events.event6.time") - config.getInt("game-config.events.event5.time"));
            game.setNextEventLabel(config.getString("game-config.events.event6.label"));
        }
        if (game.getTimePassed() == config.getInt("game-config.events.event6.time")) {
            arena.getPlayers().forEach(p -> {
                Utils.sendMessage(p, config.getString("game-config.events.event6.message").replace("%time%", String.valueOf(config.getInt("game-config.game-duration") - game.getTimePassed())));
                Sounds.ORB_PICKUP.play(p);
            });

            for (int x = -38; x < 92; ++x) {
                for (int y = 24; y < 60; ++y) {
                    for (int z = -105; z < 15; ++z) {
                        Block block = arena.getWorld().getBlockAt(x, y, z);

                        if (block.getType() != Material.OBSIDIAN && block.getType() != Material.AIR) block.setType(Material.AIR);
                    }
                }
            }

            game.setTimeBeforeEvent(config.getInt("game-config.game-duration") - game.getTimePassed());
            game.setNextEventLabel(config.getString("messages.game-end-label"));
        }
    }
}
