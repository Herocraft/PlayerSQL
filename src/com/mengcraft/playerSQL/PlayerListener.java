package com.mengcraft.playerSQL;

import com.mengcraft.playerSQL.thread.PlayerJoinThread;
import com.mengcraft.playerSQL.thread.PlayerQuitThread;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements org.bukkit.event.Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerQuitThread thread = new PlayerQuitThread(player);
        PlayerSQL.plugin.getServer().getScheduler().runTaskAsynchronously(PlayerSQL.plugin,thread);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerJoinThread thread = new PlayerJoinThread(player);
        PlayerSQL.plugin.getServer().getScheduler().runTaskLaterAsynchronously(PlayerSQL.plugin, thread, 10);
    }
}
