package net.milkycraft.tcprotect;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class EncompassRegionBuilder extends RegionBuilder {
	public EncompassRegionBuilder(SignChangeEvent e) {
		String owner1 = e.getLine(2);
		String owner2 = e.getLine(3);
		Block b = e.getBlock();
		World w = b.getWorld();
		Player p = e.getPlayer();
		String pName = p.getName();
		Block last = b;
		Block current = b;
		Block next = null;
		Block first = null;
		String regionName = e.getLine(1);
		List<Integer> px = new LinkedList<>();
		List<Integer> pz = new LinkedList<>();
		Block bFirst1 = null;
		Block bFirst2 = null;
		List<Block> redstone = new LinkedList<>();
		int oldFacing = 0;
		int curFacing = 0;
		if (regionName.length() == 0) {
			for (int i = 0;; i++) {
				if (pName.length() > 13) {
                    regionName = pName.substring(0, 13) + "_" + i;
                }
				else {
					regionName = pName + "_" + i;
				}
				if (TCProtect.rm.getRegion(regionName, w) == null) {
					if (regionName.length() <= 16) {
                        break;
                    }
					setErrorSign(e,
							"Couldn't generate automatic region name, please name it yourself.");
					return;
				}
			}
		}

		if (TCProtect.rm.getRegion(regionName, w) != null) {
			setErrorSign(e, "That name is already taken, please choose another one.");
			return;
		}
		if ((regionName.length() < 2) || (regionName.length() > 16)) {
			setErrorSign(e, "Invalid name, place a 2-16 character name in the 2nd row.");
			return;
		}
		if (regionName.contains(" ")) {
			setErrorSign(e, "The name of the region can't have a space in it.");
			return;
		}
		for (int i = 0; i < TCProtect.maxScan; i++) {
			int nearbyCount = 0;
			int x = current.getX();
			int y = current.getY();
			int z = current.getZ();
			int blockSize = 6;
			Block[] block;
			if (TCProtect.blockID == 55) {
				block = new Block[12];
				blockSize = 12;
				block[0] = w.getBlockAt(x + 1, y, z);
				block[1] = w.getBlockAt(x - 1, y, z);
				block[2] = w.getBlockAt(x, y, z + 1);
				block[3] = w.getBlockAt(x, y, z - 1);
				block[4] = w.getBlockAt(x + 1, y + 1, z);
				block[5] = w.getBlockAt(x - 1, y + 1, z);
				block[6] = w.getBlockAt(x, y + 1, z + 1);
				block[7] = w.getBlockAt(x, y + 1, z - 1);
				block[8] = w.getBlockAt(x + 1, y - 1, z);
				block[9] = w.getBlockAt(x - 1, y - 1, z);
				block[10] = w.getBlockAt(x, y - 1, z + 1);
				block[11] = w.getBlockAt(x, y - 1, z - 1);
			} else if ((TCProtect.blockID == 85) || (TCProtect.blockID == 101)
					|| (TCProtect.blockID == 113)) {
				block = new Block[6];
				blockSize = 6;
				block[0] = w.getBlockAt(x + 1, y, z);
				block[1] = w.getBlockAt(x - 1, y, z);
				block[2] = w.getBlockAt(x, y, z + 1);
				block[3] = w.getBlockAt(x, y, z - 1);
				block[4] = w.getBlockAt(x, y - 1, z);
				block[5] = w.getBlockAt(x, y + 1, z);
			} else {
				block = new Block[6];
				blockSize = 6;
				block[0] = w.getBlockAt(x + 1, y, z);
				block[1] = w.getBlockAt(x - 1, y, z);
				block[2] = w.getBlockAt(x, y, z + 1);
				block[3] = w.getBlockAt(x, y, z - 1);
				block[4] = w.getBlockAt(x, y - 1, z);
				block[5] = w.getBlockAt(x, y + 1, z);
			}

			for (int bi = 0; bi < blockSize; bi++) {
				boolean validBlock = false;
				if ((TCProtect.blockID == 85)
						&& ((block[bi].getType().equals(Material.FENCE)) || (block[bi].getType()
								.equals(Material.FENCE_GATE)))) {
                    validBlock = true;
                }
				else {
					validBlock = block[bi].getTypeId() == TCProtect.blockID;
				}
				if ((validBlock) && (!block[bi].getLocation().equals(last.getLocation()))) {
					nearbyCount++;
					next = block[bi];
					curFacing = bi % 4;
					if (i == 1) {
						if (nearbyCount == 1) {
							bFirst1 = block[bi];
						}
						if (nearbyCount == 2) {
							bFirst2 = block[bi];
						}
					}
				}
			}
			if (nearbyCount == 1) {
				if (i != 0) {
					redstone.add(current);
					if (current.equals(first)) {
						List<String> owners = new LinkedList<>();
						owners.add(pName);
						if (owner1.length() != 0) {
							if (owner1.contains(" ")) {
								setErrorSign(e, "The first sign owner's name is invalid.");
								return;
							}
							if (owner1.equals(pName)) {
                                p.sendMessage(ChatColor.YELLOW
                                        + "[RP]: You don't need to enter your name manually, it's added automatically.");
                            }
							else {
								owners.add(owner1);
							}
						}
						if (owner1.length() != 0) {
							if (owner2.contains(" ")) {
								setErrorSign(e, "The second sign owner's name is invalid.");
								return;
							}
							if (owner2.equals(pName)) {
                                p.sendMessage(ChatColor.YELLOW
                                        + "[RP]: You don't need to enter your name manually, it's added automatically.");
                            }
							else {
								owners.add(owner2);
							}
						}
						int[] rx = new int[px.size()];
						int[] rz = new int[pz.size()];
						int bl = 0;
						for (Iterator<Integer> localIterator1 = px.iterator(); localIterator1
								.hasNext();) {
							int bx = localIterator1.next().intValue();
							rx[bl] = bx;
							bl++;
						}
						bl = 0;
						for (Iterator<Integer> localIterator1 = pz.iterator(); localIterator1
								.hasNext();) {
							int bz = localIterator1.next().intValue();
							rz[bl] = bz;
							bl++;
						}

						for (Block ib : redstone) {
							Region other = TCProtect.rm.getRegion(ib.getLocation());
							if (other != null) {
								setErrorSign(e,
										"You're overlapping another region. (" + other.getName()
												+ ")");
								return;
							}
						}
						Region region = new Region(regionName, owners, rx, rz);
						if (TCProtect.rm.isSurroundingRegion(region, w)) {
							setErrorSign(e, "You're completely surrounding another region.");
							return;
						}
						int pLimit = TCProtect.ph.getPlayerLimit(p);
						boolean areaUnlimited = TCProtect.ph.hasPerm(p, "tcprotect.unlimited");
						int totalArea = TCProtect.rm.getRegions(p).size();
						if ((pLimit >= 0) && (totalArea + 1 > pLimit) && (!areaUnlimited)) {
							setErrorSign(e,
									"You can't make any more regions because you've reached the maximum area alotted per player.");
							return;
						}

						p.sendMessage(ChatColor.AQUA
								+ "Your area used: "
								+ (totalArea + region.getArea())
								+ ", left: "
								+ ChatColor.GOLD
								+ (areaUnlimited ? "unlimited" : Integer.valueOf(pLimit
										- (totalArea + region.getArea()))) + ChatColor.AQUA + ".");
						if (TCProtect.removeBlocks) {
							b.breakNaturally();
							for (Block rb : redstone) {
								rb.breakNaturally();
							}
						}
						this.r = region;
						return;
					}
				}
			} else if ((i == 1) && (nearbyCount == 2)) {
				redstone.add(current);
				first = current;
				int x1 = bFirst1.getX();
				int z1 = bFirst1.getZ();
				int x2 = bFirst2.getX();
				int z2 = bFirst2.getZ();
				int distx = Math.abs(x1 - x2);
				int distz = Math.abs(z1 - z2);
				if (((distx != 2) || (distz != 0)) && ((distz != 2) || (distx != 0))) {
					px.add(Integer.valueOf(current.getX()));
					pz.add(Integer.valueOf(current.getZ()));
				}
			} else if (i != 0) {
				setErrorSign(
						e,
						"Error in your area at: (x: "
								+ current.getX()
								+ ", y: "
								+ current.getY()
								+ ", z: "
								+ current.getZ()
								+ "). Press f3 to look there for those coordinates and make sure there isn't 3 blocks touching.");
				return;
			}

			if ((oldFacing != curFacing) && (i > 1)) {
				px.add(Integer.valueOf(current.getX()));
				pz.add(Integer.valueOf(current.getZ()));
			}

			last = current;
			if (next == null) {
				setErrorSign(e,
						"Put your sign next to the block you want. There is no viable block next to your sign.");
				return;
			}
			current = next;
			oldFacing = curFacing;
		}
		setErrorSign(e, "That area is too big!");
	}
}