package io.github.bedwarsrel.database;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.shop.PlayerQuickBuy;
import io.github.bedwarsrel.shop.QuickBuyItem;
import io.github.bedwarsrel.villager.MerchantCategory;
import io.github.bedwarsrel.villager.VillagerTrade;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class QuickBuyDatabaseManager {
    
    private DatabaseManager databaseManager;
    
    public QuickBuyDatabaseManager(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        initializeTable();
    }
    
    public void initializeTable() {
        try {
            Connection connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            
            // 创建快捷购买表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS `" + databaseManager.getTablePrefix()
                + "quick_buy` (`uuid` varchar(255) NOT NULL, `slot` int(11) NOT NULL, "
                + "`category_name` varchar(255) NOT NULL, `item_index` int(11) NOT NULL, "
                + "PRIMARY KEY (`uuid`, `slot`))";
            
            PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL);
            preparedStatement.executeUpdate();
            connection.commit();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public PlayerQuickBuy loadPlayerQuickBuy(UUID uuid) {
        List<QuickBuyItem> items = new ArrayList<>();
        
        try {
            Connection connection = databaseManager.getConnection();
            String sql = "SELECT * FROM " + databaseManager.getTablePrefix() 
                + "quick_buy WHERE uuid = ?";
            
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                int slot = resultSet.getInt("slot");
                String categoryName = resultSet.getString("category_name");
                int itemIndex = resultSet.getInt("item_index");
                
                items.add(new QuickBuyItem(slot, categoryName, itemIndex));
            }
            
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return new PlayerQuickBuy(uuid, items);
    }
    
    public void savePlayerQuickBuy(PlayerQuickBuy quickBuy) {
        try {
            Connection connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            
            // 删除玩家现有的快捷购买设置
            String deleteSQL = "DELETE FROM " + databaseManager.getTablePrefix() 
                + "quick_buy WHERE uuid = ?";
            PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
            deleteStatement.setString(1, quickBuy.getPlayerUUID().toString());
            deleteStatement.executeUpdate();
            
            // 插入新的快捷购买设置
            String insertSQL = "INSERT INTO " + databaseManager.getTablePrefix() 
                + "quick_buy(uuid, slot, category_name, item_index) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
            
            for (QuickBuyItem item : quickBuy.getItems()) {
                insertStatement.setString(1, quickBuy.getPlayerUUID().toString());
                insertStatement.setInt(2, item.getSlot());
                insertStatement.setString(3, item.getCategoryName());
                insertStatement.setInt(4, item.getItemIndex());
                
                insertStatement.addBatch();
            }
            
            insertStatement.executeBatch();
            connection.commit();
            
            insertStatement.close();
            deleteStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}