package io.github.bedwarsrel.com.v1_12_r1;

import io.github.bedwarsrel.com.SpawnEggInterface;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEgg1_12 implements SpawnEggInterface {

  private EntityType type;

  public SpawnEgg1_12(EntityType type) {
    this.type = type;
  }

  /**
   * 从物品堆栈转换为1.12版本的生成蛋
   *
   * @param item - ItemStack，数量被忽略
   * @return SpawnEgg 1.12
   */
  public static SpawnEgg1_12 fromItemStack(ItemStack item) {
    if (item == null) {
      throw new IllegalArgumentException("item cannot be null");
    }
    if (item.getType() != Material.MONSTER_EGG) {
      throw new IllegalArgumentException("item is not a monster egg");
    }
    net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
    NBTTagCompound tagCompound = stack.getTag();
    if (tagCompound != null) {
      @SuppressWarnings("deprecation")
      EntityType type = EntityType.fromName(tagCompound.getCompound("EntityTag").getString("id"));
      if (type != null) {
        return new SpawnEgg1_12(type);
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
    net.minecraft.server.v1_12_R1.ItemStack stack = CraftItemStack.asNMSCopy(item);
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
  public SpawnEgg1_12 clone() {
    return new SpawnEgg1_12(this.type);
  }
}