package com.mengcraft.PlayerSQL;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DoCommand
{
	DoPlayer doPlayer = new DoPlayer();

	public boolean onPlayer(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!sender.hasPermission("playersql.use")) {
				sender.sendMessage("你没有playersql.use权限");
				return true;
			}
			if (args.length < 1) {
				sender.sendMessage("/player save");
				sender.sendMessage("/player load");
				return true;
			}
			if (args.length < 2) {
				if (args[0].equals("save")) {
					if (doPlayer.savePlayer((Player) sender)) {
						sender.sendMessage("保存玩家数据成功");
						return true;
					}
					else {
						sender.sendMessage("保存玩家数据失败");
						return true;
					}
				}
				else {
					sender.sendMessage("/player save");
					return true;
				}
			}
			else {
				sender.sendMessage("/player save");
				return true;
			}
		}
		else {
			sender.sendMessage("无法执行命令");
			return true;
		}
	}

	public boolean onPlayeradmin(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("playersql.admin")) {
			sender.sendMessage("你没有playersql.admin权限");
			return true;
		}
		if (args.length < 1) {
			sender.sendMessage("/playeradmin save-all");
			return true;
		}
		if (args.length < 2) {
			if (args[0].equalsIgnoreCase("save-all")) {
				if (doPlayer.saveAllPlayer()) {
					sender.sendMessage("保存在线玩家数据成功");
					return true;
				}
				else {
					sender.sendMessage("保存在线玩家数据失败");
					return true;
				}
			}
			else {
				sender.sendMessage("/playeradmin save-all");
				return true;
			}
		}
		else {
			sender.sendMessage("/playeradmin save-all");
			return true;
		}
	}

}
