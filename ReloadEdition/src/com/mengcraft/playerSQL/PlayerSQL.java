package com.mengcraft.playerSQL;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerSQL extends JavaPlugin {

    public static PlayerSQL plugin;
    public static Connection database;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean use = getConfig().getBoolean("plugin.use");
        if (use) {
            try {
                plugin = this;
                setDatabase();
                setTables();
                PlayerManager.setup();
                getServer().getPluginManager().registerEvents(new PlayerListener(), this);
                try {
                    Metrics metrics = new Metrics(this);
                    metrics.start();
                } catch (IOException e) {
                    getLogger().warning("Failed to connect to Metrics");
                    getLogger().warning("Failed to connect to Metrics");
                }
                getLogger().info("Author: min梦梦");
                getLogger().info("插件作者: min梦梦");
            } catch (SQLException e) {
                e.printStackTrace();
                getLogger().warning("Failed to connect to database");
                getLogger().warning("Please modify config.yml!!!!!");
                getServer().getPluginManager().disablePlugin(this);
            }
        } else {
            getLogger().warning("Please modify config.yml!!!!!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        PlayerManager.saveAll();
        PlayerManager.clear();
        getLogger().info("Author: min梦梦");
        getLogger().info("插件作者: min梦梦");
    }

    private void setTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS PlayerSQL(" +
                "ID int NOT NULL AUTO_INCREMENT, " +
                "NAME text NULL, " +
                "DATA text NULL, " +
                "PRIMARY KEY(ID)" +
                ");";
        Statement statement = database.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    public void setDatabase() throws SQLException {
        String driver = getConfig().getString("plugin.driver");
        String database = getConfig().getString("plugin.database");
        String username = getConfig().getString("plugin.username");
        String password = getConfig().getString("plugin.password");
        try {
            Class.forName(driver);
            PlayerSQL.database = DriverManager.getConnection(database, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
