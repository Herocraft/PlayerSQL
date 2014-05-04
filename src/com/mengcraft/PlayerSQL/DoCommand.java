package com.mengcraft.PlayerSQL;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class DoCommand
{

	public boolean onPlayer(CommandSender sender, String[] args)
	{
		if (!sender.hasPermission("playersql.admin")) {
			sender.sendMessage("你没有playersql.admin权限");
			return true;
		}
		if (args.length < 1) {
			sender.sendMessage("/player save-all");
			return true;
		}
		if (args.length < 2) {
			if (args[0].equals("save-all")) {
				OnCommandThread thread = new OnCommandThread(sender);
				thread.start();
				return true;
			}
			else {
				sender.sendMessage("/player save-all");
				return true;
			}
		}
		else {
			sender.sendMessage("/player save-all");
			return true;
		}
	}

	class OnCommandThread extends Thread
	{
		private CommandSender sender;

		public OnCommandThread(CommandSender commandSender)
		{
			sender = commandSender;
		}

		@Override
		public void run()
		{
			if (DoPlayer.saveAllPlayer()) {
				sender.sendMessage(ChatColor.GREEN + "保存在线玩家成功");
			}
		}
	}

}
