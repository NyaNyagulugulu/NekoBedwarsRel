package io.github.bedwarsrel.com.v1_8_r3;

import io.github.bedwarsrel.com.SpawnEggInterface;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEgg implements SpawnEggInterface {

  private EntityType type;

  public SpawnEgg(EntityType type) {
    this.type = type;
  }

  /**
   * Converts from an item stack to a spawn egg
   *
   * @param item - ItemStack, quantity is disregarded
   * @return SpawnEgg
   */
  public static SpawnEgg fromItemStack(ItemStack item) {
    if (item == null) {
      throw new IllegalArgumentException("item cannot be null");
    }
    if (item.getType() != Material.MONSTER_EGG) {
      throw new IllegalArgumentException("item is not a monster egg");
    }
    net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
    NBTTagCompound tagCompound = stack.getTag();
    if (tagCompound != null) {
      @SuppressWarnings("deprecation")
      EntityType type = EntityType.fromName(tagCompound.getCompound("EntityTag").getString("id"));
      if (type != null) {
        return new SpawnEgg(type);
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  @Override
  public EntityType getSpawnedType() {
    return type;
  }

  @Override
  public void setSpawnedType(EntityType type) {
    if (type.isAlive()) {
      this.type = type;
    }
  }

  @Override
  public ItemStack toItemStack() {
    return toItemStack(1);
  }

  @Override
  @SuppressWarnings("deprecation")
  public ItemStack toItemStack(int amount) {
    ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
    net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(item);
    NBTTagCompound tagCompound = stack.getTag();
    if (tagCompound == null) {
      tagCompound = new NBTTagCompound();
    }
    NBTTagCompound id = new NBTTagCompound();
    id.setString("id", type.getName());
    tagCompound.set("EntityTag", id);
    stack.setTag(tagCompound);
    return CraftItemStack.asBukkitCopy(stack);
  }

  @Override
  public String toString() {
    return "SPAWN EGG{" + getSpawnedType() + "}";
  }

  @Override
  public SpawnEgg clone() {
    return new SpawnEgg(this.type);
  }
}