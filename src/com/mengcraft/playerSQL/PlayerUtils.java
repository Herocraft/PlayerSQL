package com.mengcraft.playerSQL;

import com.comphenix.protocol.utility.StreamSerializer;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerUtils {
    static String buildArmorDate(ItemStack[] itemStacks) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < itemStacks.length; i++) {
            if (i > 0) {
                stringBuilder.append(";");
            }
            if (itemStacks[i] != null) {
                try {
                    stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStacks[i]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    static String buildStacksData(ItemStack[] itemStacks) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < itemStacks.length; i++) {
            if (i > 0) {
                stringBuilder.append(";");
            }
            if (itemStacks[i] != null && itemStacks[i].getType() != Material.AIR) {
                try {
                    stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStacks[i]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

    static ItemStack[] restoreStacks(String string) {
        if (string != null) {
            String[] strings = string.split(";");
            ItemStack[] itemStacks = new ItemStack[strings.length];
            for (int i = 0; i < strings.length; i++) {
                if (!strings[i].equals("")) {
                    try {
                        itemStacks[i] = StreamSerializer.getDefault().deserializeItemStack(strings[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return itemStacks;
        } else return new ItemStack[]{new ItemStack(Material.AIR)};
    }

    public static void savePlayers() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        if (players.length > 0) try {
            String sql = "UPDATE PlayerSQL " + "SET " +
                    "Economy = ?, " +
                    "Health = ?, " +
                    "Food = ?, " +
                    "Level = ?, " +
                    "Armor = ?, " +
                    "Inventory = ?, " +
                    "EndChest = ? " +
                    "WHERE PlayerName = ?;";
            PreparedStatement statement = Database.connection.prepareStatement(sql);
            for (Player player : players) {
                double economy = PlayerSQL.economy != null ? PlayerSQL.economy.getBalance(player.getName()) : 0;
                ItemStack[] armorStacks = player.getInventory().getArmorContents();
                ItemStack[] inventoryStacks = player.getInventory().getContents();
                ItemStack[] endChestStacks = player.getEnderChest().getContents();
                statement.setDouble(1, economy);
                statement.setDouble(2, player.getHealth());
                statement.setInt(3, player.getFoodLevel());
                statement.setInt(4, player.getTotalExperience());
                statement.setString(5, buildArmorDate(armorStacks));
                statement.setString(6, buildStacksData(inventoryStacks));
                statement.setString(7, buildStacksData(endChestStacks));
                statement.setString(8, player.getName().toLowerCase());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(Player player) throws SQLException {

        double economy = PlayerSQL.economy != null ? PlayerSQL.economy.getBalance(player.getName()) : 0;

        PlayerInventory inventory = player.getInventory();
        Inventory endChest = player.getEnderChest();

        ItemStack[] armorStacks = inventory.getArmorContents();
        String armorData = buildArmorDate(armorStacks);

        ItemStack[] inventoryStacks = inventory.getContents();
        String inventoryData = buildStacksData(inventoryStacks);

        ItemStack[] endChestStacks = endChest.getContents();
        String endChestData = buildStacksData(endChestStacks);

        Database.openConnect();
        Statement statement = Database.connection.createStatement();
        String sql = "UPDATE PlayerSQL " + "SET " +
                "Economy = " + economy + ", " +
                "Health = " + player.getHealth() + ", " +
                "Food = " + player.getFoodLevel() + ", " +
                "Level = " + SetExpFix.getTotalExperience(player) + ", " +
                "Armor = '" + armorData + "', " +
                "Inventory = '" + inventoryData + "', " +
                "EndChest = '" + endChestData + "' " +
                "WHERE PlayerName = '" + player.getName().toLowerCase() + "';";
        statement.executeUpdate(sql);
        statement.close();
    }

    public static int getLocked(Player player) {
        try {
            String sql = "SELECT Locked FROM PlayerSQL WHERE PlayerName = ?;";
            PreparedStatement statement = Database.connection.prepareStatement(sql);
            statement.setString(1, player.getName().toLowerCase());
            ResultSet resultSet = statement.executeQuery();
            int dataLock = resultSet.next() ? 0 : 2;
            if (dataLock < 2) {
                dataLock = resultSet.getInt(1);
                resultSet.close();
                statement.close();
            }
            return dataLock;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void loadPlayer(Player player) throws SQLException {
        String playerName = player.getName();
        String sql = "SELECT Health, Food, Level, Armor, Inventory, EndChest, Economy "
                + "FROM PlayerSQL WHERE PlayerName = '" + playerName.toLowerCase() + "';";
        Database.openConnect();
        Statement statement = Database.connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        boolean status = resultSet.next();
        if (status) {
            double health = resultSet.getDouble(1);
            double maxHealth = player.getMaxHealth();
            int food = resultSet.getInt(2);
            int exp = resultSet.getInt(3);

            String armorData = resultSet.getString(4);
            String inventoryData = resultSet.getString(5);
            String endChestData = resultSet.getString(6);

            health = Math.min(health, maxHealth);
            player.setHealth(health);
            player.setFoodLevel(food);
            SetExpFix.setTotalExperience(player, exp);

            PlayerInventory inventory = player.getInventory();
            Inventory endChest = player.getEnderChest();

            inventory.setArmorContents(restoreStacks(armorData));
            inventory.setContents(restoreStacks(inventoryData));
            endChest.setContents(restoreStacks(endChestData));

            if (PlayerSQL.economy != null) {
                double economy = resultSet.getDouble(7);
                double playerEconomy = PlayerSQL.economy.getBalance(playerName);
                if (economy > 0) {
                    if (economy > playerEconomy)
                        PlayerSQL.economy.depositPlayer(playerName, economy - playerEconomy);
                    else PlayerSQL.economy.withdrawPlayer(playerName, playerEconomy - economy);
                }
            }
        }
    }

    public static void setupPlayer(Player player) {
        try {
            String playerName = player.getName().toLowerCase();
            String sql = "INSERT INTO PlayerSQL " + "(PlayerName, Locked) " + "VALUES ('" + playerName + "', 1);";
            Statement statement = Database.connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void lockPlayer(Player player) throws SQLException {
        String playerName = player.getName().toLowerCase();
        Statement statement = Database.connection.createStatement();
        String sql = "UPDATE PlayerSQL " + "SET Locked = 1 " + "WHERE PlayerName = '" + playerName + "';";
        statement.executeUpdate(sql);
        statement.close();
    }

    public static void lockPlayers() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        if (players.length > 0) try {
            String sql = "UPDATE PlayerSQL SET Locked = 1 WHERE PlayerName = ?;";
            PreparedStatement statement = Database.connection.prepareStatement(sql);
            for (Player player : players) {
                statement.setString(1, player.getName().toLowerCase());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unlockPlayer(Player player) throws SQLException {
        String playerName = player.getName().toLowerCase();
        Statement statement = Database.connection.createStatement();
        String sql = "UPDATE PlayerSQL " + "SET Locked = 0 " + "WHERE PlayerName = '" + playerName + "';";
        statement.executeUpdate(sql);
        statement.close();
    }

    public static void unlockPlayers() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        if (players.length > 0) try {
            String sql = "UPDATE PlayerSQL " + "SET Locked = 0 " + "WHERE PlayerName = ?;";
            PreparedStatement statement = Database.connection.prepareStatement(sql);
            for (Player player : players) {
                statement.setString(1, player.getName().toLowerCase());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
