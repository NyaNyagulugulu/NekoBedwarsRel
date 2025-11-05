package io.github.bedwarsrel.com;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.com.v1_12_r1.SpawnEgg1_12;
import io.github.bedwarsrel.com.v1_8_R3.SpawnEgg;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggFactory {
  
  /**
   * Create a spawn egg for the current server version
   *
   * @param type The entity type
   * @return SpawnEggInterface
   */
  public static SpawnEggInterface createSpawnEgg(EntityType type) {
    String version = BedwarsRel.getInstance().getCurrentVersion();
    
    if (version.startsWith("v1_8")) {
      return new SpawnEgg(type);
    } else if (version.startsWith("v1_12")) {
      return new SpawnEgg1_12(type);
    } else {
      // Default to 1.8 version for older versions
      return new SpawnEgg(type);
    }
  }
  
  /**
   * Convert an item stack to a spawn egg for the current server version
   *
   * @param item The item stack
   * @return SpawnEggInterface
   */
  public static SpawnEggInterface fromItemStack(ItemStack item) {
    String version = BedwarsRel.getInstance().getCurrentVersion();
    
    if (version.startsWith("v1_8")) {
      return SpawnEgg.fromItemStack(item);
    } else if (version.startsWith("v1_12")) {
      return SpawnEgg1_12.fromItemStack(item);
    } else {
      // Default to 1.8 version for older versions
      return SpawnEgg.fromItemStack(item);
    }
  }
}