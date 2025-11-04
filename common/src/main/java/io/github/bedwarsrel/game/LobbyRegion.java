package io.github.bedwarsrel.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import java.util.ArrayList;
import java.util.List;

/**
 * 大厅区域管理类
 * 用于在游戏开始时清空大厅区域，并在游戏结束后恢复
 */
public class LobbyRegion {
    // 区域的最小角坐标
    private Location minCorner;
    // 区域的最大角坐标
    private Location maxCorner;
    // 区域所在的世界
    private World world;
    // 存储原始方块的列表
    private List<Block> originalBlocks;
    // 标记大厅区域是否已被激活（即是否已被清空）
    private boolean isActive = false;

    /**
     * 构造函数，初始化大厅区域
     * @param pos1 区域的第一个坐标
     * @param pos2 区域的第二个坐标
     */
    public LobbyRegion(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("无效的大厅区域坐标");
        }

        this.world = pos1.getWorld();
        this.minCorner = new Location(world, 
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ()));
        this.maxCorner = new Location(world,
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ()));
        this.originalBlocks = new ArrayList<>();
    }

    /**
     * 将大厅区域的方块填充为空气
     * 在填充前会存储原始方块以备恢复
     */
    public void fillWithAir() {
        if (isActive) return;
        
        // 填充空气前先存储原始方块
        originalBlocks.clear();
        for (int x = minCorner.getBlockX(); x <= maxCorner.getBlockX(); x++) {
            for (int y = minCorner.getBlockY(); y <= maxCorner.getBlockY(); y++) {
                for (int z = minCorner.getBlockZ(); z <= maxCorner.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    originalBlocks.add(block);
                }
            }
        }
        
        // 填充空气
        for (int x = minCorner.getBlockX(); x <= maxCorner.getBlockX(); x++) {
            for (int y = minCorner.getBlockY(); y <= maxCorner.getBlockY(); y++) {
                for (int z = minCorner.getBlockZ(); z <= maxCorner.getBlockZ(); z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
        
        isActive = true;
    }

    /**
     * 恢复大厅区域的原始状态
     * 将之前存储的原始方块重新放置回去
     */
    public void restore() {
        if (!isActive) return;
        
        // 恢复原始方块
        for (Block block : originalBlocks) {
            world.getBlockAt(block.getLocation()).setType(block.getType());
        }
        
        originalBlocks.clear();
        isActive = false;
    }

    /**
     * 检查大厅区域是否已被激活
     * @return 如果大厅区域已被激活则返回true，否则返回false
     */
    public boolean isActive() {
        return isActive;
    }
}