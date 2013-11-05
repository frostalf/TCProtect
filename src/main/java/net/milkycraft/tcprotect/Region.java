package net.milkycraft.tcprotect;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.UnhandledException;

import static org.bukkit.ChatColor.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Region implements Serializable {
	private static final long serialVersionUID = 3904371508520551177L;
	private long creationDate;
	private long lastUsed;
	private int[] x;
	private int[] z;
	private int minMbrX = 0;
	private int maxMbrX = 0;
	private int minMbrZ = 0;
	private int maxMbrZ = 0;
	private String name;
	private List<SerialLocation> fences = new ArrayList<>();
	private List<String> owners;
	private List<String> members;
	private String creator = "";

	/*
	 * 0 - PvP 1 - Chest 2 - Lever 3 - Button 4 - Door 5 - Mobs 6 - Animals 7 -
	 * Potions 8 - Invincible 9 - Super Protect 10 - Homes 13 - Snow Form
	 * 14 - Ice Melt
	 */
	protected boolean[] f = { Flags.pvp, Flags.chest, Flags.lever, Flags.button, Flags.door,
			Flags.mobs, Flags.animals, Flags.potions, Flags.invincible, Flags.superProtect,
			Flags.homes, Flags.snowForm, Flags.iceMelt };

	public void setFlag(int flag, boolean value) {
		if (flag > this.f.length) {
			return;
		}
		this.f[flag] = value;
	}

	public void setX(int[] x) {
		this.x = x;
	}

	public void setZ(int[] z) {
		this.z = z;
	}

	public void setOwners(List<String> owners) {
		this.owners = owners;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public void setCreator(String s) {
		this.creator = s;
	}

	public int[] getX() {
		return this.x;
	}

	public int[] getZ() {
		return this.z;
	}

	public String getCreator() {
		return this.creator;
	}

	public String getName() {
		return this.name;
	}

	public List<String> getOwners() {
		return this.owners;
	}

	public List<String> getMembers() {
		return this.members;
	}

	public int getCenterX() {
		return (this.minMbrX + this.maxMbrX) / 2;
	}

	public int getCenterZ() {
		return Double.valueOf((this.minMbrZ + this.maxMbrZ) / 2.0D).intValue();
	}

	public int getMaxMbrX() {
		return this.maxMbrX;
	}

	public int getMinMbrX() {
		return this.minMbrX;
	}

	public int getMaxMbrZ() {
		return this.maxMbrZ;
	}

	public int getMinMbrZ() {
		return this.minMbrZ;
	}

	public String info() {
		String ownerstring = "";
		String memberstring = "";
		for (int i = 0; i < this.owners.size(); i++) {
			ownerstring = ownerstring + ", " + this.owners.get(i);
		}
		for (int i = 0; i < this.members.size(); i++) {
			memberstring = memberstring + ", " + this.members.get(i);
		}
		if (this.owners.size() > 0) {
            ownerstring = ownerstring.substring(2);
        }
		else {
			ownerstring = "None";
		}
		if (this.members.size() > 0) {
            memberstring = memberstring.substring(2);
        }
		else {
			memberstring = "None";
		}
		return
				AQUA + "Name: " +
				GOLD + this.name + 
				AQUA + ", Creator: " +
				GOLD + this.creator + 
				AQUA + ", Center: [" +
				GOLD + getCenterX() + 
				AQUA + ", " +
				GOLD + getCenterZ() + 
				AQUA + "], Owners: [" +
				GOLD + ownerstring + 
				AQUA + "], Members: [" +
				GOLD + memberstring + 
				AQUA + "].";
	}

	public Region(int[] x, int[] z, String name, List<String> owners, List<String> members,
			String creator, int maxMbrX, int minMbrX, int maxMbrZ, int minMbrZ, boolean[] flags) {
		this.x = x;
		this.creationDate = System.currentTimeMillis();
		this.lastUsed = this.creationDate;
		this.z = z;
		this.maxMbrX = maxMbrX;
		this.minMbrX = minMbrX;
		this.maxMbrZ = maxMbrZ;
		this.minMbrZ = minMbrZ;
		this.name = name;
		this.owners = owners;
		this.members = members;
		this.creator = creator;
		this.f = flags;
	}

	public Region(String name, List<String> owners, int[] x, int[] z) {
		int size = x.length;
		if (size != z.length) {
			throw new Error("The X & Z arrays are different sizes!");
		}
		this.x = x;
		this.z = z;
		if (size < 4) {
			throw new Error("You can't generate a polygon with less then 4 points!");
		}
		if (size == 4) {
			this.x = null;
			this.z = null;
		}
		this.owners = owners;
		this.creationDate = System.currentTimeMillis();
		this.lastUsed = this.creationDate;
		this.members = new ArrayList<>();
		this.name = name;
		this.creator = (owners.get(0));
		this.maxMbrX = x[0];
		this.minMbrX = x[0];
		this.maxMbrZ = z[0];
		this.minMbrZ = z[0];
		for (int i = 0; i < x.length; i++) {
			if (x[i] > this.maxMbrX) {
				this.maxMbrX = x[i];
			}
			if (x[i] < this.minMbrX) {
				this.minMbrX = x[i];
			}
			if (z[i] > this.maxMbrZ) {
				this.maxMbrZ = z[i];
			}
			if (z[i] < this.minMbrZ) {
                this.minMbrZ = z[i];
            }
		}
	}

	public void delete() {
		TCProtect.rm.remove(this);
		TCProtect.logger.info("Region " + name + " has been deleted!");
	}

	public void addFence(Block b) {
		this.fences.add(new SerialLocation(b.getWorld().getName(), b.getX(), b.getY(), b.getZ()));
	}

	public void expire() {
		TCProtect.logger.info("Setting " + fences.size() + " fence blocks to air!");
		Block bl = null;
		for (SerialLocation loc : fences) {
			Block b = Bukkit.getWorld(loc.world).getBlockAt(loc.x, loc.y, loc.z);
			b.setType(Material.AIR);//;setTypeId(0);
			if (bl != null) {
                return;
			} else {
                bl = b;
            }
		}
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator
					+ "protections.db";
			conn = DriverManager.getConnection(dbPath);
			st = conn.createStatement();
			rs = st.executeQuery("SELECT * FROM protections");
			while (rs.next()){
				if (TCProtect.plugin.getServer().getWorld(rs.getString("world")) != null){
					Region re = TCProtect.rm.getRegion(new Location(
							TCProtect.plugin.getServer().getWorld(rs.getString("world")),
							rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
					if (re != null) {
                        if (re == this) {
                            st.executeUpdate("DELETE FROM protections WHERE " +
                                    "world = '" + rs.getString("world") + "'" +
                                    "AND x = '" + rs.getInt("x") + "'" +
                                    "AND y = '" + rs.getInt("y") + "'" +
                                    "AND z = '" + rs.getInt("z") + "'");
                        }
                    }
				}
			}
		}
		catch (ClassNotFoundException | SQLException ex){
			TCProtect.logger.info("Either Sqlite Class not found or SQL query errored: " + ex.getCause().getMessage());
		}
		finally {
			try {
				st.close();
				rs.close();
                conn.close();
			}
			catch (Exception exc){
                TCProtect.logger.info("Error in Closing SQlite DB connection: " + exc.getCause().getMessage());
			}
		}
		if(this.getOwners() != null || this.name != null){
            try{
        PurgerThread.addPlayers(this.getOwners(), this.name);
		Bukkit.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Region " +
				ChatColor.GOLD + ChatColor.BOLD + this.name
				+ " at " + ChatColor.AQUA + ChatColor.BOLD + bl.getX() + ", " + bl.getZ() +
				ChatColor.DARK_RED + ChatColor.BOLD + " has been purged!");
		this.delete();
            }
            catch (Exception ex){
                TCProtect.logger.info("Either could not get Owner name or Region name: " + ex.getCause().getMessage());
            }
        }
	}

	public int getArea() {
		if (this.x == null) {
			return (this.maxMbrX - this.minMbrX) * (this.maxMbrZ - this.minMbrZ);
		}
		int area = 0;
		for (int i = 0; i < this.x.length; i++) {
			int j = (i + 1) % this.x.length;
			area += this.x[i] * this.z[j] - this.z[i] * this.x[j];
		}
		area = Math.abs(area / 2);
		return area;
	}

	public boolean inBoundingRect(int bx, int bz) {
		return (bx <= this.maxMbrX) && (bx >= this.minMbrX) && (bz <= this.maxMbrZ)
				&& (bz >= this.minMbrZ);
	}

	public Long getCreationDate() {
		return this.creationDate;
	}

	public Long lastUsed() {
		return this.lastUsed;
	}

	public void us3() {
		this.lastUsed = System.currentTimeMillis();
	}

	public boolean inBoundingRect(Region other) {
		if (other.maxMbrX < this.minMbrX) {
			return false;
		}
		if (other.maxMbrZ < this.minMbrZ) {
			return false;
		}
		if (other.minMbrX > this.maxMbrX) {
			return false;
		}

		return other.minMbrZ <= this.maxMbrZ;
	}

	public boolean intersects(int bx, int bz) {
		if (this.x == null) {
			return true;
		}

		boolean ret = false;
		int i = 0;
		for (int j = this.x.length - 1; i < this.x.length; j = i++) {
			if (((this.z[i] > bz) || (bz >= this.z[j]))
					&& ((this.z[j] > bz) || (bz >= this.z[i]) || (bx >= (this.x[j] - this.x[i])
					* (bz - this.z[i]) / (this.z[j] - this.z[i]) + this.x[i]))) {
                continue;
            }
			ret = !ret;
		}

		return ret;
	}

	public boolean isOwner(String p) {
		return this.owners.contains(p);
	}

	public boolean isOwner(Player player) {
		return this.owners.contains(player.getName());
	}

	public boolean isMember(String p) {
		return this.members.contains(p);
	}

	public boolean isMember(Player player) {
		return this.members.contains(player.getName());
	}

	public void addMember(String p) {
		if ((!this.members.contains(p)) && (!this.owners.contains(p))) {
            this.members.add(p);
        }
	}

	public void addOwner(String p) {
		if (this.members.contains(p)) {
			this.members.remove(p);
		}
		if (!this.owners.contains(p)) {
            this.owners.add(p);
        }
	}

	public void removeMember(String p) {
		if (this.members.contains(p)) {
			this.members.remove(p);
		}
		if (this.owners.contains(p)) {
            this.owners.remove(p);
        }
	}

	public void removeOwner(String p) {
		if (this.owners.contains(p)) {
            this.owners.remove(p);
        }
	}

	public boolean getFlag(int flag) {
		return this.f[flag];
	}

	public boolean canBuild(Player p) {
		if (p.getLocation().getY() < TCProtect.heightStart) {
            return true;
        }
		return (isOwner(p)) || (isMember(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.bypass"));
	}

	public boolean canPVP(Player p) {
		if (this.f[0]) {
			return true;
		}
		return (!isSuperProtect() ? TCProtect.ph.hasPerm(p, "tcprotect.bypass") : superUserCheck(p));
	}

	public boolean canChest(Player p) {
		if (this.f[1]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canHome(Player p) {
		if (this.f[12]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.flag.homes")) : superUserCheck(p));
	}

	public boolean canLever(Player p) {
		if (this.f[2]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canButton(Player p) {
		if (this.f[3]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canDoor(Player p) {
		if (this.f[4]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canMobs() {
		return this.f[5];
	}

	public boolean canAnimals(Player p) {
		if (this.f[6]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canPotion(Player p) {
		if (this.f[7]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
	}

	public boolean canCrops(LivingEntity e) {
		if (this.f[9]) {
			return true;
		}
		if (e instanceof Player) {
			Player p = (Player) e;
			return (!isSuperProtect() ? (isOwner(p) || isMember(p))
					|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));
		}
		return false;
	}

	public boolean canItems(Player p) {
		if (this.f[10]) {
			return true;
		}
		return (!isSuperProtect() ? (isOwner(p) || isMember(p))
				|| (TCProtect.ph.hasPerm(p, "tcprotect.bypass")) : superUserCheck(p));

	}

	public static boolean canRegion(Player p) {
		int limit = 0;
		if (p.hasPermission("tcprotect.admin.unlimitedregions")) {
			return true;
		}
		YamlConfiguration yaml = new YamlConfiguration();
		File yamlFile = new File(TCProtect.pathConfig);
		try {
			yaml.load(yamlFile);
			if (p.hasPermission("tcprotect.group.platinum")) {
                limit = yaml.getInt("platinum");
            }
			else if (p.hasPermission("tcprotect.group.exclusive")) {
                limit = yaml.getInt("exclusive");
            }
			else if (p.hasPermission("tcprotect.group.preferred")) {
                limit = yaml.getInt("preferred");
            }
		} catch (Exception ex) {
			Logger.getLogger("Minecraft").warning(
					"[TCProtect] Failed to get region limit from configuration!");
			ex.printStackTrace();
			if (p.isOp()) {
				Logger.getLogger("Minecraft").warning(
						"[TCProtect] Overridden by op status. Region creation allowed.");
				return true;
			}
			return false;
		}
		if (TCProtect.rm.getRegions(p).size() < limit) {
            return true;
        }
		return false;
	}

	public boolean canSnowForm() {
		checkNullFlags();
		return f[13];
	}

	public boolean canIceMelt() {
		checkNullFlags();
		return f[14];
	}

	public boolean isInvincible() {
		return f[8];
	}

	public boolean superUserCheck(Player p) {
		checkNullFlags();
		return (isOwner(p) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.super")));
	}

	public boolean isSuperProtect() {
		checkNullFlags();
		return f[11];
	}

	public void checkNullFlags() {

		if (this.f == null) {
			this.f = new boolean[Flags.class.getFields().length];
			this.f[0] = Flags.pvp;
			this.f[1] = Flags.chest;
			this.f[2] = Flags.lever;
			this.f[3] = Flags.button;
			this.f[4] = Flags.door;
			this.f[5] = Flags.mobs;
			this.f[6] = Flags.animals;
			this.f[7] = Flags.potions;
			this.f[8] = Flags.invincible;
			this.f[9] = Flags.crops;
			this.f[10] = Flags.items;
			this.f[11] = Flags.superProtect;
			this.f[12] = Flags.homes;
			this.f[13] = Flags.snowForm;
			this.f[14] = Flags.iceMelt;
		}
		if (this.f.length != Flags.class.getFields().length) {
			boolean[] newArray = new boolean[Flags.class.getFields().length];
			System.arraycopy(f, 0, newArray, 0, this.f.length);
			f = newArray;
			this.f[7] = Flags.potions;
			this.f[8] = Flags.invincible;
			this.f[9] = Flags.crops;
			this.f[10] = Flags.items;
			this.f[11] = Flags.superProtect;
			this.f[12] = Flags.homes;
			this.f[13] = Flags.snowForm;
			this.f[14] = Flags.iceMelt;
		}

	}

	public void resetFlags() {
		f = null;
		checkNullFlags();
	}

	public int ownersSize() {
		return this.owners.size();
	}

	public String getFlagInfo() {
		checkNullFlags();
		return
				AQUA + "PvP: " +
				GOLD + this.f[0] +
				AQUA + ", Chest opening: " +
				GOLD + this.f[1] +
				AQUA + ", Lever usage: " +
				GOLD + this.f[2] +
				AQUA + ", Button usage: " +
				GOLD + this.f[3] +
				AQUA + ", Door usage: " +
				GOLD + this.f[4] +
				AQUA + ", Monster spawning: " +
				GOLD + this.f[5] +
				AQUA + ", Animal hurting: " +
				GOLD + this.f[6] +
				AQUA + ", Potions: " +
				GOLD + this.f[7] +
				AQUA + ", Invincibility: " +
				GOLD + this.f[8] +
				AQUA + ", Crop trampling: " +
				GOLD + this.f[9] +
				AQUA + ", Item Pickup: " +
				GOLD + this.f[10] +
				AQUA + ", Super Protect: " +
				GOLD + this.f[11] + 
				AQUA + ", Non-Member Homeset: " +
				GOLD + this.f[12];
	}

	public void setName(String name) {
		this.name = name;
	}
}