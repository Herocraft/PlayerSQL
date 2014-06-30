package com.mengcraft.playerSQL;

import com.mengcraft.playerSQL.thread.PlayerDailyThread;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class PlayerSQL extends JavaPlugin {
    public static Economy economy;
    public static PlayerSQL plugin;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null) {
            saveDefaultConfig();
            plugin = this;
            boolean config = getConfig().getBoolean("config.use")
                    && Database.openConnect()
                    && Database.createTables();
            if (config) {
                if (getConfig().getBoolean("config.economy")) setupEconomy();

                PlayerListener listener = new PlayerListener();
                getServer().getPluginManager().registerEvents(listener, this);

                PlayerUtils.lockPlayers();
                if (getConfig().getBoolean("daily.use")) {
                    int delay = getConfig().getInt("daily.delay") * 20;
                    PlayerDailyThread thread = new PlayerDailyThread();
                    getServer().getScheduler().runTaskTimerAsynchronously(this, thread, delay, delay);
                }
                try {
                    Metrics metrics = new Metrics(this);
                    metrics.start();
                    getLogger().info("Connect mcstats.org success");
                } catch (IOException e) {
                    getLogger().warning("Failed to connect mcstats.org");
                }
            } else {
                getLogger().warning("Please check the config.yml");
                setEnabled(false);
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PlayerSQL] ProtocolLib NOT found!");
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        boolean status = plugin != null &&
                getConfig().getBoolean("config.use") &&
                Database.openConnect();
        if (status) {
            HandlerList.unregisterAll(plugin);
            PlayerUtils.savePlayers();
            PlayerUtils.unlockPlayers();
            Database.closeConnect();
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}

