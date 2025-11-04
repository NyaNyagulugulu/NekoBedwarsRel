package io.github.bedwarsrel.events;

import io.github.bedwarsrel.game.Game;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bedwars游戏重置事件
 * 当游戏重置时触发此事件
 */
public class BedwarsGameResetEvent extends Event {

  private static final HandlerList handlers = new HandlerList();
  private Game game = null;

  /**
   * 构造函数
   * @param game 重置的游戏对象
   */
  public BedwarsGameResetEvent(Game game) {
    this.game = game;
  }

  /**
   * 获取事件处理器列表
   * @return 事件处理器列表
   */
  public static HandlerList getHandlerList() {
    return BedwarsGameResetEvent.handlers;
  }

  /**
   * 获取重置的游戏对象
   * @return 重置的游戏对象
   */
  public Game getGame() {
    return this.game;
  }

  /**
   * 获取事件处理器列表
   * @return 事件处理器列表
   */
  @Override
  public HandlerList getHandlers() {
    return BedwarsGameResetEvent.handlers;
  }

}