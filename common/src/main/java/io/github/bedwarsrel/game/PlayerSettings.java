package io.github.bedwarsrel.game;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.database.QuickBuySettingsManager;
import io.github.bedwarsrel.villager.VillagerTrade;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;

public class PlayerSettings {

  private Object hologram = null;
  private boolean isTeleporting = false;
  private Player player = null;
  private boolean useOldShop = false;
  // 快捷购买设置，存储物品的标识符
  private List<String> quickBuySettings = new ArrayList<String>();
  // 用于追踪是否需要保存到数据库
  private boolean quickBuySettingsModified = false;
  private QuickBuySettingsManager qbManager = null;

  public PlayerSettings(Player player) {
    this.player = player;
    // 初始化快捷购买管理器
    if (BedwarsRel.getInstance().getDatabaseManager() != null) {
      this.qbManager = new QuickBuySettingsManager(BedwarsRel.getInstance().getDatabaseManager());
    }
    // 旧版商店已被移除，始终使用新版商店
    this.useOldShop = false;
    // 初始化快捷购买设置（从数据库加载，如果不存在则使用默认值）
    loadQuickBuySettingsFromDatabase();
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

  public boolean useOldShop() {
    // 旧版商店已被移除，始终返回false
    return false;
  }

  /**
   * 获取快捷购买设置
   * @return 快捷购买设置列表
   */
  public List<String> getQuickBuySettings() {
    return this.quickBuySettings;
  }

  /**
   * 设置快捷购买设置
   * @param quickBuySettings 快捷购买设置列表
   */
  public void setQuickBuySettings(List<String> quickBuySettings) {
    this.quickBuySettings = quickBuySettings;
    this.quickBuySettingsModified = true;
    // 延迟保存到数据库，避免频繁写入
    scheduleSaveToDatabase();
  }

  /**
   * 设置快捷购买槽位的物品
   * @param slot 槽位索引 (0-8)
   * @param itemIdentifier 物品标识符
   */
  public void setQuickBuyItem(int slot, String itemIdentifier) {
    if (slot >= 0 && slot < 9) {
      if (this.quickBuySettings.size() <= slot) {
        // 确保列表大小足够
        while (this.quickBuySettings.size() <= slot) {
          this.quickBuySettings.add(null);
        }
      }
      this.quickBuySettings.set(slot, itemIdentifier);
      this.quickBuySettingsModified = true;
      // 延迟保存到数据库，避免频繁写入
      scheduleSaveToDatabase();
    }
  }

  /**
   * 获取快捷购买槽位的物品
   * @param slot 槽位索引 (0-8)
   * @return 物品标识符
   */
  public String getQuickBuyItem(int slot) {
    if (slot >= 0 && slot < this.quickBuySettings.size()) {
      return this.quickBuySettings.get(slot);
    }
    return null;
  }

  /**
   * 从数据库加载快捷购买设置
   */
  private void loadQuickBuySettingsFromDatabase() {
    if (this.qbManager != null) {
      this.quickBuySettings = this.qbManager.loadQuickBuySettings(this.player.getUniqueId());
    } else {
      // 如果数据库未启用，初始化默认设置
      for (int i = 0; i < 9; i++) {
        this.quickBuySettings.add(null); // 默认为空
      }
    }
  }

  /**
   * 异步延迟保存快捷购买设置到数据库
   */
  private void scheduleSaveToDatabase() {
    if (this.qbManager != null && this.quickBuySettingsModified) {
      // 使用异步任务延迟保存，避免频繁写入
      CompletableFuture.runAsync(() -> {
        try {
          Thread.sleep(1000); // 延迟1秒，避免频繁写入
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        // 检查在此期间是否再次修改
        if (this.quickBuySettingsModified) {
          this.qbManager.saveQuickBuySettings(this.player.getUniqueId(), this.quickBuySettings);
          this.quickBuySettingsModified = false;
        }
      });
    }
  }

  /**
   * 立即保存快捷购买设置到数据库（在特殊情况下使用，如玩家退出游戏时）
   */
  public void saveQuickBuySettingsImmediately() {
    if (this.qbManager != null && this.quickBuySettingsModified) {
      this.qbManager.saveQuickBuySettings(this.player.getUniqueId(), this.quickBuySettings);
      this.quickBuySettingsModified = false;
    }
  }
}