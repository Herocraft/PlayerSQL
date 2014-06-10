package com.mengcraft.playerSQL;

public class PTrans
{
	static String a;
	static String b;
	static String c;
	static String d;
	static String e;
	static String f;
	static String g;
	static String o;
	static String h;
	static String i;
	static String j;
	static String k;
	static String l;
	static String m;
	static String n;

	static void translat()
	{
		if (Main.plugin.getConfig().getBoolean("config.english")) {
			a = "Save all online players complate";
			b = "Online: ";
			c = " Players";
			d = "Save player ";
			e = "Load player ";
			f = " success";
			g = " failed";
			h = "You don't hava playerSQL.admin permission";
			i = "Connect success";
			j = "Check success";
			k = "Thanks for download.";
			l = "My Email: caoli5288@gmail.com";
			m = "Connect failed";
			n = "Turn on the plugin in config.yml";
			o = "Total: ";
		}
		else {
			a = "保存在线玩家结束";
			b = "开始保存在线玩家: ";
			c = " 人";
			d = "保存玩家 ";
			e = "载入玩家 ";
			f = " 成功";
			g = " 失败";
			h = "你没有playersql.admin权限";
			i = "数据库连接成功";
			j = "数据表效验成功";
			k = "梦梦家高性能服务器出租";
			l = "淘宝店 http://shop105595113.taobao.com";
			m = "数据库连接失败";
			n = "请在配置文件中启用插件";
			o = "进度 ";
		}
	}
}
