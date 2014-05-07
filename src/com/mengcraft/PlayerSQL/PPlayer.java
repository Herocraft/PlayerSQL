package com.mengcraft.PlayerSQL;

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

public class PPlayer
{
	static String buildStackData(ItemStack[] itemStacks)
	{
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < itemStacks.length; i++) {
			if (i > 0) {
				stringBuilder.append(";");
			}
			if (itemStacks[i] != null && itemStacks[i].getType() != Material.AIR) {
				try {
					stringBuilder.append(StreamSerializer.getDefault().serializeItemStack(itemStacks[i]));
				}
				catch (IOException e) {
					continue;
				}
			}
		}
		String string = stringBuilder.toString();
		return string;
	}

	static ItemStack[] restoreStackData(String string)
	{
		String[] strings = string.split(";");
		ItemStack[] itemStacks = new ItemStack[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if (!strings[i].equals("")) {
				try {
					itemStacks[i] = StreamSerializer.getDefault().deserializeItemStack(strings[i]);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				continue;
			}
		}
		return itemStacks;
	}

	public static Boolean savePlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		double health = player.getHealth();
		int food = player.getFoodLevel();
		int level = player.getLevel();
		float exp = player.getExp();
		PlayerInventory inventory = player.getInventory();
		Inventory enderChest = player.getEnderChest();

		ItemStack[] armorStacks = inventory.getArmorContents();
		String armorData = buildStackData(armorStacks);

		ItemStack[] inventoryStacks = inventory.getContents();
		String inventoryData = buildStackData(inventoryStacks);

		ItemStack[] enderChestStacks = enderChest.getContents();
		String enderChestData = buildStackData(enderChestStacks);

		try {
			Statement statement = PSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL " + "SET " + "Health = " + health + ", Food = " + food + ", " + "Level = " + level
					+ ", " + "Exp = " + Float.toString(exp) + ", " + "Armor = '" + armorData + "', " + "Inventory = '"
					+ inventoryData + "', " + "EnderChest = '" + enderChestData + "' " + "WHERE PlayerName = '" + playerName
					+ "';";
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
			String sql = "SELECT Locked, Health, Food, Level, Exp, Armor, Inventory, EnderChest "
					+ "FROM PlayerSQL WHERE PlayerName = '" + playerName + "';";
			Statement statement = PSQL.connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.last()) {
				if (resultSet.getInt(1) > 0) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "玩家数据锁状态有误");
					player.sendMessage(ChatColor.RED + "玩家数据锁状态有误请通知管理员");
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
				inventory.setArmorContents(restoreStackData(armorData));

				if (inventoryData == null) {
					return true;
				}
				inventory.setContents(restoreStackData(inventoryData));

				if (enderChestData == null) {
					return true;
				}
				enderChest.setContents(restoreStackData(enderChestData));

				resultSet.close();
				statement.close();
				return true;
			}
			else {
				sql = "INSERT INTO PlayerSQL " + "(PlayerName, Locked) " + "VALUES ('" + playerName + "', 1);";
				statement.executeUpdate(sql);
				resultSet.close();
				statement.close();
				return true;
			}
		}
		catch (SQLException e) {
			return false;
		}
	}

	public static boolean lockPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = PSQL.connection.createStatement();
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
			Statement statement = PSQL.connection.createStatement();
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
