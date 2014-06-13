package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PTrans;
import com.mengcraft.playerSQL.PlayerSQL;
import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinThread implements Runnable {
    private Player player;

    public PlayerJoinThread(PlayerJoinEvent event) {
        player = event.getPlayer();
    }

    @Override
    public void run() {
        if (PlayerUtils.loadPlayer(player)) {
            PlayerSQL.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.f);
            if (!PlayerUtils.lockPlayer(player)) {
                PlayerSQL.plugin.getLogger().info("锁定玩家 " + player.getName() + PTrans.g);
            }
        } else {
            player.sendMessage("自动载入玩家失败");
            player.sendMessage("请联系管理员");
            PlayerSQL.plugin.getLogger().info(PTrans.e + player.getName() + PTrans.g);
        }
    }

}
