package com.mengcraft.playerSQL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

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
