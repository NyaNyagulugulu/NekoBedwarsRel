package io.github.bedwarsrel.database;

import io.github.bedwarsrel.BedwarsRel;
import io.github.bedwarsrel.game.PlayerSettings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QuickBuySettingsManager {

  private DatabaseManager databaseManager;

  public QuickBuySettingsManager(DatabaseManager databaseManager) {
    this.databaseManager = databaseManager;
    initializeTable();
  }

  private void initializeTable() {
    try (Connection connection = this.databaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "CREATE TABLE IF NOT EXISTS `" + this.databaseManager.getTablePrefix()
                + "quick_buy_settings` (`uuid` varchar(255) NOT NULL, `quick_buy_settings` text, PRIMARY KEY (`uuid`))")) {
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void saveQuickBuySettings(UUID uuid, List<String> quickBuySettings) {
    String settingsString = String.join(",", quickBuySettings);
    try (Connection connection = this.databaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO `" + this.databaseManager.getTablePrefix()
                + "quick_buy_settings` (uuid, quick_buy_settings) VALUES (?, ?) ON DUPLICATE KEY UPDATE quick_buy_settings = VALUES(quick_buy_settings)")) {
      statement.setString(1, uuid.toString());
      statement.setString(2, settingsString);
      statement.executeUpdate();
    } catch (SQLException e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }
  }

  public List<String> loadQuickBuySettings(UUID uuid) {
    List<String> quickBuySettings = new ArrayList<>();
    // 初始化9个槽位为null
    for (int i = 0; i < 9; i++) {
      quickBuySettings.add(null);
    }

    try (Connection connection = this.databaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "SELECT quick_buy_settings FROM `" + this.databaseManager.getTablePrefix()
                + "quick_buy_settings` WHERE uuid = ?")) {
      statement.setString(1, uuid.toString());
      ResultSet resultSet = statement.executeQuery();

      if (resultSet.next()) {
        String settingsString = resultSet.getString("quick_buy_settings");
        if (settingsString != null && !settingsString.isEmpty()) {
          String[] settingsArray = settingsString.split(",");
          for (int i = 0; i < Math.min(settingsArray.length, 9); i++) {
            // 如果是空字符串，转换为null
            quickBuySettings.set(i, settingsArray[i].isEmpty() ? null : settingsArray[i]);
          }
        }
      }
    } catch (SQLException e) {
      BedwarsRel.getInstance().getBugsnag().notify(e);
      e.printStackTrace();
    }

    return quickBuySettings;
  }
}