package io.github.bedwarsrel.shop.Specials;

import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.Team;
import io.github.bedwarsrel.utils.ChatWriter;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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

}
