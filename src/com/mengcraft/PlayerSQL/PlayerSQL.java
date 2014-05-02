package com.mengcraft.PlayerSQL;

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
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		if (getConfig().getBoolean("use")) {
			plugin = this;
			if (doSQL.openConnect()) {
				getLogger().info("数据库连接成功");
				if (doSQL.createTables()) {
					getServer().getPluginManager().registerEvents(this, this);
					getLogger().info("数据表效验成功");
					doPlayer.lockAllPlayer();
					getLogger().info("梦梦家高性能服务器出租");
					getLogger().info("淘宝店 http://shop105595113.taobao.com");
					
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
		if (doSQL.openConnect()) {
			doPlayer.unlockAllPlayer();
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
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (doPlayer.savePlayer(player)) {
			getLogger().info("保存玩家数据"
					+ player.getName()
					+ "成功");
		}
		else {
			getLogger().info("保存玩家数据"
					+ player.getName()
					+ "失败");
			}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (doPlayer.loadPlayer(player)) {
			getLogger().info("载入玩家数据"
					+ player.getName()
					+ "成功");
		}
		else {
			player.sendMessage("自动载入玩家数据失败");
			player.sendMessage("请立即手动执行/player load手动载入");
			getLogger().info("载入玩家数据"
					+ player.getName()
					+ "失败");
			}
	}
}
