package com.mengcraft.playerSQL;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements org.bukkit.event.Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitThread playerQuitThread = new PlayerQuitThread(event);
        playerQuitThread.start();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = Main.plugin.getConfig().getInt("config.delay");
        PlayerJoinThread playerJoinThread = new PlayerJoinThread(event);
        Main.plugin.getServer().getScheduler().runTaskLaterAsynchronously(Main.plugin, playerJoinThread, delay);
    }
}

