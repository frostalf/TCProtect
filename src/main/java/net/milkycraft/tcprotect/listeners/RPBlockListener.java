package net.milkycraft.tcprotect.listeners;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import net.milkycraft.tcprotect.TCProtect;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class RPBlockListener implements Listener {
	TCProtect plugin;

	public RPBlockListener(TCProtect plugin) {
		this.plugin = plugin;
	}

	void setErrorSign(SignChangeEvent e, Player p, String error) {
		e.setLine(0, ChatColor.RED + "[TCProtect]: Error");
		p.sendMessage(ChatColor.RED + "[TCProtect] ERROR:" + error);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		try {
			Block b = e.getBlock();
			Player p = e.getPlayer();
			if (!TCProtect.rm.canBuild(p, b, p.getWorld())) {
				p.sendMessage(ChatColor.RED + "You can't build here!");
				e.setCancelled(true);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingPlace(HangingPlaceEvent e) {
		try {
			Block b = e.getBlock();
			Player p = e.getPlayer();
			if (!TCProtect.rm.canBuild(p, b, p.getWorld())) {
				p.sendMessage(ChatColor.RED + "You can't build here!");
				e.setCancelled(true);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		if (!(e.getRemover() instanceof Player)) {
            return;
        }
		Player p = (Player) e.getRemover();
		if (!TCProtect.rm.canBuild(p, p.getWorld().getBlockAt(e.getEntity().getLocation()),
				p.getWorld())) {
			p.sendMessage(ChatColor.RED + "You can't build here!");
			e.setCancelled(true);
		}
	}

	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		// Empty block
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
			Block b = e.getBlock();
			if (!TCProtect.rm.canBuild(p, b, p.getWorld())) {
				p.sendMessage(ChatColor.RED + "You can't build here!");
				e.setCancelled(true);
				return;
			}
			if (TCProtect.unlockPlayers.contains(p.getName())){
			Connection conn = null;
			Statement st = null;
			ResultSet rs = null;
			try {
				Class.forName("org.sqlite.JDBC");
				String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator + "protections.db";
				conn = DriverManager.getConnection(dbPath);
				st = conn.createStatement();
				String world = b.getWorld().getName();
				int x = b.getX();
				int y = b.getY();
				int z = b.getZ();
				rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
						+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
				int count = 0;
				while (rs.next()) {
					count = rs.getInt(1);
				}
				if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
					x = b.getX() + 1;
					if (new Location(b.getWorld(), x, y, z).getBlock().getType() == b.getType()) {
						rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
								+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
						while (rs.next()) {
							count += 1;
						}
					}
					x = b.getX() - 1;
					if (new Location(b.getWorld(), x, y, z).getBlock().getType() == b.getType()) {
						rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
								+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
						while (rs.next()) {
							count = rs.getInt(1);
						}
					}
					x = b.getX();
					z = b.getZ() + 1;
					if (new Location(b.getWorld(), x, y, z).getBlock().getType() == b.getType()) {
						rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
								+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
						while (rs.next()) {
							count = rs.getInt(1);
						}
					}
					z = b.getX() - 1;
					if (new Location(b.getWorld(), x, y, z).getBlock().getType() == b.getType()) {
						rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
								+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
						while (rs.next()) {
							count = rs.getInt(1);
						}
					}
				}
				if (count > 0) {
					if (TCProtect.rm.getRegion(b.getLocation()).getOwners().contains(p.getName())
							|| p.hasPermission("tcprotect.lock.admin")) {
						st.executeUpdate("DELETE FROM protections WHERE world = '" + world
								+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
						p.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Unregistered locked "
								+ b.getType().toString().toLowerCase());
					} else {
						p.sendMessage(ChatColor.RED + "[TCProtect] You may not break this block!");
						e.setCancelled(true);
					}
					TCProtect.unlockPlayers.remove(p.getName());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					conn.close();
					st.close();
					rs.close();
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void onSnowForm(BlockFormEvent e) {
		if (e.getNewState().getType() == Material.SNOW) {
			if (TCProtect.rm.getRegion(e.getBlock().getLocation()) != null) {
				if (!TCProtect.rm.getRegion(e.getBlock().getLocation()).canSnowForm()) {
					e.setCancelled(true);
				}
			}
		} else if (e.getNewState().getType() == Material.ICE) {
			if (TCProtect.rm.getRegion(e.getBlock().getLocation()) != null) {
				if (!TCProtect.rm.getRegion(e.getBlock().getLocation()).canIceMelt()) {
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onIceMelt(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.ICE) {
			if (TCProtect.rm.getRegion(e.getBlock().getLocation()) != null) {
				if (!TCProtect.rm.getRegion(e.getBlock().getLocation()).canIceMelt()) {
					e.setCancelled(true);
				}
			}
		}
	}
}