package com.mengcraft.playerSQL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.comphenix.protocol.utility.StreamSerializer;

public class Utils {
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
    }

    public static Boolean savePlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        double health = player.getHealth();
        int food = player.getFoodLevel();
        int level = player.getLevel();
        float exp = player.getExp();
        PlayerInventory inventory = player.getInventory();
        Inventory endChest = player.getEnderChest();

        ItemStack[] armorStacks = inventory.getArmorContents();
        String armorData = buildArmorDate(armorStacks);

        ItemStack[] inventoryStacks = inventory.getContents();
        String inventoryData = buildStacksData(inventoryStacks);

        ItemStack[] endChestStacks = endChest.getContents();
        String endChestData = buildStacksData(endChestStacks);

        try {
            Statement statement = SQL.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET " +
                    "Health = " + health + ", Food = " + food + ", " + "Level = " + level
                    + ", " + "Exp = " + Float.toString(exp) + ", " + "Armor = '" + armorData + "', " + "Inventory = '"
                    + inventoryData + "', " + "EndChest = '" + endChestData + "' " + "WHERE PlayerName = '" + playerName
                    + "';";
            statement.executeUpdate(sql);

            boolean status = Main.economy != null &&
                    Main.plugin.getConfig().getBoolean("config.economy", true);
            if (status) {
                double economy = Main.economy.getBalance(playerName);
                sql = "UPDATE PlayerSQL SET Economy = " + economy + ";";
                statement.executeUpdate(sql);
            }

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean loadPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        String sql = "SELECT Locked, Health, Food, Level, Exp, Armor, Inventory, EndChest "
                + "FROM PlayerSQL WHERE PlayerName = '" + playerName + "';";
        try {
            Statement statement = SQL.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                if (resultSet.getInt(1) > 0) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "玩家" + playerName + "数据锁状态有误");
                    player.sendMessage(ChatColor.RED + "玩家数据锁状态有误请通知管理员");
                }
                double health = resultSet.getDouble(2);
                double maxHealth = player.getMaxHealth();
                int food = resultSet.getInt(3);
                int level = resultSet.getInt(4);
                float exp = resultSet.getFloat(5);

                String armorData = resultSet.getString(6);
                String inventoryData = resultSet.getString(7);
                String endChestData = resultSet.getString(8);

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

                boolean status = Main.economy != null &&
                        Main.plugin.getConfig().getBoolean("config.economy", true);
                if (status) {
                    sql = "SELECT Economy FROM PlayerSQL WHERE PlayerName = '" + playerName + "';";
                    resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        double economy = resultSet.getDouble(1);
                        double playerEconomy = Main.economy.getBalance(playerName);
                        if (economy > 0) {
                            if (economy > playerEconomy) Main.economy.depositPlayer(playerName, economy - playerEconomy);
                            else Main.economy.withdrawPlayer(playerName, playerEconomy - economy);
                        }
                    }
                }

                resultSet.close();
                statement.close();
                return true;
            } else {
                sql = "INSERT INTO PlayerSQL " + "(PlayerName) " + "VALUES ('" + playerName + "');";
                statement.executeUpdate(sql);
                resultSet.close();
                statement.close();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean lockPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        try {
            Statement statement = SQL.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET Locked = 1 " + "WHERE PlayerName = '" + playerName + "';";
            statement.executeUpdate(sql);
            statement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean unlockPlayer(Player player) {
        String playerName = player.getName().toLowerCase();
        try {
            Statement statement = SQL.connection.createStatement();
            String sql = "UPDATE PlayerSQL " + "SET Locked = 0 " + "WHERE PlayerName = '" + playerName + "';";
            statement.executeUpdate(sql);
            statement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean lockAllPlayer() {
        Player[] players = Main.plugin.getServer().getOnlinePlayers();
        boolean b = true;
        for (Player player : players) {
            if (!lockPlayer(player)) {
                b = false;
                Main.plugin.getLogger().info("锁定玩家 " + player.getName() + " 失败");
            }
        }
        return b;
    }

    public static boolean unlockAllPlayer() {
        Player[] players = Main.plugin.getServer().getOnlinePlayers();
        boolean b = true;
        for (Player player : players) {
            if (!unlockPlayer(player)) {
                b = false;
                Main.plugin.getLogger().info("解锁玩家 " + player.getName() + " 失败");
            }
        }
        return b;
    }

    public static boolean saveAllPlayer() {
        Player[] players = Main.plugin.getServer().getOnlinePlayers();
        boolean b = true;
        for (Player player : players) {
            if (!savePlayer(player)) {
                b = false;
                Main.plugin.getLogger().info("保存玩家 " + player.getName() + " 失败");
            }
        }
        return b;
    }

}
