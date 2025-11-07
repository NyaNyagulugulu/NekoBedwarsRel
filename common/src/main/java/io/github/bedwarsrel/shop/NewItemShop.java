package io.github.bedwarsrel.shop;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.Game;
import io.github.bedwarsrel.game.PlayerSettings;
import io.github.bedwarsrel.utils.ChatWriter;
import io.github.bedwarsrel.utils.SoundMachine;
import io.github.bedwarsrel.utils.Utils;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.VillagerTrade;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class NewItemShop {

  private List<MerchantCategory> categories = null;
  private MerchantCategory currentCategory = null;

  public NewItemShop(List<MerchantCategory> categories) {
    this.categories = categories;
  }

  @SuppressWarnings("deprecation")
  private void addCategoriesToInventory(Inventory inventory, Player player, Game game) {
    for (MerchantCategory category : this.categories) {

      if (category.getMaterial() == null) {
        BedwarsRel.getInstance().getServer().getConsoleSender()
            .sendMessage(ChatWriter.pluginMessage(ChatColor.RED
                + "Careful: Not supported material in shop category '" + category.getName() + "'"));
        continue;
      }

      if (player != null && !player.hasPermission(category.getPermission())) {
        continue;
      }

      ItemStack is = new ItemStack(category.getMaterial(), 1);
      ItemMeta im = is.getItemMeta();

      if (Utils.isColorable(is)) {
        is.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
      }
      if (this.currentCategory != null && this.currentCategory.equals(category)) {
        im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
      }

      im.setDisplayName(category.getName());
      im.setLore(category.getLores());
      im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
      is.setItemMeta(im);

      inventory.addItem(is);
    }

  }

  @SuppressWarnings("unchecked")

  private boolean buyItem(VillagerTrade trade, ItemStack item, Player player) {

    PlayerInventory inventory = player.getInventory();

    boolean success = true;



    int item1ToPay = trade.getItem1().getAmount();

    Iterator<?> stackIterator = inventory.all(trade.getItem1().getType()).entrySet().iterator();



    int firstItem1 = inventory.first(trade.getItem1());

    if (firstItem1 > -1) {

      inventory.clear(firstItem1);

    } else {

      // pay

      while (stackIterator.hasNext()) {

        Entry<Integer, ? extends ItemStack> entry =

            (Entry<Integer, ? extends ItemStack>) stackIterator.next();

        ItemStack stack = (ItemStack) entry.getValue();



        int endAmount = stack.getAmount() - item1ToPay;

        if (endAmount < 0) {

          endAmount = 0;

        }



        item1ToPay = item1ToPay - stack.getAmount();

        stack.setAmount(endAmount);

        inventory.setItem(entry.getKey(), stack);



        if (item1ToPay <= 0) {

          break;

        }

      }

    }



    if (trade.getItem2() != null) {

      int item2ToPay = trade.getItem2().getAmount();

      stackIterator = inventory.all(trade.getItem2().getType()).entrySet().iterator();



      int firstItem2 = inventory.first(trade.getItem2());

      if (firstItem2 > -1) {

        inventory.clear(firstItem2);

      } else {

        // pay item2

        while (stackIterator.hasNext()) {

          Entry<Integer, ? extends ItemStack> entry =

              (Entry<Integer, ? extends ItemStack>) stackIterator.next();

          ItemStack stack = (ItemStack) entry.getValue();



          int endAmount = stack.getAmount() - item2ToPay;

          if (endAmount < 0) {

            endAmount = 0;

          }



          item2ToPay = item2ToPay - stack.getAmount();

          stack.setAmount(endAmount);

          inventory.setItem(entry.getKey(), stack);



          if (item2ToPay <= 0) {

            break;

          }

        }

      }

    }



    ItemStack addingItem = item.clone();

    ItemMeta meta = addingItem.getItemMeta();

    List<String> lore = meta.getLore();



    if (lore.size() > 0) {

      lore.remove(lore.size() - 1);

      if (trade.getItem2() != null) {

        lore.remove(lore.size() - 1);

      }

    }



    meta.setLore(lore);

    addingItem.setItemMeta(meta);



    HashMap<Integer, ItemStack> notStored = inventory.addItem(addingItem);

    if (notStored.size() > 0) {

      ItemStack notAddedItem = notStored.get(0);

      int removingAmount = addingItem.getAmount() - notAddedItem.getAmount();

      addingItem.setAmount(removingAmount);

      inventory.removeItem(addingItem);



      // restore

      inventory.addItem(trade.getItem1());

      if (trade.getItem2() != null) {

        inventory.addItem(trade.getItem2());

      }



      success = false;

    }



    player.updateInventory();

    return success;

  }

  private void changeToOldShop(Game game, Player player) {
    player.sendMessage("旧版商店已被移除");
    player.closeInventory();
  }

  private int getBuyInventorySize(int sizeCategories, int sizeOffers) {
    return this.getInventorySize(sizeCategories) + this.getInventorySize(sizeOffers);
  }

  public List<MerchantCategory> getCategories() {
    return this.categories;
  }

  private int getCategoriesSize(Player player) {
    int size = 0;
    for (MerchantCategory cat : this.categories) {
      if (cat.getMaterial() == null) {
        continue;
      }

      if (player != null && !player.hasPermission(cat.getPermission())) {
        continue;
      }

      size++;
    }

    return size;
  }

  private MerchantCategory getCategoryByMaterial(Material material) {
    for (MerchantCategory category : this.categories) {
      if (category.getMaterial() == material) {
        return category;
      }
    }

    return null;
  }

  private int getInventorySize(int itemAmount) {
    int nom = (itemAmount % 9 == 0) ? 9 : (itemAmount % 9);
    return itemAmount + (9 - nom);
  }

  private VillagerTrade getTradingItem(MerchantCategory category, ItemStack stack, Game game,
      Player player) {
    for (VillagerTrade trade : category.getOffers()) {
      if (trade.getItem1().getType() == Material.AIR
          && trade.getRewardItem().getType() == Material.AIR) {
        continue;
      }
      ItemStack iStack = this.toItemStack(trade, player, game);
      if (iStack.getType() == Material.ENDER_CHEST && stack.getType() == Material.ENDER_CHEST) {
        return trade;
      } else if ((iStack.getType() == Material.POTION
          || (!BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")
          && (iStack.getType().equals(Material.valueOf("TIPPED_ARROW"))
          || iStack.getType().equals(Material.valueOf("LINGERING_POTION"))
          || iStack.getType().equals(Material.valueOf("SPLASH_POTION")))))) {
        if (BedwarsRel.getInstance().getCurrentVersion().startsWith("v1_8")) {
          if (iStack.getItemMeta().equals(stack.getItemMeta())) {
            return trade;
          }
        } else {
          PotionMeta iStackMeta = (PotionMeta) iStack.getItemMeta();
          PotionMeta stackMeta = (PotionMeta) stack.getItemMeta();
          if (iStackMeta.getBasePotionData().equals(stackMeta.getBasePotionData()) && iStackMeta
              .getCustomEffects().equals(stackMeta.getCustomEffects())) {
            return trade;
          }
        }
      } else if (iStack.equals(stack)) {
        return trade;
      }
    }

    return null;
  }

  private void handleBuyInventoryClick(InventoryClickEvent ice, Game game, Player player) {



    int sizeCategories = this.getCategoriesSize(player);

    List<VillagerTrade> offers = this.currentCategory.getOffers();

    int sizeItems = offers.size();

    int totalSize = this.getBuyInventorySize(sizeCategories, sizeItems);


    ItemStack item = ice.getCurrentItem();
    boolean cancel = false;
    int bought = 0;
    boolean oneStackPerShift = false; // 由于相关的配置和方法已被移除，设为false



    if (this.currentCategory == null) {

      player.closeInventory();

      return;

    }



    if (ice.getRawSlot() < sizeCategories) {

      // is category click

      ice.setCancelled(true);


      if (item == null) {

        return;

      }



      if (item.getType().equals(this.currentCategory.getMaterial())) {
        // 不返回分类选择界面，保持当前分类打开
        // this.currentCategory = null;
        // this.openCategoryInventory(player);
      } else {
        // 打开点击的分类购买界面
        this.handleCategoryInventoryClick(ice, game, player);
      }

    } else {
      // 检查是否点击了分隔行（灰色玻璃行）
      // 分隔行开始于分类区域之后，共9个槽位
      int categoryRows = (sizeCategories + 8) / 9; // 分类占用的行数
      int separatorStart = categoryRows * 9; // 分隔行开始位置
      int separatorEnd = separatorStart + 9; // 分隔行结束位置（不包含）

      if (ice.getRawSlot() >= separatorStart && ice.getRawSlot() < separatorEnd) {
        // 点击了分隔行，取消操作，不执行任何功能
        ice.setCancelled(true);
        return;
      }

      // 检查是否点击了快捷购买区域（第4行，槽位36-44）
      int quickBuyStartSlot = 36; // 第4行开始
      int quickBuyEndSlot = 44;   // 第4行结束
      if (ice.getRawSlot() >= quickBuyStartSlot && ice.getRawSlot() <= quickBuyEndSlot) {
        // 点击了快捷购买区域
        int slotIndex = ice.getRawSlot() - quickBuyStartSlot; // 获取槽位索引 (0-8)
        PlayerSettings playerSettings = game.getPlayerSettings(player);
        String itemIdentifier = playerSettings.getQuickBuyItem(slotIndex);
        if (ice.isShiftClick()) {
          // Shift点击快捷购买槽位，移除该槽位设置
          playerSettings.setQuickBuyItem(slotIndex, null); // 清空该槽位
          // 更新界面显示为灰色玻璃
          ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // 灰色玻璃
          ItemMeta meta = glassPane.getItemMeta();
          List<String> lore = new ArrayList<String>();
          lore.add(ChatColor.GRAY + "按住Shift点击物品设置");
          meta.setLore(lore);
          meta.setDisplayName(" ");
          glassPane.setItemMeta(meta);
          ice.getInventory().setItem(ice.getRawSlot(), glassPane);
          player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "已移除快捷购买栏第" + (slotIndex + 1) + "个位置的物品"));
        } else if (itemIdentifier != null && !itemIdentifier.isEmpty()) {
          // 如果该槽位有设置的物品，尝试购买该物品
          ItemStack quickBuyItem = this.getItemFromIdentifier(itemIdentifier, player, game);
          if (quickBuyItem != null) {
            // 找到对应的分类和交易
            String[] parts = itemIdentifier.split("_");
            if (parts.length == 2) {
              String categoryName = parts[0];
              int tradeIndex;
              try {
                tradeIndex = Integer.parseInt(parts[1]);
              } catch (NumberFormatException e) {
                return;
              }

              // 查找分类
              MerchantCategory category = null;
              for (MerchantCategory cat : this.categories) {
                if (cat.getName().equals(categoryName)) {
                  category = cat;
                  break;
                }
              }

              if (category != null && tradeIndex >= 0 && tradeIndex < category.getOffers().size()) {
                VillagerTrade trade = category.getOffers().get(tradeIndex);
                if (trade != null && this.hasEnoughRessource(player, trade)) {
                  boolean success = this.buyItem(trade, quickBuyItem, player);
                  if (success) {
                    // 发送购买成功消息
                    ItemMeta meta = quickBuyItem.getItemMeta();
                    String itemName;
                    if (meta.hasDisplayName()) {
                      itemName = meta.getDisplayName();
                    } else {
                      // 如果没有自定义名称，使用物品类型名称并格式化
                      itemName = this.getItemDisplayName(quickBuyItem.getType());
                    }
                    player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "你购买了" + ChatColor.GOLD + itemName + ChatColor.GREEN + "喵!"));
                  }
                } else {
                  player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "你没有足够的资源购买这个物品!"));
                }
              }
            }
          }
        } else {
          // 点击了空的快捷购买槽位，不执行任何操作，但也不显示错误消息
          // 这允许玩家以后通过Shift点击商店物品来设置这个槽位
        }
        ice.setCancelled(true);
        return;
      }

      // 在新的布局中，物品区域在灰色分隔行之后和快捷购买行之后，需要重新判断物品槽位
      // 计算分类区域所占的行数
      int categoryRowsCal = (sizeCategories + 8) / 9; // 分类占用的行数（向上取整）
      int separatorStartCal = categoryRowsCal * 9; // 分隔行开始位置
      int itemStartSlot = separatorStartCal + 9; // 物品区域开始位置（跳过分隔行）

      // 如果点击的是物品区域
      if (ice.getRawSlot() >= itemStartSlot && ice.getRawSlot() < 36 // 36是快捷购买行的起始，物品区域不能覆盖快捷购买行
          && ice.getRawSlot() < 54 && item != null && item.getType() != Material.AIR) {
        // 这是一个购买物品的点击

        ice.setCancelled(true);


        if (item == null || item.getType() == Material.AIR) {

          return;

        }



        MerchantCategory category = this.currentCategory;

        VillagerTrade trade = this.getTradingItem(category, item, game, player);


        if (trade == null) {

          return;

        }



        player.playSound(player.getLocation(), SoundMachine.get("ITEM_PICKUP", "ENTITY_ITEM_PICKUP"),

            Float.valueOf("1.0"), Float.valueOf("1.0"));


        // 如果按住shift键，设置为快捷购买
        if (ice.isShiftClick()) {
          // 计算该交易在分类中的索引
          int tradeIndex = -1;
          for (int i = 0; i < category.getOffers().size(); i++) {
            if (category.getOffers().get(i).equals(trade)) {
              tradeIndex = i;
              break;
            }
          }

          if (tradeIndex != -1) {
            // 生成物品标识符
            String itemIdentifier = this.generateItemIdentifier(category, tradeIndex);
            // 获取玩家未设置快捷购买的第一个槽位
            PlayerSettings playerSettings = game.getPlayerSettings(player);
            int emptySlot = -1;
            for (int i = 0; i < 9; i++) {
              if (playerSettings.getQuickBuyItem(i) == null || playerSettings.getQuickBuyItem(i).isEmpty()) {
                emptySlot = i;
                break;
              }
            }

            if (emptySlot != -1) {
              // 设置快捷购买
              playerSettings.setQuickBuyItem(emptySlot, itemIdentifier);
              // 更新界面显示
              ItemStack quickBuyItem = this.toItemStack(trade, player, game);
              int quickBuySlot = 36 + emptySlot; // 快捷购买行的槽位
              ice.getInventory().setItem(quickBuySlot, quickBuyItem);
              player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "已将物品设置到快捷购买栏第" + (emptySlot + 1) + "个位置"));
            } else {
              // 如果所有槽位都已占用，询问玩家是否替换某个槽位
              player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + "所有快捷购买栏位都已占用，请Shift点击一个快捷购买槽位来替换"));
            }
          }
        } else {
          // 购买物品
          // enough ressources?

          if (!this.hasEnoughRessource(player, trade)) {

            player

                .sendMessage(

                    ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel

                        ._l(player, "errors.notenoughress")));

            return;

          }



          // 处理购买逻辑，移除了shift购买一组物品的功能
          boolean success = this.buyItem(trade, ice.getCurrentItem(), player);
          if (success) {
            // 发送购买成功消息
            ItemStack itemToBuy = this.toItemStack(trade, player, game);
            ItemMeta meta = itemToBuy.getItemMeta();
            String itemName;
            if (meta.hasDisplayName()) {
              itemName = meta.getDisplayName();
            } else {
              // 如果没有自定义名称，使用物品类型名称并格式化
              itemName = this.getItemDisplayName(itemToBuy.getType());
            }
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.GREEN + "你购买了" + ChatColor.GOLD + itemName + ChatColor.GREEN + "喵!"));
          }
        }

      } else {

        // 对于不属于分类、分隔符或物品区域的点击（如空槽位），也需要取消操作
        ice.setCancelled(true);

        return;

      }

    }

  }

  private void handleCategoryInventoryClick(InventoryClickEvent ice, Game game, Player player) {



    int catSize = this.getCategoriesSize(player);

    int sizeCategories = this.getInventorySize(catSize) + 9;

    int rawSlot = ice.getRawSlot();


    if (rawSlot >= this.getInventorySize(catSize) && rawSlot < sizeCategories) {

      ice.setCancelled(true);

      if (ice.getCurrentItem().getType() == Material.SLIME_BALL) {

        this.changeToOldShop(game, player);

        return;

      }

    }



    if (rawSlot >= sizeCategories) {

      ice.setCancelled(false);

      return;

    }



    MerchantCategory clickedCategory = this.getCategoryByMaterial(ice.getCurrentItem().getType());

    if (clickedCategory == null) {

      ice.setCancelled(false);

      return;

    }



    this.openBuyInventory(clickedCategory, player, game);

  }

  public void handleInventoryClick(InventoryClickEvent ice, Game game, Player player) {
    if (!this.hasOpenCategory()) {
      this.handleCategoryInventoryClick(ice, game, player);
    } else {
      this.handleBuyInventoryClick(ice, game, player);
    }
  }

  private boolean hasEnoughRessource(Player player, VillagerTrade trade) {
    ItemStack item1 = trade.getItem1();
    ItemStack item2 = trade.getItem2();
    PlayerInventory inventory = player.getInventory();

    if (item2 != null) {
      if (!inventory.contains(item1.getType(), item1.getAmount())
          || !inventory.contains(item2.getType(), item2.getAmount())) {
        return false;
      }
    } else {
      if (!inventory.contains(item1.getType(), item1.getAmount())) {
        return false;
      }
    }

    return true;
  }

  public boolean hasOpenCategory() {
    return (this.currentCategory != null);
  }

  public boolean hasOpenCategory(MerchantCategory category) {
    if (this.currentCategory == null) {
      return false;
    }

    return (this.currentCategory.equals(category));
  }

  public void openBuyInventory(MerchantCategory category, Player player, Game game) {

    List<VillagerTrade> offers = category.getOffers();
    int sizeCategories = this.getCategoriesSize(player);

    // 固定使用6行界面 (54个槽位)
    int invSize = 54;

    player.playSound(player.getLocation(), SoundMachine.get("CLICK", "UI_BUTTON_CLICK"),
        Float.valueOf("1.0"), Float.valueOf("1.0"));

    this.currentCategory = category;
    Inventory buyInventory = Bukkit
        .createInventory(player, invSize, BedwarsRel._l(player, "ingame.shop.name"));
    this.addCategoriesToInventory(buyInventory, player, game);

    // 计算分类占用的行数（向上取整）
    int categoryRows = (sizeCategories + 8) / 9; // 每行最多9个
    // 分类区域结束位置
    int categoryEndSlot = categoryRows * 9;

    // 添加快捷购买区域（第4行，槽位36-44）
    int quickBuyRowStart = 36; // 第4行开始位置
    PlayerSettings playerSettings = game.getPlayerSettings(player);
    for (int i = 0; i < 9; i++) {
      String itemIdentifier = playerSettings.getQuickBuyItem(i);
      if (itemIdentifier != null && !itemIdentifier.isEmpty()) {
        // 如果有设置的物品，显示该物品
        ItemStack quickBuyItem = this.getItemFromIdentifier(itemIdentifier, player, game);
        if (quickBuyItem != null) {
          buyInventory.setItem(quickBuyRowStart + i, quickBuyItem);
        } else {
          // 如果物品不存在，显示灰色玻璃
          ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // 灰色玻璃
          ItemMeta meta = glassPane.getItemMeta();
          List<String> lore = new ArrayList<String>();
          lore.add(ChatColor.RED + "物品不存在");
          meta.setLore(lore);
          meta.setDisplayName(" ");
          glassPane.setItemMeta(meta);
          buyInventory.setItem(quickBuyRowStart + i, glassPane);
        }
      } else {
        // 如果没有设置物品，显示灰色玻璃并添加提示Lore
        ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // 灰色玻璃
        ItemMeta meta = glassPane.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "按住Shift点击物品设置");
        meta.setLore(lore);
        meta.setDisplayName(" ");
        glassPane.setItemMeta(meta);
        buyInventory.setItem(quickBuyRowStart + i, glassPane);
      }
    }

    // 在类别和物品之间添加一排玻璃作为分隔符
    int separatorRowStart = categoryEndSlot; // 分隔符行的起始槽位
    for (int i = 0; i < 9; i++) {
      // 默认使用灰色玻璃
      ItemStack glassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7); // 灰色玻璃
      
      ItemMeta meta = glassPane.getItemMeta();
      
      // 显示类别和物品标识，分行显示（类别在上，物品在下）
      List<String> lore = new ArrayList<String>();
      lore.add(ChatColor.DARK_GRAY + "⬆ " + ChatColor.GRAY + "类别");
      lore.add(ChatColor.DARK_GRAY + "⬇ " + ChatColor.GRAY + "物品");
      meta.setLore(lore);
      meta.setDisplayName(" "); // 空白名称
      
      glassPane.setItemMeta(meta);
      buyInventory.setItem(separatorRowStart + i, glassPane);
    }

    // 如果当前有选中的分类，找到其在界面中的位置并将其下方的玻璃改为绿色
    if (this.currentCategory != null && categoryRows > 0) {
      // 找到当前分类在界面中的位置（在分类区域中的索引）
      int categoryIndex = -1;
      int tempIndex = 0;
      for (MerchantCategory categoryCheck : this.categories) {
        if (categoryCheck.getMaterial() == null || (player != null && !player.hasPermission(categoryCheck.getPermission()))) {
          continue;
        }

        if (categoryCheck.equals(this.currentCategory)) {
          categoryIndex = tempIndex;
          break;
        }
        tempIndex++;
      }

      // 如果找到了当前分类的位置，将其下方的玻璃改为绿色
      if (categoryIndex >= 0) {
        int glassPosition = categoryEndSlot + (categoryIndex % 9); // 当前分类下方的槽位
        if (glassPosition < 54) { // 确保索引在有效范围内
          ItemStack greenGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5); // 绿色玻璃
          ItemMeta meta = greenGlass.getItemMeta();
          List<String> lore = new ArrayList<String>();
          lore.add(ChatColor.DARK_GRAY + "⬆ " + ChatColor.GRAY + "类别");
          lore.add(ChatColor.DARK_GRAY + "⬇ " + ChatColor.GRAY + "物品");
          meta.setLore(lore);
          meta.setDisplayName(" "); // 空白名称
          greenGlass.setItemMeta(meta);
          buyInventory.setItem(glassPosition, greenGlass);
        }
      }
    }



    // 添加物品到剩余空间（在分隔符之后，但不能覆盖快捷购买行）
    for (int i = 0; i < offers.size(); i++) {
      VillagerTrade trade = offers.get(i);
      if (trade.getItem1().getType() == Material.AIR
          && trade.getRewardItem().getType() == Material.AIR) {
        continue;
      }

      // 计算物品槽位：从分隔符行之后开始，但跳过快捷购买行（36-44）
      int itemSlot = separatorRowStart + 9 + i; // +9 是跳过分隔符行
      // 如果物品槽位与快捷购买行冲突，需要跳过
      if (itemSlot >= 36 && itemSlot <= 44) {
        // 如果物品槽位在快捷购买行范围内，移动到下一个可用位置
        itemSlot = 45 + i; // 从第5行开始继续放置
      }

      // 确保物品不会覆盖分隔符或超出界面范围，并且不覆盖快捷购买行
      if (itemSlot < invSize && (itemSlot < 36 || itemSlot > 44)) { // 确保不覆盖快捷购买行
        ItemStack tradeStack = this.toItemStack(trade, player, game);
        buyInventory.setItem(itemSlot, tradeStack);
      }
    }

    player.openInventory(buyInventory);
  }

  public void openCategoryInventory(Player player) {

    int catSize = this.getCategoriesSize(player);

    int nom = (catSize % 9 == 0) ? 9 : (catSize % 9);

    int size = (catSize + (9 - nom)) + 9;



    Inventory inventory = Bukkit.createInventory(player, size, BedwarsRel

        ._l(player, "ingame.shop.name"));



    Game game = BedwarsRel.getInstance().getGameManager().getGameOfPlayer(player);



    this.addCategoriesToInventory(inventory, player, game);



    ItemStack slime = new ItemStack(Material.SLIME_BALL, 1);

    ItemMeta slimeMeta = slime.getItemMeta();


    slimeMeta.setDisplayName(BedwarsRel._l(player, "ingame.shop.oldshop"));

    slimeMeta.setLore(new ArrayList<String>());

    slime.setItemMeta(slimeMeta);


    inventory.setItem(size - 5, slime);


    player.openInventory(inventory);

  }

  public void setCurrentCategory(MerchantCategory category) {
    this.currentCategory = category;
  }

  @SuppressWarnings("deprecation")
  private ItemStack toItemStack(VillagerTrade trade, Player player, Game game) {
    ItemStack tradeStack = trade.getRewardItem().clone();
    Method colorable = Utils.getColorableMethod(tradeStack.getType());
    ItemMeta meta = tradeStack.getItemMeta();
    ItemStack item1 = trade.getItem1();
    ItemStack item2 = trade.getItem2();
    if (Utils.isColorable(tradeStack)) {
      tradeStack.setDurability(game.getPlayerTeam(player).getColor().getDyeColor().getWoolData());
    } else if (colorable != null) {
      colorable.setAccessible(true);
      try {
        colorable.invoke(meta, new Object[]{game.getPlayerTeam(player).getColor().getColor()});
      } catch (Exception e) {
        BedwarsRel.getInstance().getBugsnag().notify(e);
        e.printStackTrace();
      }
    }
    List<String> lores = meta.getLore();
    if (lores == null) {
      lores = new ArrayList<String>();
    }

    lores.add(ChatColor.WHITE + String.valueOf(item1.getAmount()) + " "
        + item1.getItemMeta().getDisplayName());
    if (item2 != null) {
      lores.add(ChatColor.WHITE + String.valueOf(item2.getAmount()) + " "
          + item2.getItemMeta().getDisplayName());
    }

    meta.setLore(lores);
    tradeStack.setItemMeta(meta);
    return tradeStack;
  }

  private String getItemDisplayName(Material material) {
    // 创建一个基础的物品名称映射，包含常用的Minecraft物品中文名称
    switch (material) {
      case DIAMOND: return "钻石";
      case IRON_INGOT: return "铁锭";
      case GOLD_INGOT: return "金锭";
      case COBBLESTONE: return "圆石";
      case WOOL: return "羊毛";
      case LOG: return "原木";
      case WOOD: return "木板";
      case SANDSTONE: return "砂岩";
      case STONE: return "石头";
      case DIRT: return "泥土";
      case CLAY: return "粘土";
      case GLASS: return "玻璃";
      case COAL: return "煤炭";
      case EMERALD: return "绿宝石";
      case BOW: return "弓";
      case ARROW: return "箭";
      case STONE_SWORD: return "石剑";
      case IRON_SWORD: return "铁剑";
      case DIAMOND_SWORD: return "钻石剑";
      case STONE_AXE: return "石斧";
      case IRON_AXE: return "铁斧";
      case DIAMOND_AXE: return "钻石斧";
      case STONE_PICKAXE: return "石镐";
      case IRON_PICKAXE: return "铁镐";
      case DIAMOND_PICKAXE: return "钻石镐";
      case STONE_SPADE: return "石锹";
      case IRON_SPADE: return "铁锹";
      case DIAMOND_SPADE: return "钻石锹";
      case LEATHER_CHESTPLATE: return "皮革胸甲";
      case CHAINMAIL_CHESTPLATE: return "锁链胸甲";
      case IRON_CHESTPLATE: return "铁胸甲";
      case DIAMOND_CHESTPLATE: return "钻石胸甲";
      case LEATHER_LEGGINGS: return "皮革护腿";
      case CHAINMAIL_LEGGINGS: return "锁链护腿";
      case IRON_LEGGINGS: return "铁护腿";
      case DIAMOND_LEGGINGS: return "钻石护腿";
      case LEATHER_BOOTS: return "皮革靴子";
      case CHAINMAIL_BOOTS: return "锁链靴子";
      case IRON_BOOTS: return "铁靴子";
      case DIAMOND_BOOTS: return "钻石靴子";
      case LEATHER_HELMET: return "皮革头盔";
      case CHAINMAIL_HELMET: return "锁链头盔";
      case IRON_HELMET: return "铁头盔";
      case DIAMOND_HELMET: return "钻石头盔";
      case WATER_BUCKET: return "水桶";
      case LAVA_BUCKET: return "岩浆桶";
      case BUCKET: return "桶";
      case ENDER_PEARL: return "末影珍珠";
      case SNOW_BALL: return "雪球";
      case EGG: return "鸡蛋";
      case APPLE: return "苹果";
      case BREAD: return "面包";
      case COOKED_BEEF: return "熟牛肉";
      case COOKED_CHICKEN: return "熟鸡肉";
      case MUSHROOM_SOUP: return "蘑菇汤";
      case POTION: return "药水";
      case SADDLE: return "鞍";
      case TNT: return "TNT";
      case REDSTONE: return "红石粉";
      case LADDER: return "梯子";
      case TORCH: return "火把";
      case CHEST: return "箱子";
      case FENCE: return "栅栏";
      case WORKBENCH: return "工作台";
      case FURNACE: return "熔炉";
      case ENCHANTMENT_TABLE: return "附魔台";
      case ANVIL: return "铁砧";
      case BOOK: return "书";
      case BOOK_AND_QUILL: return "书与笔";
      case WRITTEN_BOOK: return "成书";
      case COMPASS: return "指南针";
      case WATCH: return "时钟";
      case MONSTER_EGG: return "刷怪蛋";
      default:
        // 如果没有找到对应的中文名称，则使用英文名称格式
        String name = material.name();
        name = name.toLowerCase().replace('_', ' ');
        String[] parts = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
          if (i > 0) sb.append(" ");
          if (parts[i].length() > 0) {
            sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
          }
        }
        return sb.toString();
    }
  }

  /**
   * 从物品标识符获取物品
   * @param itemIdentifier 物品标识符
   * @param player 玩家
   * @param game 游戏
   * @return 物品堆栈
   */
  private ItemStack getItemFromIdentifier(String itemIdentifier, Player player, Game game) {
    // 解析物品标识符格式，例如 "categoryName_tradeIndex"，如 "swords_0" 表示swords分类的第0个物品
    String[] parts = itemIdentifier.split("_");
    if (parts.length != 2) {
      return null;
    }

    String categoryName = parts[0];
    int tradeIndex;
    try {
      tradeIndex = Integer.parseInt(parts[1]);
    } catch (NumberFormatException e) {
      return null;
    }

    // 查找对应分类
    MerchantCategory category = null;
    for (MerchantCategory cat : this.categories) {
      if (cat.getName().equals(categoryName)) {
        category = cat;
        break;
      }
    }

    if (category == null) {
      return null;
    }

    // 获取对应索引的交易
    List<VillagerTrade> offers = category.getOffers();
    if (tradeIndex < 0 || tradeIndex >= offers.size()) {
      return null;
    }

    VillagerTrade trade = offers.get(tradeIndex);
    return this.toItemStack(trade, player, game);
  }

  /**
   * 生成物品标识符
   * @param category 分类
   * @param tradeIndex 交易索引
   * @return 物品标识符
   */
  private String generateItemIdentifier(MerchantCategory category, int tradeIndex) {
    return category.getName() + "_" + tradeIndex;
  }

}