package com.mengcraft.playerSQL.thread;

import com.mengcraft.playerSQL.PlayerSQL;
import com.mengcraft.playerSQL.PlayerUtils;
import org.bukkit.entity.Player;

public class PlayerDailyThread extends Thread {

    @Override
    public void run() {
        final int delay = PlayerSQL.plugin.getConfig().getInt("daily.delay");
        final int min = PlayerSQL.plugin.getConfig().getInt("daily.min");

        Player[] players = PlayerSQL.plugin.getServer().getOnlinePlayers();

        if (players.length >= min) {
            Player player;
            for (Player p : players) {
                player = p.isOnline() ?
                        p : null;
                if (player != null) PlayerUtils.savePlayer(player);
                try {
                    sleep(delay * 50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            PlayerSQL.plugin.getLogger().info("Save data successfully");
        }
        PlayerDailyThread thread = new PlayerDailyThread();
        PlayerSQL.plugin.getServer().getScheduler().runTaskLaterAsynchronously(PlayerSQL.plugin, thread, delay * 5);
    }
}
