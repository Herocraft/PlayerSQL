package com.mengcraft.playerSQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PMain extends JavaPlugin implements Listener {
    public static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        PTrans.translat();
        if (getConfig().getBoolean("config.use")) {
            if (SQLUtils.openConnect()) {
                getLogger().info(PTrans.i);
                if (SQLUtils.createTables()) {
                    getServer().getPluginManager().registerEvents(this, this);
                    getLogger().info(PTrans.j);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.k);
                    Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.l);
                } else {
                    getLogger().info("数据表效验失败");
                    setEnabled(false);
                }
                if (!PUtils.lockAllPlayer()) {
                    getLogger().info("锁定在线玩家失败");
                }
                if (getConfig().getBoolean("daily.use")) {
                    PThread dailySaveThread = new PThread();
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
        if (!getConfig().getBoolean("config.use")) {
            return;
        }
        if (SQLUtils.openConnect()) {
            if (PUtils.saveAllPlayer() && PUtils.unlockAllPlayer()) {
                getLogger().info(PTrans.a);
            }
            if (!SQLUtils.closeConnect()) {
                getLogger().info("关闭数据库连接失败");
            }
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.k);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.l);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitThread playerQuitThread = new PlayerQuitThread(event);
        playerQuitThread.start();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = getConfig().getInt("config.delay");
        PlayerJoinThread playerJoinThread = new PlayerJoinThread(event);
        getServer().getScheduler().runTaskLater(plugin,playerJoinThread,delay);
    }
}

class PlayerQuitThread extends Thread {
    private Player player;

    public PlayerQuitThread(PlayerQuitEvent event) {
        player = event.getPlayer();
    }

    @Override
    public void run() {
        Plugin plugin = PMain.plugin;
        if (PUtils.savePlayer(player)) {
            plugin.getLogger().info(PTrans.d + player.getName() + PTrans.f);
            if (!PUtils.unlockPlayer(player)) {
                plugin.getLogger().info("解锁玩家 " + player.getName() + PTrans.g);
            }
        } else {
            plugin.getLogger().info(PTrans.d + player.getName() + PTrans.g);
        }
    }
}

class PlayerJoinThread implements Runnable {
    private Player player;

    public PlayerJoinThread(PlayerJoinEvent event) {
        player = event.getPlayer();
    }

    @Override
    public void run() {
        if (PUtils.loadPlayer(player)) {
            PMain.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.f);
            if (!PUtils.lockPlayer(player)) {
                PMain.plugin.getLogger().info("锁定玩家 " + player.getName() + PTrans.g);
            }
        } else {
            player.sendMessage("自动载入玩家失败");
            player.sendMessage("请联系管理员");
            PMain.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.g);
        }
    }

}
