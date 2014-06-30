package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PlayerQuitThread implements Runnable {
    private Player player;

    public PlayerQuitThread(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        try {
            PlayerUtils.savePlayer(this.player);
            PlayerUtils.unlockPlayer(this.player);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
