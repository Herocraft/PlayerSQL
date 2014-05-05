package com.mengcraft.PlayerSQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DailySaveThread extends Thread
{

	@Override
	public void run()
	{
		final boolean show = PlayerSQL.plugin.getConfig().getBoolean("daily.show");
		final int delay = PlayerSQL.plugin.getConfig().getInt("daily.delay");
		final int min = PlayerSQL.plugin.getConfig().getInt("daily.min");
		Player[] players;

		while (PlayerSQL.plugin.isEnabled()) {
			try {
				sleep(delay * 250);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!PlayerSQL.plugin.isEnabled()) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "保存在线玩家结束");
				return;
			}
			players = PlayerSQL.plugin.getServer().getOnlinePlayers();
			if (players.length > min) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "开始保存在线玩家: " + players.length + " 人");
				for (int i = 0; i < players.length; i++) {
					if (!players[i].isOnline()) {
						continue;
					}
					if (DoPlayer.savePlayer(players[i]) && show) {
							Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "保存玩家 " + players[i].getName() + " 成功");
							Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "进度 " + (i + 1) + " / " + players.length);
					}
					try {
						sleep(delay * 50);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!PlayerSQL.plugin.isEnabled()) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "保存在线玩家结束");
						return;
					}
				}
			}
		}
	}

}
