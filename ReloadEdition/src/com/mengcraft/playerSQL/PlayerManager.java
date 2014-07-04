package com.mengcraft.playerSQL;

import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class PlayerManager {

    private static HashMap<Player, PlayerData> playerMap;

    public static void setup() {
        playerMap = new HashMap<>();
    }

    public static void saveAll() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        if (players.length > 0) {
            try {
                String sql = "UPDATE PlayerSQL " +
                        "SET DATA = ? " +
                        "WHERE NAME = ?;";
                PreparedStatement statement = PlayerSQL.database.prepareStatement(sql);
                for (Player player : players) {
                    PlayerData playerData = PlayerManager.get(player);
                    String name = playerData.getName();
                    String data = playerData.getData();
                    statement.setString(1, data);
                    statement.setString(2, name);
                    statement.addBatch();
                }
                statement.executeBatch();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static PlayerData get(Player player) {
        PlayerData playerData = playerMap.get(player);
        if (playerData == null) {
            playerData = new PlayerData(player);
            playerMap.put(player, playerData);
        }
        return playerData;
    }

    public static void remove(Player player) {
        boolean containsKey = playerMap.containsKey(player);
        if (containsKey) playerMap.remove(player);
    }

    public static void clear() {
        playerMap.clear();
    }

}
