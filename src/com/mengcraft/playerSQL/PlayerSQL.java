package com.mengcraft.playerSQL;

import com.mengcraft.playerSQL.thread.PlayerDailyThread;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class PlayerSQL extends JavaPlugin implements Listener {
    public static Economy economy = null;
    public static Plugin plugin;

    @Override
    public void onEnable() {
        boolean status = getServer().getPluginManager().getPlugin("ProtocolLib") == null;
        if (status) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PlayerSQL] ProtocolLib NOT found!");
            setEnabled(false);
        } else {
            saveDefaultConfig();
            plugin = this;
            status = getConfig().getBoolean("config.use") && Database.openConnect();
            if (status) {
                Database.createTables();
                PlayerUtils.lockAllPlayer();
                if (getConfig().getBoolean("daily.use")) {
                    PlayerDailyThread thread = new PlayerDailyThread();
                    getServer().getScheduler().runTaskAsynchronously(plugin, thread);
                }
                Plugin vault = getServer().getPluginManager().getPlugin("Vault");
                if (vault != null) {
                    String s = setupEconomy() ? "Hook to Vault success" : null;
                    if (s != null) getLogger().info(s);
                }
                PlayerListener listener = new PlayerListener();
                getServer().getPluginManager().registerEvents(listener, plugin);
                try {
                    Metrics metrics = new Metrics(this);
                    metrics.start();
                    getLogger().info("Connect mcstats.org success");
                } catch (IOException e) {
                    getLogger().info("Failed to connect mcstats.org");
                }
            } else {
                getLogger().info("Please check the config.yml");
                setEnabled(false);
            }
        }
    }

    @Override
    public void onDisable() {
        boolean status = plugin != null &&
                getConfig().getBoolean("config.use") &&
                Database.openConnect();
        if (status) {
            HandlerList.unregisterAll(plugin);
            PlayerUtils.saveAllPlayer();
            PlayerUtils.unlockAllPlayer();
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

