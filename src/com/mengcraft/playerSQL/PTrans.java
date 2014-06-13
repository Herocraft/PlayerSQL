package com.mengcraft.playerSQL;

public class PTrans
{
    public static String a;
    public 	static String b;
    public 	static String c;
    public 	static String d;
    public 	static String e;
    public 	static String f;
    public 	static String g;
    public static String o;
    public static String h;
    public 	static String i;
    public 	static String j;
    public static String k;
    public static String l;
    public 	static String m;
    public 	static String n;

	static void translate()
	{
		if (PlayerSQL.plugin.getConfig().getBoolean("config.english")) {
			a = "Save all online players complete";
			b = "Online: ";
			c = " Players";
			d = "Save player ";
			e = "Load player ";
			f = " success";
			g = " failed";
			h = "You don't have playerSQL.admin permission";
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
