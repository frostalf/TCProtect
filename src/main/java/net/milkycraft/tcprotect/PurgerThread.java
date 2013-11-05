package net.milkycraft.tcprotect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

public class PurgerThread extends Thread {

	OfflinePlayer[] list;
	static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
	private HashMap<String, TimeUnit> times = new HashMap<>();
	private TimeUnit unit = TimeUnit.DAYS;

	public PurgerThread() {
		this.list = Bukkit.getOfflinePlayers();
		times.put("ms", TimeUnit.MILLISECONDS);
		times.put("s", TimeUnit.SECONDS);
		times.put("m", TimeUnit.MINUTES);
		times.put("h", TimeUnit.HOURS);
		times.put("d", TimeUnit.DAYS);
		if (times.containsKey(TCProtect.timeUnit)) {
            unit = times.get(TCProtect.timeUnit);
        }
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.MILLISECOND, (int)System.currentTimeMillis());
		Date date = c.getTime();
		Set<Region> all = new HashSet<>();
		for (OfflinePlayer op : list) {
			all.addAll(TCProtect.rm.getRegions(op.getName()));
		}
		TCProtect.logger.info("Filtering through " + all.size() + " regions!");
		TCProtect.logger.info("Purging all regions with last activity before " + sdf.format(date));
		List<String> exempt = new ArrayList<>();
		try {
			File f = new File(TCProtect.plugin.getDataFolder(), "purgeexempt.yml");
			YamlConfiguration y = new YamlConfiguration();
			y.load(f);
			exempt = y.getStringList("exempt");
		}
		catch (Exception ex){
			ex.printStackTrace();
		}
		for (Region reg : all) {
			if (!exempt.contains(reg.getName())){
				Date last = new Date(reg.lastUsed());
				if (getDateDiff(last, date, unit) > TCProtect.timeBack) {
					reg.expire();
				}
			}
		}
		long end = System.currentTimeMillis();
		TCProtect.logger.info("Finished the purge (took " + (end - start) + " milleseconds!)");
	}

	private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
		long diffInMillies = date2.getTime() - date1.getTime();
		return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
	}

	public static void addPlayers(List<String> p, String r){
		File f = new File(TCProtect.plugin.getDataFolder(), "purge.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			for (String pl : p){
				List<String> list = y.getStringList(pl);
				list.add(r);
				y.set(pl, list);
			}
			y.save(f);
		}
		catch (Exception ex){
			ex.printStackTrace();
			TCProtect.logger.warning("Failed to add player " + p + " to purge alert list!");
		}
	}

	public static void removePlayer(String p){
		File f = new File(TCProtect.plugin.getDataFolder(), "purge.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			if (y.isSet(p)) {
                y.set(p, null);
            }
			y.save(f);
		}
		catch (Exception ex){
			ex.printStackTrace();
			TCProtect.logger.warning("Failed to remove player " + p + " to purge alert list!");
		}
	}

	public static List<String> alertRegions(String p){
		File f = new File(TCProtect.plugin.getDataFolder(), "purge.yml");
		YamlConfiguration y = new YamlConfiguration();
		try {
			y.load(f);
			return y.getStringList(p);
		}
		catch (Exception ex){
			ex.printStackTrace();
			TCProtect.logger.warning("Failed to check purge alert list!");
		}
		return new ArrayList<>();
	}
}
