package io.github.bedwarsrel.shop;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class PlayerQuickBuy implements ConfigurationSerializable {
    
    private UUID playerUUID;
    private List<QuickBuyItem> items;
    
    public PlayerQuickBuy(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.items = new ArrayList<>();
    }
    
    public PlayerQuickBuy(UUID playerUUID, List<QuickBuyItem> items) {
        this.playerUUID = playerUUID;
        this.items = items != null ? items : new ArrayList<>();
    }
    
    public void addItem(QuickBuyItem item) {
        // 检查是否已存在相同的物品
        for (int i = 0; i < items.size(); i++) {
            QuickBuyItem existingItem = items.get(i);
            if (existingItem.getSlot() == item.getSlot()) {
                items.set(i, item);
                return;
            }
        }
        items.add(item);
    }
    
    public void removeItem(int slot) {
        items.removeIf(item -> item.getSlot() == slot);
    }
    
    public QuickBuyItem getItem(int slot) {
        for (QuickBuyItem item : items) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }
    
    public boolean hasItem(int slot) {
        return getItem(slot) != null;
    }
    
    @Override
    public java.util.Map<String, Object> serialize() {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("playerUUID", playerUUID.toString());
        
        List<java.util.Map<String, Object>> serializedItems = new ArrayList<>();
        for (QuickBuyItem item : items) {
            serializedItems.add(item.serialize());
        }
        result.put("items", serializedItems);
        
        return result;
    }
    
    public static PlayerQuickBuy deserialize(java.util.Map<String, Object> args) {
        UUID playerUUID = UUID.fromString((String) args.get("playerUUID"));
        
        List<QuickBuyItem> items = new ArrayList<>();
        if (args.containsKey("items") && args.get("items") instanceof List) {
            List<?> itemList = (List<?>) args.get("items");
            for (Object itemObj : itemList) {
                if (itemObj instanceof java.util.Map) {
                    QuickBuyItem item = QuickBuyItem.deserialize((java.util.Map<String, Object>) itemObj);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }
        }
        
        return new PlayerQuickBuy(playerUUID, items);
    }
}