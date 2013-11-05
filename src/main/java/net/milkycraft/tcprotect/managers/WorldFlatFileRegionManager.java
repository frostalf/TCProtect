package net.milkycraft.tcprotect.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import net.milkycraft.tcprotect.FileType;
import net.milkycraft.tcprotect.LargeChunkObject;
import net.milkycraft.tcprotect.Location2I;
import net.milkycraft.tcprotect.TCUtility;
import net.milkycraft.tcprotect.TCProtect;
import net.milkycraft.tcprotect.Region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

public class WorldFlatFileRegionManager implements WorldRegionManager {
	HashMap<Long, LargeChunkObject> regions = new HashMap<>(100);
	World world;

	public WorldFlatFileRegionManager(World world) {
		this.world = world;
	}

	@Override
	public void add(Region r) {
		int cMaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
		int cMaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
		int cMinX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
		int cMinZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
		for (int xl = cMinX; xl <= cMaxX; xl++) {
			for (int zl = cMinZ; zl <= cMaxZ; zl++) {
				LargeChunkObject lco = this.regions.get(Long.valueOf(Location2I.getXZLong(xl, zl)));
				if (lco == null) {
					lco = new LargeChunkObject();
				}
				lco.addRegion(r);
				this.regions.put(Long.valueOf(Location2I.getXZLong(xl, zl)), lco);
			}
		}
		save();
	}

	@Override
	public void remove(Region r) {
		int cMaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
		int cMaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
		int cMinX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
		int cMinZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
		for (int xl = cMinX; xl <= cMaxX; xl++) {
			for (int zl = cMinZ; zl <= cMaxZ; zl++) {
				LargeChunkObject lco = this.regions.get(Long.valueOf(Location2I.getXZLong(xl, zl)));
				if (lco != null) {
					lco.removeRegion(r);
				}
			}
		}
		save();
	}

	@Override
	public boolean canBuild(Player p, Block b) {
		int bx = b.getX();
		int bz = b.getZ();
		LargeChunkObject lco = this.regions.get(Long.valueOf(LargeChunkObject.getBlockLCOLong(bx,
				bz)));
		if (lco == null) {
			return true;
		}
		if (lco.regions == null) {
			return true;
		}
		Iterator<?> i = lco.regions.iterator();
		while (i.hasNext()) {
			Region poly = (Region) i.next();
			if ((poly.inBoundingRect(bx, bz)) && (poly.intersects(bx, bz))) {
				return poly.canBuild(p);
			}
		}

		return true;
	}

	@Override
	public Set<Region> getRegions(Player p) {
		return getRegions(p.getName());
	}

	@Override
	public Set<Region> getRegions(String p) {
		Set<Region> ls = new HashSet<>();
		for (LargeChunkObject lco : this.regions.values()) {
			if (lco == null) {
				continue;
			}
			if (lco.regions == null) {
				continue;
			}
			for (Region r : lco.regions) {
				if (r.getCreator().equalsIgnoreCase(p)) {
					ls.add(r);
				}
			}
		}
		return ls;
	}

	@Override
	public boolean regionExists(Block b) {
		return regionExists(b.getX(), b.getZ());
	}

