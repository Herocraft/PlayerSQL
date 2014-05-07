package com.mengcraft.PlayerSQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerSQL extends JavaPlugin implements Listener
{
	public static Plugin plugin;
	public static boolean isEnglish;
	private PCommand doCommand = new PCommand();

	@Override
	public void onEnable()
	{
		plugin = this;
		saveDefaultConfig();
		reloadConfig();
		isEnglish = getConfig().getBoolean("english");
		PTranslat.translat();
		if (getConfig().getBoolean("use")) {
			if (PSQL.openConnect()) {
				getLogger().info(PTranslat.i);
				if (PSQL.createTables()) {
					getServer().getPluginManager().registerEvents(this, this);
					getLogger().info(PTranslat.j);
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTranslat.k);
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + PTranslat.l);
				}
				else {
					getLogger().info("数据表效验失败");
					setEnabled(false);
				}
				if (!PPlayer.lockAllPlayer()) {
					getLogger().info("锁定在线玩家失败");
				}
				if (getConfig().getBoolean("daily.use")) {
					DSThread dailySaveThread = new DSThread();
					dailySaveThread.start();
				}
			}
			else {
				getLogger().info(PTranslat.m);
				setEnabled(false);
			}
		}
		else {
			getLogger().info(PTranslat.n);
			setEnabled(false);
		}
	}

	@Override
	public void onDisable()
	{
		if (!getConfig().getBoolean("use")) {
			return;
		}
		if (PSQL.openConnect()) {
			if (PPlayer.saveAllPlayer() && PPlayer.unlockAllPlayer()) {
				getLogger().info(PTranslat.a);
			}
			if (PSQL.closeConnect()) {
				getLogger().info("关闭数据库连接成功");
			}
			else {
				getLogger().info("关闭数据库连接失败");
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "高性能服务器出租");
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equalsIgnoreCase("player")) {
			return doCommand.onPlayer(sender, args);
		}
		return true;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		PlayerQuitThread playerQuitThread = new PlayerQuitThread(event);
		playerQuitThread.start();
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		PlayerJoinThread playerJoinThread = new PlayerJoinThread(event);
		playerJoinThread.start();
	}
}

class PlayerQuitThread extends Thread
{
	private Player player;

	public PlayerQuitThread(PlayerQuitEvent event)
	{
		player = event.getPlayer();
	}

	@Override
	public void run()
	{
		Plugin plugin = PlayerSQL.plugin;
		if (PPlayer.savePlayer(player)) {
			plugin.getLogger().info(PTranslat.d + player.getName() + PTranslat.f);
			if (!PPlayer.unlockPlayer(player)) {
				plugin.getLogger().info("解锁玩家 " + player.getName() + PTranslat.g);
			}
		}
		else {
			plugin.getLogger().info(PTranslat.d + player.getName() + PTranslat.g);
		}
	}
}

class PlayerJoinThread extends Thread
{
	private Player player;

	public PlayerJoinThread(PlayerJoinEvent event)
	{
		player = event.getPlayer();
	}

	@Override
	public void run()
	{
		try {
			Thread.sleep(PlayerSQL.plugin.getConfig().getLong("delay") * 50);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (PPlayer.loadPlayer(player)) {
			PlayerSQL.plugin.getLogger().info(PTranslat.e + player.getName() + PTranslat.f);
			if (!PPlayer.lockPlayer(player)) {
				PlayerSQL.plugin.getLogger().info("锁定玩家 " + player.getName() + PTranslat.g);
			}
		}
		else {
			player.sendMessage("自动载入玩家失败");
			player.sendMessage("请联系管理员");
			PlayerSQL.plugin.getLogger().info(PTranslat.e + player.getName() + PTranslat.g);
		}
	}

}
