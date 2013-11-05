package net.milkycraft.tcprotect.managers;

import java.util.Set;

import net.milkycraft.tcprotect.Region;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract interface WorldRegionManager {
	public abstract void load();

	public abstract void save();

	public abstract Region getRegion(String paramString);

	public abstract int getTotalRegionSize(String paramString);

	public abstract Set<Region> getRegions(Player paramPlayer);

	public abstract Set<Region> getRegionsNear(Player paramPlayer, int paramInt);

	public abstract Set<Region> getRegions(String paramString);

	public abstract Region getRegion(Player paramPlayer);

	public abstract void add(Region paramRegion);

	public abstract void remove(Region paramRegion);

	public abstract boolean canBuild(Player paramPlayer, Block paramBlock);

	public abstract boolean isSurroundingRegion(Region paramRegion);

	public abstract boolean regionExists(Block paramBlock);

	public abstract Region getRegion(Location paramLocation);

	public abstract boolean regionExists(Region paramRegion);

	public abstract void setFlagValue(Region paramRegion, int paramInt, boolean paramBoolean);

	public abstract void setRegionName(Region paramRegion, String paramString);

	public abstract boolean regionExists(int paramInt1, int paramInt2);

	public abstract Set<Region> getPossibleIntersectingRegions(Region paramRegion);
}