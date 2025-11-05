package io.github.bedwarsrel.com;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public interface SpawnEggInterface {
  
  /**
   * Get the type of entity this egg will spawn.
   *
   * @return The entity type.
   */
  EntityType getSpawnedType();
  
  /**
   * Set the type of entity this egg will spawn.
   *
   * @param type The entity type.
   */
  void setSpawnedType(EntityType type);
  
  /**
   * Get an ItemStack of one spawn egg
   *
   * @return ItemStack
   */
  ItemStack toItemStack();
  
  /**
   * Get an itemstack of spawn eggs
   *
   * @return ItemStack of spawn eggs
   */
  ItemStack toItemStack(int amount);
  
  /**
   * Clone this spawn egg
   *
   * @return A clone of this spawn egg
   */
  SpawnEggInterface clone();
  
  /**
   * Converts from an item stack to a spawn egg
   *
   * @param item - ItemStack, quantity is disregarded
   * @return SpawnEggInterface
   */
  static SpawnEggInterface fromItemStack(ItemStack item) {
    // This method should be implemented in each version-specific class
    throw new UnsupportedOperationException("This method should be implemented in version-specific classes");
  }
}