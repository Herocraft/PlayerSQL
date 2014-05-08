package com.mengcraft.PlayerSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.Plugin;

public class PSQL
{
	public static Connection connection;

	static String[] getSQLConfig()
	{
		Plugin plugin = PlayerSQL.plugin;
		String[] sqlConfig = { plugin.getConfig().getString("mysql.addr"), plugin.getConfig().getString("mysql.port"),
				plugin.getConfig().getString("mysql.data"), plugin.getConfig().getString("mysql.user"),
				plugin.getConfig().getString("mysql.pass") };
		return sqlConfig;
	}

	static Boolean getConnect()
	{
		if (connection != null) {
			try {
				if (connection.isClosed()) {
					connection = null;
					return false;
				}
				else {
					return true;
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			return false;
		}
		return false;
	}

	static Boolean closeConnect()
	{
		if (getConnect()) {
			try {
				connection.close();
				return true;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	static Boolean openConnect()
	{
		if (getConnect()) {
			return true;
		}
		else {
			String[] sqlConfig = getSQLConfig();
			String addr = sqlConfig[0];
			String port = sqlConfig[1];
			String data = sqlConfig[2];
			String user = sqlConfig[3];
			String pass = sqlConfig[4];
			try {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection("jdbc:mysql://" + addr + ":" + port + "/" + data, user, pass);
				return true;
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	static Boolean createTables()
	{
		if (openConnect()) {
			try {
				Statement statement = connection.createStatement();
				String sql = "CREATE TABLE IF NOT EXISTS " + "PlayerSQL (" + "Id int NOT NULL AUTO_INCREMENT, "
						+ "PlayerName text, " + "Locked int NOT NULL, " + "Health int, " + "Food int, " + "Level int, "
						+ "Exp text, " + "Armor text, " + "Inventory text, " + "EnderChest text, " + "PRIMARY KEY (Id));";
				statement.executeUpdate(sql);
				statement.close();
				return true;
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
