package com.mengcraft.playerSQL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerSQL extends JavaPlugin {

    public static PlayerSQL plugin = null;
    public static Connection connection = null;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        boolean use = getConfig().getBoolean("plugin.use");
        if (use) {
            try {
                setConnection();
                setTables();
                new CheckConnectionTask().runTaskTimer(this,
                        getConfig().getInt("plugin.check", 3000),
                        getConfig().getInt("plugin.check", 3000)
                );
                getServer().getPluginManager().registerEvents(new Listener(), this);
                getLogger().info("Author: min梦梦");
                getLogger().info("插件作者: min梦梦");
            } catch (Exception e) {
                getLogger().warning("Failed to connect to database");
                getLogger().warning("Please modify config.yml!!!!!");
                getServer().getPluginManager().disablePlugin(this);
            }
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (Exception e) {
                getLogger().warning("Failed to connect to mcstats.org");
                getLogger().warning("Failed to connect to mcstats.org");
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

    private void setConnection() throws SQLException {
        String driver = getConfig().getString("plugin.driver");
        String database = getConfig().getString("plugin.connection");
        String username = getConfig().getString("plugin.username");
        String password = getConfig().getString("plugin.password");
        try {
            Class.forName(driver);
            PlayerSQL.connection = DriverManager.getConnection(database, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS PlayerSQL(" +
                "ID int NOT NULL AUTO_INCREMENT, " +
                "NAME text NULL, " +
                "DATA text NULL, " +
                "PRIMARY KEY(ID)" +
                ");";
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
    }

    private void checkConnection() throws SQLException {
        if (connection != null) {
            boolean isClosed = connection.isClosed();
            if (isClosed) {
                setConnection();
            }
        }
    }

    public class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void playerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            PlayerData playerData = PlayerManager.get(player);
            playerData.save();
            playerData.stopDaily();
            PlayerManager.remove(player);
        }

        @EventHandler
        public void playerJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            boolean isDead = player.isDead();
            if (isDead) player.spigot().respawn();
            PlayerData getPlayer = PlayerManager.get(player);
            getPlayer.load();
            getPlayer.startDaily();
        }
    }

    public class CheckConnectionTask extends BukkitRunnable {
        @Override
        public void run() {
            try {
                checkConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
