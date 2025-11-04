package io.github.bedwarsrel.listener;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.events.BedwarsGameStartEvent;
import io.github.bedwarsrel.events.BedwarsGameResetEvent;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.LobbyRegion;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * 大厅区域监听器
 * 监听游戏开始和重置事件，处理大厅区域的清空和恢复
 */
public class LobbyRegionListener implements Listener {
    
    /**
     * 当游戏开始时触发
     * 清空大厅区域的方块
     * @param event 游戏开始事件
     */
    @EventHandler
    public void onGameStart(BedwarsGameStartEvent event) {
        Game game = event.getGame();
        Location lobby = game.getLobby();
        
        // 检查大厅是否已设置且与游戏区域在不同世界
        if (lobby != null && game.getRegion() != null && 
            !lobby.getWorld().equals(game.getRegion().getWorld())) {
            
            // 在大厅生成点周围创建一个大厅区域
            // 使用以大厅位置为中心的10x10x10区域
            Location loc1 = lobby.clone().add(-5, -5, -5);
            Location loc2 = lobby.clone().add(5, 5, 5);
            
            LobbyRegion lobbyRegion = new LobbyRegion(loc1, loc2);
            lobbyRegion.fillWithAir();
            
            // 将大厅区域存储到游戏对象中以便后续恢复
            game.setLobbyRegion(lobbyRegion);
        }
    }
    
    /**
     * 当游戏重置时触发
     * 恢复大厅区域的原始状态
     * @param event 游戏重置事件
     */
    @EventHandler
    public void onGameReset(BedwarsGameResetEvent event) {
        // 当游戏重置时会调用此事件
        // 恢复大厅区域
        Game game = event.getGame();
        if (game.getLobbyRegion() != null) {
            game.getLobbyRegion().restore();
        }
    }
}