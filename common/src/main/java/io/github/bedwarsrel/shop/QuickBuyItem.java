package io.github.bedwarsrel.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class QuickBuyItem implements ConfigurationSerializable {
    
    private int slot;
    private String categoryName;
    private int itemIndex;
    
    public QuickBuyItem(int slot, String categoryName, int itemIndex) {
        this.slot = slot;
        this.categoryName = categoryName;
        this.itemIndex = itemIndex;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("slot", slot);
        result.put("categoryName", categoryName);
        result.put("itemIndex", itemIndex);
        return result;
    }
    
    public static QuickBuyItem deserialize(Map<String, Object> args) {
        if (!args.containsKey("slot") || !args.containsKey("categoryName") || !args.containsKey("itemIndex")) {
            return null;
        }
        
        int slot = (int) args.get("slot");
        String categoryName = (String) args.get("categoryName");
        int itemIndex = (int) args.get("itemIndex");
        
        return new QuickBuyItem(slot, categoryName, itemIndex);
    }
}