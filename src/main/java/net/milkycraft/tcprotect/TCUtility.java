package net.milkycraft.tcprotect;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.Server;

public class TCUtility {
	public static TCProtect plugin;

	public static void init(TCProtect plugin) {
		TCUtility.plugin = plugin;
	}

	public static boolean isFileEmpty(String s) {
		File f = new File(s);
		if (!f.isFile()) {
			return true;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(s);
			int b = fis.read();
			if (b != -1) {
                return false;
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	static Server getServer() {
		return plugin.getServer();
	}

	public static int[] toIntArray(List<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer e : list) {
            ret[(i++)] = e.intValue();
        }
		return ret;
	}
}