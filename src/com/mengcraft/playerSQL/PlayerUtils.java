package com.mengcraft.playerSQL;

import com.comphenix.protocol.utility.StreamSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
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

    public static void savePlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        double health = player.getHealth();
        int food = player.getFoodLevel();
        int level = player.getLevel();
        float exp = player.getExp();

        double economy = 0;

        boolean status = PlayerSQL.economy != null &&
                PlayerSQL.plugin.getConfig().getBoolean("config.economy", true);

        if (status) economy = PlayerSQL.economy.getBalance(playerName);

        PlayerInventory inventory = player.getInventory();
        Inventory endChest = player.getEnderChest();

        ItemStack[] armorStacks = inventory.getArmorContents();
        String armorData = buildArmorDate(armorStacks);

        ItemStack[] inventoryStacks = inventory.getContents();
        String inventoryData = buildStacksData(inventoryStacks);

        ItemStack[] endChestStacks = endChest.getContents();
        String endChestData = buildStacksData(endChestStacks);

        try {
            Statement statement = Database.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET " +
                    "Economy = " + economy + ", " +
                    "Health = " + health + ", " +
                    "Food = " + food + ", " +
                    "Level = " + level + ", " +
                    "Exp = " + Float.toString(exp) + ", " +
                    "Armor = '" + armorData + "', " +
                    "Inventory = '" + inventoryData + "', " +
                    "EndChest = '" + endChestData + "' " +
                    "WHERE PlayerName = '" + playerName + "';";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getLockStatus(Player player) {
        String playerName = player.getName().toLowerCase();
        String sql = "SELECT Locked FROM PlayerSQL WHERE PlayerName = '" + playerName + "';";
        int dataLock = 0;
        try {
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            dataLock = resultSet.next() ?
                    0 : 2;
            if (dataLock > 1) return 2;
            dataLock = resultSet.getInt(1);
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataLock;
    }

    public static void loadPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        String sql = "SELECT Health, Food, Level, Exp, Armor, Inventory, EndChest, Economy "
                + "FROM PlayerSQL WHERE PlayerName = '" + playerName + "';";
        try {
            Statement statement = Database.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            boolean status = resultSet.next();
            if (status) {
                double health = resultSet.getDouble(1);
                double maxHealth = player.getMaxHealth();
                int food = resultSet.getInt(2);
                int level = resultSet.getInt(3);
                float exp = resultSet.getFloat(4);

                String armorData = resultSet.getString(5);
                String inventoryData = resultSet.getString(6);
                String endChestData = resultSet.getString(7);

                health = Math.min(health, maxHealth);
                player.setHealth(health);
                player.setFoodLevel(food);
                player.setLevel(level);
                player.setExp(exp);

                PlayerInventory inventory = player.getInventory();
                Inventory endChest = player.getEnderChest();

                inventory.setArmorContents(restoreStacks(armorData));
                inventory.setContents(restoreStacks(inventoryData));
                endChest.setContents(restoreStacks(endChestData));

                status = PlayerSQL.economy != null &&
                        PlayerSQL.plugin.getConfig().getBoolean("config.economy", true);
                if (status) {
                    double economy = resultSet.getDouble(8);
                    double playerEconomy = PlayerSQL.economy.getBalance(playerName);
                    if (economy > 0) {
                        if (economy > playerEconomy)
                            PlayerSQL.economy.depositPlayer(playerName, economy - playerEconomy);
                        else PlayerSQL.economy.withdrawPlayer(playerName, playerEconomy - economy);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setupPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        String sql = "INSERT INTO PlayerSQL " + "(PlayerName) " + "VALUES ('" + playerName + "');";
        try {
            Statement statement = Database.connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean lockPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        try {
            Statement statement = Database.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET Locked = 1 " + "WHERE PlayerName = '" + playerName + "';";
            statement.executeUpdate(sql);
            statement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void unlockPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        try {
            Statement statement = Database.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET Locked = 0 " + "WHERE PlayerName = '" + playerName + "';";
            statement.executeUpdate(sql);
            statement.close();
        } catch (SQLException e) {
            PlayerSQL.plugin.getLogger().info("Failed to unlock the player " + playerName);
            PlayerSQL.plugin.getLogger().info("This type of mistake is strange :(");
        }
    }

    public static void lockAllPlayer() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        for (Player player : players) lockPlayer(player);
    }

    public static void unlockAllPlayer() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        for (Player player : players) unlockPlayer(player);
    }

    public static void saveAllPlayer() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        for (Player player : players) savePlayer(player);
    }
}
