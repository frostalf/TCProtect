package net.milkycraft.tcprotect;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkycraft.tcprotect.listeners.RPBlockListener;
import net.milkycraft.tcprotect.listeners.RPEntityListener;
import net.milkycraft.tcprotect.listeners.RPPlayerListener;
import net.milkycraft.tcprotect.listeners.RPWorldListener;
import net.milkycraft.tcprotect.managers.CommandManager;
import net.milkycraft.tcprotect.managers.ConfigurationManager;
import net.milkycraft.tcprotect.managers.RegionManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TCProtect extends JavaPlugin {

	public static TCProtect plugin;

	public CommandManager cManager;
	public static RegionManager rm;
	public static TCPermHandler ph;
	public static TCLogger logger = null;
	public static HashMap<Player, Location> flSels = new HashMap<>();
	public static HashMap<Player, Location> slSels = new HashMap<>();
	public final static String pathMain = "plugins" + File.separator + "TCProtect" + File.separator;
	public final static String pathData = pathMain + File.separator + "data" + File.separator;
	public final static String pathConfig = pathMain + File.separator + "config.yml";
	public final static String pathFlagConfig = pathMain + File.separator + "flags.yml";
	public static FileType fileType = FileType.YML;
	public static boolean removeBlocks = false;
	public static boolean debugMessages = false;
	public static int limitAmount = 400;
	public static int blockID = 55;
	public static int maxScan = 600;
	public static int heightStart = 50;
	public static String mysqlUserName = "root";
	public static String mysqlUserPass = "pass";
	public static String mysqlDatabaseName = "mctcprotect";
	public static String mysqlHost = "localhost";
	public static boolean backup = true;
	public static int adminWandID = Material.FEATHER.getId();
	public static int infoWandID = Material.STRING.getId();
	public static int timeBack = 90;
	public static String timeUnit = "d";
	public static int lockLimit = 3;
	public static int purgeTime;
	public static List<String> lockPlayers = new ArrayList<>();
	public static List<String> unlockPlayers = new ArrayList<>();

	@Override
	public void onDisable() {
		if (rm != null) {
            rm.saveAll();
        }
	}

	@Override
	public void onEnable() {
		try {
			initVars();
			TCUtility.init(this);
			ConfigurationManager.initFiles(this);
			rm.loadAll();
			checkLockTable();
			Bukkit.getPluginManager().registerEvents(new RPBlockListener(this), this);
			Bukkit.getPluginManager().registerEvents(new RPPlayerListener(this), this);
			Bukkit.getPluginManager().registerEvents(new RPEntityListener(this), this);
			Bukkit.getPluginManager().registerEvents(new RPWorldListener(this), this);
			getCommand("tcprotect").setExecutor(this.cManager);
		} catch (Exception e) {
			e.printStackTrace();
			setEnabled(false);
			return;
		}
		plugin = this;
        purgeTime = getConfig().getInt("purge-check");
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new PurgerThread(), 0L, purgeTime * 1200);
	}

	void initVars() throws Exception {
		logger = new TCLogger(Bukkit.getLogger());
		this.cManager = new CommandManager();
		ph = new TCPermHandler();
		rm = new RegionManager();
		File old = new File("plugins" + File.separator + "tcprotect" + File.separator);
		if (old.exists()) {
			old.renameTo(new File(pathMain));
		}
	}

	public RegionManager getGlobalRegionManager() {
		return rm;
	}

	public static void disable() {
		Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("tcprotect"));
	}

	//public static void runPurge() {
	//	
	//}

	public static void checkLockTable() {
		Connection conn = null;
		Statement st = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbPath = "jdbc:sqlite:" + pathData + File.separator + "protections.db";
			conn = DriverManager.getConnection(dbPath);
			st = conn.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS protections ("
					+ "id INTEGER NOT NULL PRIMARY KEY," + "owner VARCHAR(20) NOT NULL,"
					+ "world VARCHAR(100) NOT NULL," + "x INTEGER NOT NULL,"
					+ "y INTEGER NOT NULL," + "z INTEGER NOT NULL)");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				st.close();
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}