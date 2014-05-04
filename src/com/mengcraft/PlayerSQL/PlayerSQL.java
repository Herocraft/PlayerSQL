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
	private DoCommand doCommand = new DoCommand();

	@Override
	public void onEnable()
	{
		plugin = this;
		saveDefaultConfig();
		reloadConfig();
		if (getConfig().getBoolean("use")) {
			if (DoSQL.openConnect()) {
				getLogger().info("数据库连接成功");
				if (DoSQL.createTables()) {
					getServer().getPluginManager().registerEvents(this, this);
					getLogger().info("数据表效验成功");
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "梦梦家高性能服务器出租");
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "淘宝店 http://shop105595113.taobao.com");
				}
				else {
					getLogger().info("数据表效验失败");
					setEnabled(false);
				}
				if (!DoPlayer.lockAllPlayer()) {
					getLogger().info("锁定在线玩家失败");
				}
				if (getConfig().getBoolean("daily.use")) {
					DailySaveThread dailySaveThread = new DailySaveThread();
					dailySaveThread.start();
				}
			}
			else {
				getLogger().info("数据库连接失败");
				setEnabled(false);
			}
		}
		else {
			getLogger().info("请在配置文件中启用插件");
			setEnabled(false);
		}
	}

	@Override
	public void onDisable()
	{
		if (!getConfig().getBoolean("use")) {
			return;
		}
		if (DoSQL.openConnect()) {
			if (DoPlayer.saveAllPlayer() && DoPlayer.unlockAllPlayer()) {
				getLogger().info("保存在线玩家成功");
			}
			if (DoSQL.closeConnect()) {
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
		if (DoPlayer.savePlayer(player)) {
			plugin.getLogger().info("保存玩家 " + player.getName() + " 成功");
			if (!DoPlayer.unlockPlayer(player)) {
				plugin.getLogger().info("解锁玩家 " + player.getName() + " 失败");
			}
		}
		else {
			plugin.getLogger().info("保存玩家 " + player.getName() + " 失败");
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
		if (DoPlayer.loadPlayer(player)) {
			PlayerSQL.plugin.getLogger().info("载入玩家 " + player.getName() + " 成功");
			if (!DoPlayer.lockPlayer(player)) {
				PlayerSQL.plugin.getLogger().info("锁定玩家 " + player.getName() + " 失败");
			}
		}
		else {
			player.sendMessage("自动载入玩家失败");
			player.sendMessage("请联系管理员");
			PlayerSQL.plugin.getLogger().info("载入玩家 " + player.getName() + " 失败");
		}
	}

}
