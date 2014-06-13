package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PlayerSQL;
import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;

public class PlayerJoinThread implements Runnable {
    private Player player;

    public PlayerJoinThread(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        int lockStatus = PlayerUtils.getLockStatus(this.player);
        switch (lockStatus) {
            case 0:
                PlayerUtils.loadPlayer(player);
                PlayerUtils.lockPlayer(player);
                break;
            case 1: {
                int i;
                for (i = 0; i < 4; i++) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i = PlayerUtils.getLockStatus(player) == 0 ?
                            4 : i;
                }
                PlayerUtils.loadPlayer(player);
                PlayerUtils.lockPlayer(player);
                if (i == 4) {
                    PlayerSQL.plugin.getLogger().warning("Waiting for unlock maximum time limit.");
                    PlayerSQL.plugin.getLogger().warning("Report to me, thank you.");
                }
                break;
            }
            case 2:
                PlayerUtils.setupPlayer(player);
                break;
        }
    }
}
