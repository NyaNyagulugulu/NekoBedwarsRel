package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsUseTNTSheepEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.GameState;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.scheduler.BukkitRunnable;

public class TNTSheep extends SpecialItem {

  private Game game = null;
  private Player player = null;
  private ITNTSheep sheep = null;

  private Player findTargetPlayer(Player player) {
    Player foundPlayer = null;

    if (game.getPlayers().size() == 1) {
      foundPlayer = player;
    } else {
      double distance = Double.MAX_VALUE;

      Team playerTeam = this.game.getPlayerTeam(player);

      ArrayList<Player> possibleTargets = new ArrayList<Player>();
      possibleTargets.addAll(this.game.getTeamPlayers());
      possibleTargets.removeAll(playerTeam.getPlayers());

      for (Player p : possibleTargets) {
        if (player.getWorld() != p.getWorld()) {
          continue;
        }
        double dist = player.getLocation().distance(p.getLocation());
        if (dist < distance) {
          foundPlayer = p;
          distance = dist;
        }
      }
    }
    return foundPlayer;
  }

  @Override
  public Material getActivatedMaterial() {
    return null;
  }

  public int getEntityTypeId() {
    return 91;
  }

  public Game getGame() {
    return this.game;
  }

  public void setGame(Game game) {
    this.game = game;
  }

  @Override
  public Material getItemMaterial() {
    return Material.MONSTER_EGG;
  }

  public Player getPlayer() {
    return this.player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public ITNTSheep getSheep() {
    return this.sheep;
  }

  @SuppressWarnings("deprecation")
  public void run(Location startLocation) {
    // TNTSheep functionality has been disabled
    // This prevents entity registration issues in newer Java versions
    this.player.sendMessage(ChatWriter
        .pluginMessage(
            ChatColor.RED + "TNTSheep special item has been disabled to prevent compatibility issues."));
  }

  public void updateTNT() {
    new BukkitRunnable() {

      @Override
      public void run() {
        final TNTSheep that = TNTSheep.this;

        if (that.game.isStopping() || that.game.getState() != GameState.RUNNING) {
          return;
        }

        if (that.sheep == null) {
          return;
        }

        if (that.sheep.getTNT() == null) {
          return;
        }

        TNTPrimed old = that.sheep.getTNT();
        final int fuse = old.getFuseTicks();

        if (fuse <= 0) {
          return;
        }

        final Entity source = old.getSource();
        final Location oldLoc = old.getLocation();
        final float yield = old.getYield();
        old.leaveVehicle();
        old.remove();

        new BukkitRunnable() {

          @Override
          public void run() {
            TNTPrimed primed = (TNTPrimed) that.game.getRegion().getWorld().spawnEntity(oldLoc,
                EntityType.PRIMED_TNT);
            primed.setFuseTicks(fuse);
            primed.setYield(yield);
            primed.setIsIncendiary(false);
            that.sheep.setPassenger(primed);
            that.sheep.setTNT(primed);
            that.sheep.setTNTSource(source);

            if (primed.getFuseTicks() >= 60) {
              that.updateTNT();
            }
          }
        }.runTaskLater(BedwarsRel.getInstance(), 3L);
      }

    }.runTaskLater(BedwarsRel.getInstance(), 60L);
  }

}
