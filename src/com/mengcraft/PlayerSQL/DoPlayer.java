package com.mengcraft.PlayerSQL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.utility.StreamSerializer;

public class DoPlayer {
	DoSQL doSQL = new DoSQL();
	
	public Boolean savePlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		double health = player.getHealth();
		int level = player.getLevel();
		float exp = player.getExp();
		PlayerInventory inventory = player.getInventory();
		Inventory enderChest = player.getEnderChest();
		
		StringBuilder armorDataBuilder = new StringBuilder();
		ItemStack[] armorStacks= inventory.getArmorContents();
		for (int i = 0; i < armorStacks.length; i++) {
			if (i > 0) {
				armorDataBuilder.append(";");
			}
			if (armorStacks[i] != null) {
				try {
					armorDataBuilder.append(StreamSerializer.getDefault().serializeItemStack(armorStacks[i]));
				} catch (IOException e) {
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
				} catch (IOException e) {
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
				} catch (IOException e) {
					return false;
				}
			}
		}
		String enderChestData = enderChestDataBuilder.toString();
		
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL "
					+ "SET "
					+ "Health = " + health + ", "
					+ "Level = " + level	+ ", "
					+ "Exp = " + Float.toString(exp) + ", "
					+ "Armor = '" + armorData + "', "
					+ "Inventory = '"	+ inventoryData + "', "
					+ "EnderChest = '" + enderChestData	+ "' "
					+ "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		} catch (SQLException e) {
			return false;
			}
		}
	
	public boolean loadPlayer(Player player)
	{
		Plugin plugin = PlayerSQL.plugin;
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "SELECT Locked, Health, Level, Exp, Armor, Inventory, EnderChest "
					+ "FROM PlayerSQL "
					+ "WHERE PlayerName = '" + playerName + "';";
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.last()) {
				if (resultSet.getInt(1) > 0) {
					unlockPlayer(player);
					plugin.getLogger().info("检测到数据锁有误频繁出现请调高delay");
				}
				double health = resultSet.getDouble(2);
				int level = resultSet.getInt(3);
				float exp = resultSet.getFloat(4);
				String armorData = resultSet.getString(5);
				String inventoryData = resultSet.getString(6);
				String enderChestData = resultSet.getString(7);
				
				if (health != 0) {
					player.setHealth(health);
				}
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
				return true;
				}
			else {
				sql = "INSERT INTO PlayerSQL "
						+ "(PlayerName, Locked) "
						+ "VALUES ('" + playerName + "', 1);";
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
	
	public boolean lockPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL "
					+ "SET Locked = 1 "
					+ "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		}
		catch (SQLException e) {
			return false;
			}
		}
	
	public boolean unlockPlayer(Player player)
	{
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "UPDATE PlayerSQL "
					+ "SET Locked = 0 "
					+ "WHERE PlayerName = '" + playerName + "';";
			statement.executeUpdate(sql);
			statement.close();
			return true;
		}
		catch (SQLException e) {
			return false;
			}
		}
	
	public boolean lockAllPlayer()
	{
		Plugin plugin = PlayerSQL.plugin;
		Player[] players  = plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if(!lockPlayer(player)) {
				b = false;
				plugin.getLogger().info("锁定玩家数据"  + player.getName() + "失败");
				}
			}
		return b;
		}
	
	public boolean saveAllPlayer()
	{
		Plugin plugin = PlayerSQL.plugin;
		Player[] players  = plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if(!savePlayer(player)) {
				b = false;
				plugin.getLogger().info("保存玩家数据"  + player.getName() + "失败");
				}
			}
		return b;
		}

	public void dailySavePlayer() {
		final Plugin plugin = PlayerSQL.plugin;
		if (plugin.getConfig().getBoolean("daily.use")) {
			int delay = plugin.getConfig().getInt("daily.delay");
			plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new DailySave(), 600, delay);
			}
		}
	}

class DailySave implements Runnable
{
	DoPlayer doPlayer = new DoPlayer();
	static int j = 0;
	static Player[] players = null;

	@Override
	public void run()
	{
		listPlayer();
		if (players != null) {
			if (j < players.length) {
				if (players[j].isOnline()) {
					Plugin plugin = PlayerSQL.plugin;
					if (doPlayer.savePlayer(players[j])) {
						plugin.getLogger().info("循环保存" + players[0].getName() + "成功");
						}
					else {
						plugin.getLogger().info("循环保存" + players[0].getName() + "失败");
						}
					}
				j++;
				}
			else {
				j = 0;
				players = null;
				}
			}
		}
	
	void listPlayer() {
		Plugin plugin = PlayerSQL.plugin;
		int min = plugin.getConfig().getInt("daily.min");
		if (plugin.getServer().getOnlinePlayers().length > min
				&& j < 1) {
				players = plugin.getServer().getOnlinePlayers();
				if (players.length > 0) {
					plugin.getLogger().info("在线玩家: " + players.length);
				}
			}
		}
	}