	@Override
	public boolean regionExists(int x, int z) {
		LargeChunkObject lco = this.regions
				.get(Long.valueOf(LargeChunkObject.getBlockLCOLong(x, z)));
		if ((lco == null) || (lco.regions == null)) {
			return false;
		}
		if (lco != null) {
			Iterator<?> i = lco.regions.iterator();
			while (i.hasNext()) {
				Region poly = (Region) i.next();
				if ((poly.inBoundingRect(x, z)) && (poly.intersects(x, z))) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public Region getRegion(Location l) {
		int x = l.getBlockX();
		int z = l.getBlockZ();
		return getRegion(x, z);
	}

	private Region getRegion(int x, int z) {
		LargeChunkObject lco = this.regions
				.get(Long.valueOf(LargeChunkObject.getBlockLCOLong(x, z)));
		if ((lco == null) || (lco.regions == null)) {
			return null;
		}
		Iterator<?> i = lco.regions.iterator();
		while (i.hasNext()) {
			Region poly = (Region) i.next();
			if ((poly.inBoundingRect(x, z)) && (poly.intersects(x, z))) {
				return poly;
			}
		}

		return null;
	}

	@Override
	public Region getRegion(Player p) {
		return getRegion(p.getLocation());
	}

	@Override
	public Region getRegion(String name) {
		if (name == null) {
            return null;
        }
		for (LargeChunkObject lco : this.regions.values()) {
			if ((lco == null) || (lco.regions == null)) {
				continue;
			}
			Iterator<?> ri = lco.regions.iterator();
			while (ri.hasNext()) {
				Region r = (Region) ri.next();
				if (r.getName().equalsIgnoreCase(name)) {
					return r;
				}
			}
		}
		return null;
	}

	@Override
	public void save() {
		try {
			TCProtect.logger.debug("RegionManager.Save(): File type is "
					+ TCProtect.fileType.toString());
			String world = getWorld().getName();
			File datf = new File(TCProtect.pathData, "data_" + world + ".regions");
			if (!datf.exists()) {
				datf.createNewFile();
			}
			HashMap<Long, Set<String>> lcos = new HashMap<>(this.regions.size());
			for (Entry<Long, LargeChunkObject> e : this.regions.entrySet()) {
				if ((e == null) || (e.getValue() == null) || (e.getValue().regions == null)) {
					continue;
				}
				HashSet<String> newRegions = new HashSet<>();
				for (Region r : e.getValue().regions) {
					newRegions.add(r.getName());
				}
				lcos.put(e.getKey(), newRegions);
			}
			HashMap<String, Region> newRegions = new HashMap<>();
			for (LargeChunkObject lco : this.regions.values()) {
				if (lco.regions == null) {
					continue;
				}
				for (Region r : lco.regions) {
					newRegions.put(r.getName(), r);
				}
			}
			switch (FileType.values()[TCProtect.fileType.ordinal()]) {
			case YML:
				backupRegions(datf);
				Yaml yml = new Yaml();
				yml.dump(newRegions, new FileWriter(datf));
				break;
			case YMLGZ:
				backupRegions(datf);
				Yaml ymlgz = new Yaml();
				ymlgz.dump(this.regions, new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(datf))));
				break;
			case OOS:
				backupRegions(datf);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datf));
				oos.writeObject(lcos);
				oos.writeObject(newRegions);
				oos.close();
				break;
			case OOSGZ:
				backupRegions(datf);
				ObjectOutputStream oosgz = new ObjectOutputStream(new GZIPOutputStream(
						new FileOutputStream(datf)));
				oosgz.writeObject(lcos);
				oosgz.writeObject(newRegions);
				oosgz.close();
			case MYSQL:
				break;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void backupRegions(File datf) {
		if (!TCProtect.backup) {
            return;
        }
		File dataBackup = new File(TCProtect.pathData, "data_" + getWorld().getName()
				+ ".regions.backup");
		dataBackup.delete();
		datf.renameTo(dataBackup);
		try {
			datf.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getTotalRegionSize(String p) {
		if (p == null) {
            return 0;
        }
		int total = 0;
		Set<Region> regions = new HashSet<>();
		for (LargeChunkObject lco : this.regions.values()) {
			if (lco.regions == null) {
				continue;
			}
			for (Region r : lco.regions) {
				regions.add(r);
			}
		}
		for (Region r : regions) {
			if (r.getCreator().equals(p)) {
				total += r.getArea();
			}
		}
		return total;
	}

	@Override
	public boolean isSurroundingRegion(Region r) {
		if (r == null) {
            return false;
        }
		for (LargeChunkObject lco : getRegionLcos(r)) {
			if ((lco == null) || (lco.regions == null)) {
				continue;
			}
			for (Region other : lco.regions) {
				if ((other == null) || (!r.inBoundingRect(other.getCenterX(), other.getCenterZ()))
						|| (!r.intersects(other.getCenterX(), other.getCenterZ()))) {
                    continue;
                }
				System.out.println("Intersecting!: " + other.info());
				return true;
			}

		}

		return false;
	}

	@Override
	public void load() {
		String world = getWorld().getName();
		load(TCProtect.pathData + "data_" + world + ".regions");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void load(String path) {
		String world = getWorld().getName();
		String datbackf = TCProtect.pathData + "data_" + world + ".regions.backup";
		File f;
		if (!(f = new File(path)).exists()) {
            try {
                f.createNewFile();
                TCProtect.logger.info("Created new world region file: (" + path + ").");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		try {
			if (!TCUtility.isFileEmpty(path)) {
				ObjectInputStream ois = null;
				switch (FileType.values()[TCProtect.fileType.ordinal()]) {
				case YML:
					TCProtect.logger.debug("RegionManager.load() File type: yml");
					Yaml yml = new Yaml();
					Object oyml;
					if (!((oyml = yml.load(new FileInputStream(path))) instanceof HashMap)) {
                    break;
                }
					this.regions = ((HashMap<Long, LargeChunkObject>) oyml);

					break;
				case YMLGZ:
					TCProtect.logger.debug("RegionManager.load() File type: ymlgz");
					Yaml ymlgz = new Yaml();
					Object oymlgz;
					if (!((oymlgz = ymlgz.load(new GZIPInputStream(new FileInputStream(path)))) instanceof HashMap)) {
                    break;
                }
					this.regions = ((HashMap<Long, LargeChunkObject>) oymlgz);

					break;
				case OOSGZ:
					ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(path)));
				case OOS:
					TCProtect.logger.debug("RegionManager.load() File type: oos");
					if (ois == null) {
						ois = new ObjectInputStream(new FileInputStream(path));
					}
					Object oois = ois.readObject();
					HashMap<Long, Set> lcos;
					if ((oois instanceof HashMap)) {
                    lcos = (HashMap<Long, Set>) oois;
                }
					else {
						lcos = null;
					}

					oois = ois.readObject();
					HashMap<String, Region> newRegions;
					if ((oois instanceof HashMap)) {
                    newRegions = (HashMap<String, Region>) oois;
                }
					else {
						newRegions = null;
					}
					ois.close();
					this.regions = new HashMap<>(lcos.size());
					for (Entry<Long, Set> ss : lcos.entrySet()) {
                    this.regions.put(ss.getKey(),
                            new LargeChunkObject(newRegions, ss.getValue()));
                }
				default:
					break;
				}
			} else {
				if ((TCProtect.backup) && (backupExists()) && (!path.equalsIgnoreCase(datbackf))) {
					load(datbackf);
					TCProtect.logger.info("Main data file is blank, Reading from backup.");
					return;
				}
				TCProtect.logger.info("Creating a new data file.");
				this.regions = new HashMap<>();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ZipException e) {
			if ((TCProtect.backup) && (backupExists()) && (!path.equalsIgnoreCase(datbackf))) {
				load(datbackf);
				TCProtect.logger.info("The data file is corrupt. Loading from backup.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean backupExists() {
		String world = getWorld().getName();
		String datbackf = TCProtect.pathData + "data_" + world + ".regions.backup";
		return new File(datbackf).exists();
	}

	@Override
	public Set<Region> getRegionsNear(Player player, int area) {
		int px = player.getLocation().getBlockX();
		int pz = player.getLocation().getBlockZ();
		int cmaxX = LargeChunkObject.convertBlockToLCO(px + area);
		int cmaxZ = LargeChunkObject.convertBlockToLCO(pz + area);
		int cminX = LargeChunkObject.convertBlockToLCO(px - area);
		int cminZ = LargeChunkObject.convertBlockToLCO(pz - area);
		Set<Region> ret = new HashSet<>();
		for (int xl = cminX; xl <= cmaxX; xl++) {
			for (int zl = cminZ; zl <= cmaxZ; zl++) {
				LargeChunkObject lco = this.regions.get(Long.valueOf(Location2I.getXZLong(xl, zl)));
				if ((lco == null) || (lco.regions == null)) {
					continue;
				}
				Iterator<?> i = lco.regions.iterator();
				while (i.hasNext()) {
					Region poly = (Region) i.next();
					if ((Math.abs(poly.getCenterX() - px) <= area)
							&& (Math.abs(poly.getCenterZ() - pz) <= area)) {
						ret.add(poly);
					}
				}
			}
		}
		return ret;
	}

	@Override
	public boolean regionExists(Region region) {
		if (region == null) {
			return false;
		}
		for (LargeChunkObject lco : this.regions.values()) {
			if (lco.regions == null) {
				continue;
			}
			Iterator<?> ri = lco.regions.iterator();
			while (ri.hasNext()) {
				Region r = (Region) ri.next();
				if ((r != null) && (r.getName().equalsIgnoreCase(region.getName()))) {
					return true;
				}
			}
		}

		return false;
	}

	public World getWorld() {
		return this.world;
	}

	@Override
	public void setFlagValue(Region region, int flag, boolean value) {
		region.setFlag(flag, value);
	}

	@Override
	public void setRegionName(Region region, String name) {
		region.setName(name);
	}

	@Override
	public Set<Region> getPossibleIntersectingRegions(Region r) {
		Set<Region> ret = new HashSet<>();
		if (r == null) {
			return ret;
		}
		int cmaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
		int cmaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
		int cminX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
		int cminZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
		for (int xl = cminX; xl <= cmaxX; xl++) {
			for (int zl = cminZ; zl <= cmaxZ; zl++) {
				LargeChunkObject lco = this.regions.get(Long.valueOf(Location2I.getXZLong(xl, zl)));
				if ((lco == null) || (lco.regions == null)) {
					continue;
				}
				Iterator<?> i = lco.regions.iterator();
				while (i.hasNext()) {
					Region other = (Region) i.next();
					if (r.inBoundingRect(other)) {
						ret.add(other);
					}
				}
			}
		}
		return ret;
	}

	public List<LargeChunkObject> getRegionLcos(Region r) {
		List<LargeChunkObject> ret = new LinkedList<>();
		int cmaxX = LargeChunkObject.convertBlockToLCO(r.getMaxMbrX());
		int cmaxZ = LargeChunkObject.convertBlockToLCO(r.getMaxMbrZ());
		int cminX = LargeChunkObject.convertBlockToLCO(r.getMinMbrX());
		int cminZ = LargeChunkObject.convertBlockToLCO(r.getMinMbrZ());
		for (int xl = cminX; xl <= cmaxX; xl++) {
			for (int zl = cminZ; zl <= cmaxZ; zl++) {
				LargeChunkObject lco = this.regions.get(Long.valueOf(Location2I.getXZLong(xl, zl)));
				if ((lco == null) || (lco.regions == null)) {
					continue;
				}
				ret.add(lco);
			}
		}
		return ret;
	}
}