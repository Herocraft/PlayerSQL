package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PTrans;
import com.mengcraft.playerSQL.PlayerSQL;
import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerQuitThread extends Thread {
    private Player player;

    public PlayerQuitThread(PlayerQuitEvent event) {
        player = event.getPlayer();
    }

    @Override
    public void run() {
        Plugin plugin = PlayerSQL.plugin;
        if (PlayerUtils.savePlayer(player)) {
            plugin.getLogger().info(PTrans.d + player.getName() + PTrans.f);
            if (!PlayerUtils.unlockPlayer(player)) {
                plugin.getLogger().info("解锁玩家 " + player.getName() + PTrans.g);
            }
        } else {
            plugin.getLogger().info(PTrans.d + player.getName() + PTrans.g);
        }
    }
}
