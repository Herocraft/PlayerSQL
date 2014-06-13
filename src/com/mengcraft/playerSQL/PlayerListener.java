package com.mengcraft.playerSQL;

import com.mengcraft.playerSQL.thread.PlayerJoinThread;
import com.mengcraft.playerSQL.thread.PlayerQuitThread;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements org.bukkit.event.Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerQuitThread playerQuitThread = new PlayerQuitThread(event);
        PlayerSQL.plugin.getServer().getScheduler().runTaskAsynchronously(PlayerSQL.plugin, playerQuitThread);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        int delay = PlayerSQL.plugin.getConfig().getInt("config.delay");
        PlayerJoinThread playerJoinThread = new PlayerJoinThread(event);
        PlayerSQL.plugin.getServer().getScheduler().runTaskLaterAsynchronously(PlayerSQL.plugin, playerJoinThread, delay);
    }
}
