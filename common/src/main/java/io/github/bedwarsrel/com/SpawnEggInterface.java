package io.github.bedwarsrel.com;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnEggInterface {
  
  /**
   * 获取这个蛋将生成的实体类型
   *
   * @return 实体类型
   */
  EntityType getSpawnedType();
  
  /**
   * 设置这个蛋将生成的实体类型
   *
   * @param type 实体类型
   */
  void setSpawnedType(EntityType type);
  
  /**
   * 获取一个生成蛋的ItemStack
   *
   * @return ItemStack
   */
  ItemStack toItemStack();
  
  /**
   * 获取多个生成蛋的ItemStack
   *
   * @return 生成蛋的ItemStack
   */
  ItemStack toItemStack(int amount);
  
  /**
   * 克隆这个生成蛋
   *
   * @return 这个生成蛋的克隆
   */
  SpawnEggInterface clone();
}