package com.mengcraft.playerSQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerThread extends Thread {
    @Override
    public void run() {
        final boolean show = Main.plugin.getConfig().getBoolean("daily.show");
        final int delay = Main.plugin.getConfig().getInt("daily.delay");
        final int min = Main.plugin.getConfig().getInt("daily.min");
        Player[] players;

        while (Main.plugin.isEnabled()) {
            try {
                sleep(delay * 250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Main.plugin.isEnabled()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PTrans.a);
                return;
            }
            players = Main.plugin.getServer().getOnlinePlayers();
            if (players.length > min) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.b + players.length + PTrans.c);
                for (int i = 0; i < players.length; i++) {
                    if (!players[i].isOnline()) {
                        continue;
                    }
                    if (Utils.savePlayer(players[i]) && show) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.d + players[i].getName() + PTrans.f);
                        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.o + (i + 1) + " / " + players.length);
                    }
                    try {
                        sleep(delay * 50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!Main.plugin.isEnabled()) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PTrans.a);
                        return;
                    }
                }
            }
        }
    }

}

class PlayerQuitThread extends Thread {
    private Player player;

    public PlayerQuitThread(PlayerQuitEvent event) {
        player = event.getPlayer();
    }

    @Override
    public void run() {
        Plugin plugin = Main.plugin;
        if (Utils.savePlayer(player)) {
            plugin.getLogger().info(PTrans.d + player.getName() + PTrans.f);
            if (!Utils.unlockPlayer(player)) {
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
        if (Utils.loadPlayer(player)) {
            Main.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.f);
            if (!Utils.lockPlayer(player)) {
                Main.plugin.getLogger().info("锁定玩家 " + player.getName() + PTrans.g);
            }
        } else {
            player.sendMessage("自动载入玩家失败");
            player.sendMessage("请联系管理员");
            Main.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.g);
        }
    }

}
