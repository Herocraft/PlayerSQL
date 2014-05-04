package com.mengcraft.PlayerSQL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.comphenix.protocol.utility.StreamSerializer;

public class DoPlayer
{
	public static Boolean savePlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		double health = player.getHealth();
		int food = player.getFoodLevel();
		int level = player.getLevel();
		float exp = player.getExp();
		PlayerInventory inventory = player.getInventory();
		Inventory enderChest = player.getEnderChest();

		StringBuilder armorDataBuilder = new StringBuilder();
		ItemStack[] armorStacks = inventory.getArmorContents();
		for (int i = 0; i < armorStacks.length; i++) {
			if (i > 0) {
				armorDataBuilder.append(";");
			}
			if (armorStacks[i] != null) {
				try {
					armorDataBuilder.append(StreamSerializer.getDefault().serializeItemStack(armorStacks[i]));
				}
				catch (IOException e) {
					return false;
				}
			}
		}
		String armorData = armorDataBuilder.toString();

		StringBuilder inventoryDataBuilder = new StringBuilder();
		ItemStack[] inventoryStacks = inventory.getContents();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (i > 0) {
				inventoryDataBuilder.append(";");
			}
			if (inventoryStacks[i] != null) {
				try {
					inventoryDataBuilder.append(StreamSerializer.getDefault().serializeItemStack(inventoryStacks[i]));
				}
				catch (IOException e) {
					return false;
				}
			}
		}
		String inventoryData = inventoryDataBuilder.toString();

		StringBuilder enderChestDataBuilder = new StringBuilder();
		ItemStack[] enderChestStacks = enderChest.getContents();
		for (int i = 0; i < enderChest.getSize(); i++) {
			if (i > 0) {
				enderChestDataBuilder.append(";");
			}
			if (inventoryStacks[i] != null) {
				try {
					enderChestDataBuilder.append(StreamSerializer.getDefault().serializeItemStack(enderChestStacks[i]));
				}
				catch (IOException e) {
					return false;
				}
			}
		}
		String enderChestData = enderChestDataBuilder.toString();

		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL " + "SET " + "Health = " + health + ", Food = " + food + ", " + "Level = " + level + ", " + "Exp = "
					+ Float.toString(exp) + ", " + "Armor = '" + armorData + "', " + "Inventory = '" + inventoryData + "', " + "EnderChest = '"
					+ enderChestData + "' " + "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	public static boolean loadPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "SELECT Locked, Health, Food, Level, Exp, Armor, Inventory, EnderChest " + "FROM PlayerSQL " + "WHERE PlayerName = '" + playerName
					+ "';";
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.last()) {
				if (resultSet.getInt(1) > 0) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "玩家数据锁状态有误");
				}
				double health = resultSet.getDouble(2);
				int food = resultSet.getInt(3);
				int level = resultSet.getInt(4);
				float exp = resultSet.getFloat(5);
				String armorData = resultSet.getString(6);
				String inventoryData = resultSet.getString(7);
				String enderChestData = resultSet.getString(8);

				player.setHealth(health);
				player.setFoodLevel(food);
				player.setLevel(level);
				player.setExp(exp);

				PlayerInventory inventory = player.getInventory();
				Inventory enderChest = player.getEnderChest();

				if (armorData == null) {
					return true;
				}
				String[] armorItems = armorData.split(";");
				ItemStack[] armorStacks = new ItemStack[armorItems.length];
				for (int i = 0; i < armorStacks.length; i++) {
					if (!armorItems[i].equals("")) {
						armorStacks[i] = StreamSerializer.getDefault().deserializeItemStack(armorItems[i]);
					}
					else {
						continue;
					}
				}
				inventory.setArmorContents(armorStacks);

				if (inventoryData == null) {
					return true;
				}
				String[] inventoryItems = inventoryData.split(";");
				ItemStack[] inventoryStacks = new ItemStack[inventory.getSize()];
				for (int i = 0; i < inventoryItems.length; i++) {
					if (!inventoryItems[i].equals("")) {
						inventoryStacks[i] = StreamSerializer.getDefault().deserializeItemStack(inventoryItems[i]);
					}
					else {
						continue;
					}
				}
				inventory.setContents(inventoryStacks);

				if (enderChestData == null) {
					return true;
				}
				String[] enderChestItems = enderChestData.split(";");
				ItemStack[] enderChestStacks = new ItemStack[enderChest.getSize()];
				for (int i = 0; i < enderChestItems.length; i++) {
					if (!enderChestItems[i].equals("")) {
						enderChestStacks[i] = StreamSerializer.getDefault().deserializeItemStack(enderChestItems[i]);
					}
					else {
						continue;
					}
				}
				enderChest.setContents(enderChestStacks);
				statement.close();
				return true;
			}
			else {
				sql = "INSERT INTO PlayerSQL " + "(PlayerName, Locked) " + "VALUES ('" + playerName + "', 1);";
				statement.executeUpdate(sql);
				statement.close();
				return true;
			}
		}
		catch (SQLException e) {
			return false;
		}
		catch (IOException e) {
			return false;
		}
	}

	public static boolean lockPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL " + "SET Locked = 1 " + "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	public static boolean unlockPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL " + "SET Locked = 0 " + "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		}
		catch (SQLException e) {
			return false;
		}
	}

	public static boolean lockAllPlayer()
	{
		Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if (!lockPlayer(player)) {
				b = false;
				PlayerSQL.plugin.getLogger().info("锁定玩家 " + player.getName() + " 失败");
			}
		}
		return b;
	}

	public static boolean unlockAllPlayer()
	{
		Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if (!unlockPlayer(player)) {
				b = false;
				PlayerSQL.plugin.getLogger().info("解锁玩家 " + player.getName() + " 失败");
			}
		}
		return b;
	}

	public static boolean saveAllPlayer()
	{
		Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if (!savePlayer(player)) {
				b = false;
				PlayerSQL.plugin.getLogger().info("保存玩家 " + player.getName() + " 失败");
			}
		}
		return b;
	}

}
