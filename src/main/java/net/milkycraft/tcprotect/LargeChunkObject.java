package net.milkycraft.tcprotect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LargeChunkObject {
	public Set<Region> regions;

	public LargeChunkObject(HashMap<String, Region> regionValues, Set<String> values) {
		this.regions = new HashSet<>(values.size());
		for (String s : values) {
            this.regions.add(regionValues.get(s));
        }
	}

	public LargeChunkObject() {
		this.regions = new HashSet<>();
	}

	public void addRegion(Region r) {
		if (this.regions == null) {
			this.regions = new HashSet<>(10);
		}
		this.regions.add(r);
	}

	public void removeRegion(Region r) {
		if (this.regions == null) {
			return;
		}
		this.regions.remove(r);
		if (this.regions.size() <= 0) {
            this.regions = null;
        }
	}

	public boolean isNull() {
		return this.regions == null;
	}

	public static int convertBlockToLCO(int i) {
		int ie = i / 512;
		if (ie < 0) {
			ie--;
		}
		return ie;
	}

	public static long getBlockLCOLong(int x, int z) {
		int xe = x / 512;
		if (xe < 0) {
			xe--;
		}
		int ze = z / 512;
		if (ze < 0) {
			ze--;
		}
		return Location2I.getXZLong(xe, ze);
	}

	public static long getChunkLCOLong(int x, int z) {
		int xe = x / 32;
		if (xe < 0) {
			xe--;
		}
		int ze = x / 32;
		if (ze < 0) {
			ze--;
		}
		return Location2I.getXZLong(xe, ze);
	}
}