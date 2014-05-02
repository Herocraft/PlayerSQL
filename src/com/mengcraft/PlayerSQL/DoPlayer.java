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
					+ "SET Locked = 0, "
					+ "Health = " + health + ", "
					+ "Level = " + level	+ ", "
					+ "Exp = " + Float.toString(exp) + ", "
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
		String playerName = player.getName().toLowerCase();
		try {
			Statement statement = DoSQL.connection.createStatement();
			String sql = "SELECT Locked, Health, Level, Exp, Inventory, EnderChest "
					+ "FROM PlayerSQL "
					+ "WHERE PlayerName = '" + playerName + "';";
			ResultSet resultSet = statement.executeQuery(sql);
			if (resultSet.last()) {
				if (resultSet.getInt(1) > 0) {
					return false;
				}
				else {
					double health = resultSet.getDouble(2);
					int level = resultSet.getInt(3);
					float exp = resultSet.getFloat(4);
					String inventoryData = resultSet.getString(5);
					String enderChestData = resultSet.getString(6);
					
					if (health != 0) {
						player.setHealth(health);
					}
					player.setLevel(level);
					player.setExp(exp);
					
					PlayerInventory inventory = player.getInventory();
					Inventory enderChest = player.getEnderChest();
					
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
	
	public boolean lockAllPlayer()
	{
		Plugin plugin = PlayerSQL.plugin;
		Player[] players  = plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if(!lockPlayer(player)) {
				b = false;
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
				}
			}
		return b;
		}
	
	public boolean loadAllPlayer()
	{
		Plugin plugin = PlayerSQL.plugin;
		Player[] players  = plugin.getServer().getOnlinePlayers();
		boolean b = true;
		for (Player player : players) {
			if(!loadPlayer(player)) {
				b = false;
			}
		}
		return b;
		}
	
	}
