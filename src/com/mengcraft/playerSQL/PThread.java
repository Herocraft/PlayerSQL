package com.mengcraft.playerSQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PThread extends Thread
{

	@Override
	public void run()
	{
		final boolean show = PMain.plugin.getConfig().getBoolean("daily.show");
		final int delay = PMain.plugin.getConfig().getInt("daily.delay");
		final int min = PMain.plugin.getConfig().getInt("daily.min");
		Player[] players;

		while (PMain.plugin.isEnabled()) {
			try {
				sleep(delay * 250);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!PMain.plugin.isEnabled()) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PTrans.a);
				return;
			}
			players = PMain.plugin.getServer().getOnlinePlayers();
			if (players.length > min) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.b + players.length + PTrans.c);
				for (int i = 0; i < players.length; i++) {
					if (!players[i].isOnline()) {
						continue;
					}
					if (PUtils.savePlayer(players[i]) && show) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.d + players[i].getName() + PTrans.f);
						Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTrans.o + (i + 1) + " / " + players.length);
					}
					try {
						sleep(delay * 50);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!PMain.plugin.isEnabled()) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + PTrans.a);
						return;
					}
				}
			}
		}
	}

}
