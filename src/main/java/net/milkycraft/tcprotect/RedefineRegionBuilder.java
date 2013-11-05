package net.milkycraft.tcprotect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RedefineRegionBuilder extends RegionBuilder {
	public RedefineRegionBuilder(Player p, Region old, Location l1, Location l2) {
		if ((l1 == null) || (l2 == null)) {
			setError(p, "One or both of your selection positions aren't set!");
			return;
		}
		World w = p.getWorld();
		int maxX;
		int minX;
		if (l2.getBlockX() < l1.getBlockX()) {
			minX = l2.getBlockX();
			maxX = l1.getBlockX();
		} else {
			maxX = l2.getBlockX();
			minX = l1.getBlockX();
		}
		int maxZ;
		int minZ;
		if (l2.getBlockZ() < l1.getBlockZ()) {
			minZ = l2.getBlockZ();
			maxZ = l1.getBlockZ();
		} else {
			maxZ = l2.getBlockZ();
			minZ = l1.getBlockZ();
		}
		for (int xl = minX; xl <= maxX; xl++) {
			if ((TCProtect.rm.regionExists(xl, minZ, w))
					|| (TCProtect.rm.regionExists(xl, maxZ, w))) {
				p.sendMessage(ChatColor.RED + "You're overlapping another region.");
				return;
			}
		}
		for (int zl = minZ; zl <= maxZ; zl++) {
			if ((TCProtect.rm.regionExists(minX, zl, w))
					|| (TCProtect.rm.regionExists(maxX, zl, w))) {
				setError(p, "You're overlapping another region.");
				return;
			}
		}
		Region r = new Region(old.getName(), old.getOwners(), new int[] { l1.getBlockX(),
				l1.getBlockX(), l2.getBlockX(), l2.getBlockX() }, new int[] { l1.getBlockZ(),
				l1.getBlockZ(), l2.getBlockZ(), l2.getBlockZ() });
		if (TCProtect.rm.isSurroundingRegion(r, w)) {
			setError(p, "You're overlapping another region.");
			return;
		}
		r.f = (old.f.clone());
		this.r = r;
	}
}