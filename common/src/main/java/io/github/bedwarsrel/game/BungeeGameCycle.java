package io.github.bedwarsrel.game;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameEndEvent;
import io.github.bedwarsrel.statistics.PlayerStatistic;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class BungeeGameCycle extends GameCycle {

  public BungeeGameCycle(Game game) {
    super(game);
  }

  public void bungeeSendToServer(final String server, final Player player, boolean preventDelay) {
    if (server == null) {
      player
          .sendMessage(
              ChatWriter
                  .pluginMessage(ChatColor.RED + BedwarsRel._l(player, "errors.bungeenoserver")));
      return;
    }

    new BukkitRunnable() {

      @Override
      public void run() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
          out.writeUTF("Connect");
          out.writeUTF(server);
        } catch (Exception e) {
          BedwarsRel.getInstance().getBugsnag().notify(e);
          e.printStackTrace();
          return;
        }

        if (b != null) {
          player.sendPluginMessage(BedwarsRel.getInstance(), "BungeeCord", b.toByteArray());
        }
      }
    }.runTaskLater(BedwarsRel.getInstance(), (preventDelay) ? 0L : 20L);
  }

  private void kickAllPlayers() {
    for (Player player : this.getGame().getTeamPlayers()) {
      for (Player freePlayer : this.getGame().getFreePlayers()) {
        player.showPlayer(freePlayer);
      }
      this.getGame().playerLeave(player, false);
    }

    for (Player freePlayer : this.getGame().getFreePlayersClone()) {
      this.getGame().playerLeave(freePlayer, false);
    }
  }

  @Override
  public void onGameEnds() {
    if (BedwarsRel.getInstance().getBooleanConfig("bungeecord.full-restart", true)) {
      this.kickAllPlayers();

      this.getGame().resetRegion();
      new BukkitRunnable() {

        @Override
        public void run() {
          if (BedwarsRel.getInstance().isSpigot()
              && BedwarsRel.getInstance().getBooleanConfig("bungeecord.spigot-restart", true)) {
            BedwarsRel.getInstance().getServer()
                .dispatchCommand(BedwarsRel.getInstance().getServer().getConsoleSender(),
                    "restart");
          } else {
            Bukkit.shutdown();
          }
        }
      }.runTaskLater(BedwarsRel.getInstance(), 70L);
    } else {
      // Reset scoreboard first
      this.getGame().resetScoreboard();

      // 根据用户需求，游戏结束后玩家应保留在游戏地图中
      // 但Bungee服务器仍需要踢出玩家
      if (BedwarsRel.getInstance().isBungee()) {
        this.kickAllPlayers();
      } else {
        // 对于非Bungee服务器，让玩家留在游戏地图中
        for (Player player : this.getGame().getTeamPlayers()) {
          // 处理统计和物品栏
          if (BedwarsRel.getInstance().statisticsEnabled()) {
            PlayerStatistic statistic =
                BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(player);
            BedwarsRel.getInstance().getPlayerStatisticManager().storeStatistic(statistic);

            if (BedwarsRel.getInstance().getBooleanConfig("statistics.show-on-game-end", true)) {
              BedwarsRel.getInstance().getServer().dispatchCommand(player, "bw stats");
            }
          }

          this.getGame().setPlayerDamager(player, null);
          PlayerStorage storage = this.getGame().getPlayerStorage(player);
          storage.clean();
        }

        // 处理旁观者
        for (Player freePlayer : this.getGame().getFreePlayersClone()) {
          // 处理统计和物品栏
          if (BedwarsRel.getInstance().statisticsEnabled()) {
            PlayerStatistic statistic =
                BedwarsRel.getInstance().getPlayerStatisticManager().getStatistic(freePlayer);
            BedwarsRel.getInstance().getPlayerStatisticManager().storeStatistic(statistic);

            if (BedwarsRel.getInstance().getBooleanConfig("statistics.show-on-game-end", true)) {
              BedwarsRel.getInstance().getServer().dispatchCommand(freePlayer, "bw stats");
            }
          }

          this.getGame().setPlayerDamager(freePlayer, null);
          PlayerStorage storage = this.getGame().getPlayerStorage(freePlayer);
          storage.clean();
        }
      }

      // reset countdown prevention breaks
      this.setEndGameRunning(false);

      // Reset team chests
      for (Team team : this.getGame().getTeams().values()) {
        team.setInventory(null);
        team.getChests().clear();
      }

      // clear protections
      this.getGame().clearProtections();

      // set state and with that, the sign
      this.getGame().setState(GameState.WAITING);
      this.getGame().updateScoreboard();

      // reset region
      this.getGame().resetRegion();
    }
  }

  @Override
  public void onGameLoaded() {
    // Reset on game end
  }

  @Override
  public void onGameOver(GameOverTask task) {
    // 根据用户需求，游戏结束后玩家应保留在游戏地图中，而不是被传送到大厅
    // 保留Bungee服务器的逻辑，但不传送玩家到大厅
    if (BedwarsRel.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true)) {
      final ArrayList<Player> players = new ArrayList<Player>();
      final Game game = this.getGame();
      players.addAll(this.getGame().getTeamPlayers());
      players.addAll(this.getGame().getFreePlayers());
      for (Player player : players) {
        // 根据用户需求，不再传送玩家到大厅
        // if (!player.getWorld().equals(this.getGame().getLobby().getWorld())) {
        //   game.getPlayerSettings(player).setTeleporting(true);
        //   player.teleport(this.getGame().getLobby());
        //   game.getPlayerStorage(player).clean();
        // }

        // 保持玩家在游戏地图中
        game.getPlayerStorage(player).clean();
      }

      new BukkitRunnable() {
        @Override
        public void run() {
          for (Player player : players) {
            game.setPlayerGameMode(player);
            game.setPlayerVisibility(player);

            if (!player.getInventory().contains(Material.SLIME_BALL)) {
              // Leave game (Slimeball)
              ItemStack leaveGame = new ItemStack(Material.SLIME_BALL, 1);
              ItemMeta im = leaveGame.getItemMeta();
              im.setDisplayName(BedwarsRel._l(player, "lobby.leavegame"));
              leaveGame.setItemMeta(im);
              player.getInventory().setItem(8, leaveGame);
              player.updateInventory();
            }
          }
        }
      }.runTaskLater(BedwarsRel.getInstance(), 20L);
    }
    if (task.getCounter() == task.getStartCount() && task.getWinner() != null) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(ChatColor.GOLD + BedwarsRel._l(aPlayer, "ingame.teamwon",
                  ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD))));
        }
      }
    } else if (task.getCounter() == task.getStartCount() && task.getWinner() == null) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(
              ChatWriter.pluginMessage(ChatColor.GOLD + BedwarsRel._l(aPlayer, "ingame.draw")));
        }
      }
    }

    // game over
    if (this.getGame().getPlayers().size() == 0 || task.getCounter() == 0) {
      BedwarsGameEndEvent endEvent = new BedwarsGameEndEvent(this.getGame());
      BedwarsRel.getInstance().getServer().getPluginManager().callEvent(endEvent);

      this.onGameEnds();
      task.cancel();
    } else if ((task.getCounter() == task.getStartCount()) || (task.getCounter() % 10 == 0)
        || (task.getCounter() <= 5 && (task.getCounter() > 0))) {
      for (Player aPlayer : this.getGame().getPlayers()) {
        if (aPlayer.isOnline()) {
          aPlayer.sendMessage(ChatWriter
              .pluginMessage(ChatColor.AQUA + BedwarsRel
                  ._l(aPlayer, "ingame.serverrestart", ImmutableMap
                      .of("sec",
                          ChatColor.YELLOW.toString() + task.getCounter() + ChatColor.AQUA))));
        }
      }
    }

    task.decCounter();
  }

  @Override
  public void onGameStart() {
    // do nothing, world will be reseted on restarting
  }

  @Override
  public boolean onPlayerJoins(Player player) {
    final Player p = player;

    if (this.getGame().isFull() && !player.hasPermission("bw.vip.joinfull")) {
      if (this.getGame().getState() != GameState.RUNNING
          || !BedwarsRel.getInstance().spectationEnabled()) {
        this.bungeeSendToServer(BedwarsRel.getInstance().getBungeeHub(), p, false);
        new BukkitRunnable() {

          @Override
          public void run() {
            BungeeGameCycle.this.sendBungeeMessage(p,
                ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(p, "lobby.gamefull")));
          }
        }.runTaskLater(BedwarsRel.getInstance(), 60L);

        return false;
      }
    } else if (this.getGame().isFull() && player.hasPermission("bw.vip.joinfull")) {
      if (this.getGame().getState() == GameState.WAITING) {
        List<Player> players = this.getGame().getNonVipPlayers();

        if (players.size() == 0) {
          this.bungeeSendToServer(BedwarsRel.getInstance().getBungeeHub(), p, false);
          new BukkitRunnable() {

            @Override
            public void run() {
              BungeeGameCycle.this.sendBungeeMessage(p,
                  ChatWriter
                      .pluginMessage(ChatColor.RED + BedwarsRel._l(p, "lobby.gamefullpremium")));
            }
          }.runTaskLater(BedwarsRel.getInstance(), 60L);
          return false;
        }

        Player kickPlayer = null;
        if (players.size() == 1) {
          kickPlayer = players.get(0);
        } else {
          kickPlayer = players.get(Utils.randInt(0, players.size() - 1));
        }

        final Player kickedPlayer = kickPlayer;

        this.getGame().playerLeave(kickedPlayer, false);
        new BukkitRunnable() {

          @Override
          public void run() {
            BungeeGameCycle.this.sendBungeeMessage(kickedPlayer,
                ChatWriter
                    .pluginMessage(
                        ChatColor.RED + BedwarsRel._l(kickedPlayer, "lobby.kickedbyvip")));
          }
        }.runTaskLater(BedwarsRel.getInstance(), 60L);
      } else {
        if (this.getGame().getState() == GameState.RUNNING
            && !BedwarsRel.getInstance().spectationEnabled()) {

          new BukkitRunnable() {

            @Override
            public void run() {
              BungeeGameCycle.this
                  .bungeeSendToServer(BedwarsRel.getInstance().getBungeeHub(), p, false);
            }

          }.runTaskLater(BedwarsRel.getInstance(), 5L);

          new BukkitRunnable() {

            @Override
            public void run() {
              BungeeGameCycle.this.sendBungeeMessage(p,
                  ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel._l(p, "lobby.gamefull")));
            }
          }.runTaskLater(BedwarsRel.getInstance(), 60L);
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public void onPlayerLeave(Player player) {
    // 根据用户需求，Bungee服务器传送逻辑保留，但非Bungee服务器玩家留在游戏地图中
    if (BedwarsRel.getInstance().isBungee()) {
      if (player.isOnline() || player.isDead()) {
        this.bungeeSendToServer(BedwarsRel.getInstance().getBungeeHub(), player, true);
      }
    }
    // 非Bungee服务器：玩家留在游戏地图中，不进行任何传送

    if (this.getGame().getState() == GameState.RUNNING && !this.getGame().isStopping()) {
      this.checkGameOver();
    }
  }

  public void sendBungeeMessage(Player player, String message) {
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    out.writeUTF("Message");
    out.writeUTF(player.getName());
    out.writeUTF(message);

    player.sendPluginMessage(BedwarsRel.getInstance(), "BungeeCord", out.toByteArray());
  }

}
