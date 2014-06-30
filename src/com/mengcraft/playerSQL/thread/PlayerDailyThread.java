package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.Database;
import com.mengcraft.playerSQL.PlayerSQL;
import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;

public class PlayerDailyThread implements Runnable {

    @Override
    public void run() {
        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();
        int min = PlayerSQL.plugin.getConfig().getInt("daily.min");

        if (players.length >= min) PlayerUtils.savePlayers();
        else Database.createTables();
    }
}