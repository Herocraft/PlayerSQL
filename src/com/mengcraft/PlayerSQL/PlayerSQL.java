package com.mengcraft.PlayerSQL;

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
	DoSQL doSQL = new DoSQL();
	DoPlayer doPlayer = new DoPlayer();
	DoCommand doCommand = new DoCommand();
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		reloadConfig();
		if (getConfig().getBoolean("use")) {
			plugin = this;
			if (doSQL.openConnect()) {
				getLogger().info("数据库连接成功");
				if (doSQL.createTables()) {
					getServer().getPluginManager().registerEvents(this, this);
					getLogger().info("数据表效验成功");
					getLogger().info("梦梦家高性能服务器出租");
					getLogger().info("淘宝店 http://shop105595113.taobao.com");
					doPlayer.dailySavePlayer();
				}
				else {
					getLogger().info("数据表效验失败");
					setEnabled(false);
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
		if (doSQL.openConnect()) {
			if (doPlayer.saveAllPlayer()) {
				getLogger().info("保存在线玩家数据成功");
			}
			else {
				getLogger().info("保存在线玩家数据失败");
			}
			if (doSQL.closeConnect()) {
				getLogger().info("关闭数据库连接成功");
				}
				else {
					getLogger().info("关闭数据库连接失败");
					}
		}
		getLogger().info("高性能服务器出租");
		getLogger().info("淘宝店 http://shop105595113.taobao.com");
		}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("player")) {
			return doCommand.onPlayer(sender, args);
		}
		if (command.getName().equalsIgnoreCase("playeradmin")) {
			return doCommand.onPlayeradmin(sender, args);
		}
		return false;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (doPlayer.savePlayer(player)) {
			getLogger().info("保存玩家数据"
					+ player.getName()
					+ "成功");
			if (doPlayer.unlockPlayer(player)) {
				getLogger().info("解锁玩家数据"
						+ player.getName()
						+ "成功");
				}
			else {
				getLogger().info("解锁玩家数据"
						+ player.getName()
						+ "失败");
				}
			}
		else {
			getLogger().info("保存玩家数据"
					+ player.getName()
					+ "失败");
			}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (doPlayer.loadPlayer(player)) {
					getLogger().info("载入玩家数据" + player.getName() + "成功");
					if (!doPlayer.lockPlayer(player)) {
						getLogger().info("锁定玩家数据" + player.getName() + "失败");
						}
					}
				else {
					player.sendMessage("自动载入玩家数据失败");
					player.sendMessage("请联系管理员");
					getLogger().info("载入玩家数据" + player.getName() + "失败");
					}
			}
		}, getConfig().getInt("delay"));
	}
}
