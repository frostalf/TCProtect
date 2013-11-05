package net.milkycraft.tcprotect;

import java.util.LinkedList;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DefineRegionBuilder extends RegionBuilder {
	public DefineRegionBuilder(Player p, Location loc1, Location loc2, String name, String creator) {
		String copy = name;
		String pName = p.getName();
		if (copy.length() == 0) {
			for (int i = 0;; i++) {
				if (p.getName().length() > 13) {
                    copy = p.getName().substring(0, 13) + "_" + i;
                }
				else {
					copy = p.getName() + "_" + i;
				}
				if (TCProtect.rm.getRegion(copy, p.getWorld()) == null) {
					if (copy.length() <= 16) {
                        break;
                    }
					p.sendMessage(ChatColor.RED
							+ "Couldn't generate automatic region regionName, please regionName it yourself.");
					return;
				}
			}

		}

		if ((loc1 == null) || (loc2 == null)) {
			p.sendMessage(ChatColor.RED + "One or both of your selection positions aren't set!");
			return;
		}
		if (TCProtect.rm.getRegion(copy, p.getWorld()) != null) {
			p.sendMessage(ChatColor.RED
					+ "That regionName is already taken, please choose another one.");
			return;
		}
		if ((copy.length() < 2) || (copy.length() > 16)) {
			p.sendMessage(ChatColor.RED
					+ "Invalid regionName, place a 2-16 character regionName in the 2nd row.");
			return;
		}
		int maxX;
		int minX;
		if (loc2.getBlockX() < loc1.getBlockX()) {
			minX = loc2.getBlockX();
			maxX = loc1.getBlockX();
		} else {
			maxX = loc2.getBlockX();
			minX = loc1.getBlockX();
		}
		int maxZ;
		int minZ;
		if (loc2.getBlockZ() < loc1.getBlockZ()) {
			minZ = loc2.getBlockZ();
			maxZ = loc1.getBlockZ();
		} else {
			maxZ = loc2.getBlockZ();
			minZ = loc1.getBlockZ();
		}
		for (int xl = minX; xl <= maxX; xl++) {
			if ((TCProtect.rm.regionExists(xl, minZ, p.getWorld()))
					|| (TCProtect.rm.regionExists(xl, maxZ, p.getWorld()))) {
				setError(p, ChatColor.RED + "You're overlapping another region.");
				return;
			}
		}
		for (int zl = minZ; zl <= maxZ; zl++) {
			if ((TCProtect.rm.regionExists(minX, zl, p.getWorld()))
					|| (TCProtect.rm.regionExists(maxX, zl, p.getWorld()))) {
				setError(p, ChatColor.RED + "You're overlapping another region.");
				return;
			}
		}
		LinkedList<String> owners = new LinkedList<>();
		owners.add(creator);
		if (!pName.equals(creator)) {
			owners.add(pName);
		}
		Region r = new Region(copy, owners, new int[] { loc1.getBlockX(), loc1.getBlockX(),
				loc2.getBlockX(), loc2.getBlockX() }, new int[] { loc1.getBlockZ(),
				loc1.getBlockZ(), loc2.getBlockZ(), loc2.getBlockZ() });
		if (TCProtect.rm.isSurroundingRegion(r, p.getWorld())) {
			p.sendMessage(ChatColor.RED + "You're overlapping another region.");
			return;
		}
		this.r = r;
	}
}