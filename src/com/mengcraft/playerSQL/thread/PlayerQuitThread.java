package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;

public class PlayerQuitThread implements Runnable{
    private Player player;

    public PlayerQuitThread(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        PlayerUtils.savePlayer(this.player);
        PlayerUtils.unlockPlayer(this.player);
    }
}
