package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import org.bukkit.entity.Player;

public class PlayerSettings {

  private Object hologram = null;
  private boolean isTeleporting = false;
  private Player player = null;
  private boolean useOldShop = false;

  public PlayerSettings(Player player) {
    this.player = player;
    this.useOldShop = BedwarsRel.getInstance()
        .getBooleanConfig("player-settings.old-shop-as-default", false);
  }

  public Object getHologram() {
    return this.hologram;
  }

  public void setHologram(Object holo) {
    this.hologram = holo;
  }

  public Player getPlayer() {
    return this.player;
  }

  public boolean isTeleporting() {
    return isTeleporting;
  }

  public void setTeleporting(boolean isTeleporting) {
    this.isTeleporting = isTeleporting;
  }

  

  public void setUseOldShop(boolean value) {
    this.useOldShop = value;
  }

  public boolean useOldShop() {
    return this.useOldShop;
  }

}
