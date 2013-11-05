package net.milkycraft.tcprotect;

public class Location2I {
	final int x;
	final int z;

	public Location2I(int x, int z) {
		this.x = x;
		this.z = z;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash *= (17 + this.x);
		hash *= (29 + this.z);
		return hash;
	}

	public long longValue() {
		return this.x << 16 | this.z;
	}

	public static long getXZLong(int x, int z) {
		return x << 16 | z;
	}

	public Location2I getLocationFromLong(long l) {
		return new Location2I((int) (l >> 16), (int) (l & 0x7FFFFFFF));
	}
}