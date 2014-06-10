package com.mengcraft.playerSQL;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
    public static Economy economy;
    public static Plugin plugin;

    @Override
    public void onEnable() {
        boolean status = getServer().getPluginManager().getPlugin("ProtocolLib") == null;
        if (status) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[PlayerSQL] ProtocolLib NOT found!");
            getServer().getPluginManager().disablePlugin(this);

            return;
        }
        saveDefaultConfig();
        plugin = this;
        PTrans.translat();
        economy = getServer().getPluginManager().getPlugin("Vault") != null ?
                setupEconomy() :
                null;
        if (getConfig().getBoolean("config.use")) {
            if (SQL.openConnect()) {
                getLogger().info(PTrans.i);
                if (SQL.createTables()) {
                    PlayerListener playerListener = new PlayerListener();
                    getServer().getPluginManager().registerEvents(playerListener, plugin);
                    getLogger().info(PTrans.j);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.k);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.l);
                } else {
                    getLogger().info("数据表效验失败");
                    setEnabled(false);
                }
                if (!Utils.lockAllPlayer()) {
                    getLogger().info("锁定在线玩家失败");
                }
                if (getConfig().getBoolean("daily.use")) {
                    PlayerThread dailySaveThread = new PlayerThread();
                    dailySaveThread.start();
                }
            } else {
                getLogger().info(PTrans.m);
                setEnabled(false);
            }
        } else {
            getLogger().info(PTrans.n);
            setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        boolean status = plugin != null &&
                getConfig().getBoolean("config.use") &&
                SQL.openConnect();
        if (status) {
            if (Utils.saveAllPlayer() && Utils.unlockAllPlayer()) {
                getLogger().info(PTrans.a);
            }
            if (!SQL.closeConnect()) getLogger().info("关闭数据库连接失败");

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.k);
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.l);
        }
    }

    private Economy setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().
                getServicesManager().
                getRegistration(net.milkbowl.vault.economy.Economy.class);
        return economyProvider.getProvider();
    }
}

