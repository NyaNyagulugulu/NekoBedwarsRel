package io.github.bedwarsrel.com.v1_12_r1;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;
import net.minecraft.server.v1_12_R1.RegistryMaterials;

@SuppressWarnings("rawtypes")
public class CustomEntityRegistry extends RegistryMaterials {

  private static CustomEntityRegistry instance = null;

  private final BiMap<MinecraftKey, Class<? extends Entity>> customEntities = HashBiMap.create();
  private final BiMap<Class<? extends Entity>, MinecraftKey> customEntityClasses =
      this.customEntities.inverse();
  private final Map<Class<? extends Entity>, Integer> customEntityIds = new HashMap<>();

  private final RegistryMaterials wrapped;

  private CustomEntityRegistry(RegistryMaterials original) {
    this.wrapped = original;
  }

  public static void addCustomEntity(int entityId, String entityName,
      Class<? extends Entity> entityClass) {
    getInstance().putCustomEntity(entityId, entityName, entityClass);
  }

  public static CustomEntityRegistry getInstance() {
    if (instance != null) {
      return instance;
    }

    instance = new CustomEntityRegistry(EntityTypes.b);

    try {
      // TODO: Update name on version change (RegistryMaterials)
      Field registryMaterialsField = EntityTypes.class.getDeclaredField("b");
      registryMaterialsField.setAccessible(true);

      // Use Unsafe to bypass modifier restrictions in Java 21+
      try {
        // Try the traditional approach first (works in some Java 21 setups)
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(registryMaterialsField,
            registryMaterialsField.getModifiers() & ~Modifier.FINAL);
      } catch (Exception e1) {
        // If traditional approach fails, try VarHandle (for newer Java versions)
        try {
          VarHandle fieldHandle = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup())
              .findVarHandle(Field.class, "modifiers", int.class);
          int modifiers = (int) fieldHandle.get(registryMaterialsField);
          fieldHandle.set(registryMaterialsField, modifiers & ~Modifier.FINAL);
        } catch (Exception e2) {
          // If both approaches fail, throw a more informative error
          throw new RuntimeException("Unable to modify field modifiers for RegistryMaterials. " +
              "This is required for custom entity registration in newer Java versions. " +
              "Ensure your server is started with proper --add-opens flags: " +
              "--add-opens java.base/java.lang=ALL-UNNAMED " +
              "--add-opens java.base/java.lang.reflect=ALL-UNNAMED " +
              "--add-opens java.base/java.lang.invoke=ALL-UNNAMED", e2);
        }
      }

      registryMaterialsField.set(null, instance);
    } catch (Exception e) {
      instance = null;

      throw new RuntimeException("Unable to override the old entity RegistryMaterials", e);
    }

    return instance;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int a(Object key) { // TODO: Update name on version change (getId)
    if (this.customEntityIds.containsKey(key)) {
      return this.customEntityIds.get(key);
    }

    return this.wrapped.a(key);
  }

  @SuppressWarnings("unchecked")
  @Override
  public MinecraftKey b(Object value) { // TODO: Update name on version change (getKey)
    if (this.customEntityClasses.containsKey(value)) {
      return this.customEntityClasses.get(value);
    }

    return (MinecraftKey) wrapped.b(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends Entity> get(Object key) {
    if (this.customEntities.containsKey(key)) {
      return this.customEntities.get(key);
    }

    return (Class<? extends Entity>) wrapped.get(key);
  }

  public void putCustomEntity(int entityId, String entityName,
      Class<? extends Entity> entityClass) {
    MinecraftKey minecraftKey = new MinecraftKey(entityName);

    this.customEntities.put(minecraftKey, entityClass);
    this.customEntityIds.put(entityClass, entityId);
  }
}
