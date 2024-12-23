package me.kajias.rocketwars.listeners;

import me.kajias.rocketwars.RocketWars;
import me.kajias.rocketwars.configs.DataConfiguration;
import me.kajias.rocketwars.objects.Arena;
import me.kajias.rocketwars.objects.GamePlayer;
import me.kajias.rocketwars.objects.enums.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.BlockIterator;

public class ProjectileHit implements Listener
{
   @EventHandler
   public void onProjectileHit(ProjectileHitEvent event) {
      if(!(event.getEntity() instanceof Arrow))
         return;

      Arrow arrow = (Arrow) event.getEntity();
      if(!(arrow.getShooter() instanceof Player))
         return;

      Player player = (Player) arrow.getShooter();
      Arena arena = Arena.getPlayerArenaMap().get(player);
      if (arena != null) {
         BlockIterator iterator = new BlockIterator(arrow.getWorld(), arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 4);
         Block hitBlock;
         while (iterator.hasNext()) {
            hitBlock = iterator.next();
            if (hitBlock.getType() == Material.TNT) {
               if (arena.getState() == ArenaState.STARTED) {
                  arena.getGame().getPlayersDataForGame().stream().filter(x -> x.getUniqueId().equals(player.getUniqueId())).findAny()
                          .ifPresent(playerDataInGame -> playerDataInGame.setRocketsDestroyed(playerDataInGame.getRocketsDestroyed() + 1));
               }
               break;
            }
         }
      }
   }
}
