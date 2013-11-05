package net.milkycraft.tcprotect.listeners;

import static org.bukkit.ChatColor.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.milkycraft.tcprotect.PurgerThread;
import net.milkycraft.tcprotect.TCProtect;
import net.milkycraft.tcprotect.Region;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class RPPlayerListener implements Listener {
	TCProtect plugin;

	public RPPlayerListener(TCProtect plugin){
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onJoin(PlayerJoinEvent e){
		for (Region r : TCProtect.rm.getRegions(e.getPlayer())){
			r.us3();
		}
		List<String> regions = PurgerThread.alertRegions(e.getPlayer().getName());
		if (!regions.isEmpty()){
			for (String r : regions) {
                e.getPlayer().sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Your region " +
                        ChatColor.GOLD + ChatColor.BOLD + r +
                        ChatColor.DARK_RED + ChatColor.BOLD + " has been purged!");
            }
			PurgerThread.removePlayer(e.getPlayer().getName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuit(PlayerQuitEvent e){
		for (Region r : TCProtect.rm.getRegions(e.getPlayer())){
			r.us3();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onKick(PlayerKickEvent e){
		for (Region r : TCProtect.rm.getRegions(e.getPlayer())){
			r.us3();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCommandEvent(PlayerCommandPreprocessEvent e){
		if (e.getMessage().startsWith("/sethome")){
			if (TCProtect.rm.getRegion(e.getPlayer(), e.getPlayer().getWorld()) != null){
				if (!TCProtect.rm.getRegion(e.getPlayer(), e.getPlayer().getWorld()).canHome(
						e.getPlayer())){
					e.getPlayer().sendMessage(RED + "You are not permitted to set home here");
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityInteract(PlayerInteractEntityEvent e){
		if (e.getRightClicked().getType() == EntityType.ITEM_FRAME){
			Player p = e.getPlayer();
			Block b = e.getRightClicked().getLocation().getBlock();
			if (!TCProtect.rm.canBuild(p, b, p.getWorld())){
				e.setCancelled(true);
				// Block the rotation of item frames if they can't build
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		Block b = e.getClickedBlock();
		if (b == null) {
            return;
        }
		Region r = null;
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if (TCProtect.lockPlayers.contains(p.getName())){
				r = TCProtect.rm.getRegion(p.getLocation());
				if (r != null){
					if (r.getOwners().contains(p.getName()) || p.hasPermission("tcprotect.lock.admin")){
						Connection conn = null;
						Statement st = null;
						ResultSet rs = null;
						try {
							Class.forName("org.sqlite.JDBC");
							String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator
									+ "protections.db";
							conn = DriverManager.getConnection(dbPath);
							st = conn.createStatement();
							String world = b.getWorld().getName();
							int x = b.getX();
							int y = b.getY();
							int z = b.getZ();
							rs = st.executeQuery("SELECT * FROM protections WHERE world = '" + world
									+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
							int count = 0;
							while (rs.next()){
								if (TCProtect.plugin.getServer().getWorld(rs.getString("world")) != null){
									Region re1 = TCProtect.rm.getRegion(new Location(
											TCProtect.plugin.getServer().getWorld(rs.getString("world")),
											rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
									Region re2 = TCProtect.rm.getRegion(e.getClickedBlock().getLocation());
									if (re1 != null && re2 != null) {
                                        if (re1 == re2) {
                                            count += 1;
                                        }
                                    }
								}
							}
							if (count < TCProtect.lockLimit){
								List<Material> vb = new ArrayList<>();
								vb.add(Material.CHEST);
								vb.add(Material.TRAPPED_CHEST);
								vb.add(Material.WOODEN_DOOR);
								vb.add(Material.IRON_DOOR_BLOCK);
								vb.add(Material.TRAP_DOOR);
								vb.add(Material.WALL_SIGN);
								vb.add(Material.SIGN_POST);
								if (vb.contains(b.getType())){
									st.executeUpdate("INSERT INTO protections (owner, world, x, y, z) VALUES ('"
											+ p.getName()
											+ "', '"
											+ world
											+ "', '"
											+ x
											+ "', '"
											+ y
											+ "', '" + z + "')");
									p.sendMessage(DARK_AQUA
											+ "[TCProtect] Successfully registered lock on "
											+ WordUtils.capitalize(b.getType().toString()) + "!");
									TCProtect.lockPlayers.remove(p.getName());
								}
								else {
									p.sendMessage(DARK_AQUA
											+ "[TCProtect] Invalid block selected. Lock registration cancelled.");
									TCProtect.lockPlayers.remove(p.getName());
								}
							}
							else {
								rs = st.executeQuery("SELECT * FROM protections WHERE world = '" + world
										+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
								if (rs.getString("owner").equals(p.getName())) {
                                    p.sendMessage(DARK_AQUA
                                            + "[TCProtect] You have already registered this "
                                            + WordUtils.capitalize(b.getType().toString()) + "!");
                                }
								else {
                                    p.sendMessage(DARK_AQUA + "[TCProtect] This "
                                            + WordUtils.capitalize(b.getType().toString())
                                            + " has already been registered!");
                                }
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
							p.sendMessage(ChatColor.RED + "[TCProtect] An internal error occurred while performing this action. Please contact a server administrator.");
							TCProtect.lockPlayers.remove(p.getName());
						}
						finally {
							try {
								rs.close();
								st.close();
								conn.close();
							} catch (Exception exc){
								exc.printStackTrace();
							}
						}
						TCProtect.lockPlayers.remove(p.getName());
					}
				}
				else {
					p.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] You are not presently in a region!");
					TCProtect.lockPlayers.remove(p.getName());
				}
			}
			else if (TCProtect.unlockPlayers.contains(p.getName())){
				r = TCProtect.rm.getRegion(p.getLocation());
				if (r != null){
					if (r.getOwners().contains(p.getName()) || p.hasPermission("tcprotect.lock.admin")){
						Connection conn = null;
						Statement st = null;
						try {
							Class.forName("org.sqlite.JDBC");
							String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator
									+ "protections.db";
							conn = DriverManager.getConnection(dbPath);
							st = conn.createStatement();
							String world = b.getWorld().getName();
							int x = b.getX();
							int y = b.getY();
							int z = b.getZ();
							st.executeUpdate("DELETE FROM protections WHERE world = '" + world
									+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
							p.sendMessage(ChatColor.DARK_AQUA
									+ "[TCProtect] Lock successfully removed!");
							TCProtect.unlockPlayers.remove(p.getName());
							TCProtect.unlockPlayers.remove(p.getName());
						}
						catch (Exception ex){
							ex.printStackTrace();
							p.sendMessage(ChatColor.RED + "[TCProtect] An internal error occurred while performing this action. Please contact a server administrator.");
							TCProtect.unlockPlayers.remove(p.getName());
						}
						finally {
							try {
								conn.close();
								st.close();
							}
							catch (Exception exc){
								exc.printStackTrace();
							}
						}
					}
					else {
						p.sendMessage(ChatColor.DARK_AQUA
								+ "[TCProtect] You may not remove locks in this region!");
						TCProtect.unlockPlayers.remove(p.getName());
					}
				}
				else {
					p.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] You are not presently in a region!");
					TCProtect.unlockPlayers.remove(p.getName());
				}
			}
			else {
				r = TCProtect.rm.getRegion(p.getLocation());
				if (r != null){
					if (r.getMembers().contains(p.getName())){
						Connection conn = null;
						Statement st = null;
						ResultSet rs = null;
						try {
							Class.forName("org.sqlite.JDBC");
							String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator
									+ "protections.db";
							conn = DriverManager.getConnection(dbPath);
							st = conn.createStatement();
							String world = b.getWorld().getName();
							int x = b.getX();
							int y = b.getY();
							int z = b.getZ();
							rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '" + world
									+ "' AND x = '" + x + "' AND y = '" + y + "' AND z = '" + z + "'");
							boolean found = false;
							while (rs.next()){
								if (rs.getInt(1) > 0) {
                                    found = true;
                                }
							}
							if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST){
								if (new Location(b.getWorld(), x - 1, y, z).getBlock().getType() == b.getType()){
									rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '"
											+ world + "' AND x = '" + (x - 1) + "' AND y = '" + y + "' AND z = '"
											+ z + "'");
									while (rs.next()){
										if (rs.getInt(1) > 0) {
                                            found = true;
                                        }
									}
								}
								if (new Location(b.getWorld(), x + 1, y, z).getBlock().getType() == b.getType()){
									rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '"
											+ world + "' AND x = '" + (x + 1) + "' AND y = '" + y + "' AND z = '"
											+ z + "'");
									while (rs.next()){
										if (rs.getInt(1) > 0) {
                                            found = true;
                                        }
									}
								}
								if (new Location(b.getWorld(), x, y, z - 1).getBlock().getType() == b.getType()){
									rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '"
											+ world + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '"
											+ (z - 1) + "'");
									while (rs.next()){
										if (rs.getInt(1) > 0) {
                                            found = true;
                                        }
									}
								}
								if (new Location(b.getWorld(), x, y, z + 1).getBlock().getType() == b.getType()){
									rs = st.executeQuery("SELECT COUNT(*) FROM protections WHERE world = '"
											+ world + "' AND x = '" + x + "' AND y = '" + y + "' AND z = '"
											+ (z + 1) + "'");
									while (rs.next()){
										if (rs.getInt(1) > 0) {
                                            found = true;
                                        }
									}
								}
							}
							if (found){

								if (!r.getOwners().contains(p.getName()) || !p.hasPermission("tcprotect.lock.admin")){
									p.sendMessage(ChatColor.RED
											+ "[TCProtect] You may not open the region owners' private chests!");
									e.setCancelled(true);
								}
							}
						}
						catch (Exception ex){
							ex.printStackTrace();
						}
						finally {
							try {
								conn.close();
								st.close();
								rs.close();
							}
							catch (Exception exc){
								exc.printStackTrace();
							}
						}
					}
				}
			}
		}
		Material itemInHand = p.getItemInHand().getType();

		if (p.getItemInHand().getTypeId() == TCProtect.adminWandID){
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				if (p.hasPermission("tcprotect.magicwand")){
					TCProtect.slSels.put(p, b.getLocation());
					p.sendMessage(AQUA + "Set the second magic wand location to (" + GOLD
							+ b.getLocation().getBlockX() + AQUA + ", " + GOLD
							+ b.getLocation().getBlockY() + AQUA + ", " + GOLD
							+ b.getLocation().getBlockZ() + AQUA + ").");
					e.setCancelled(true);
					return;
				}
			}
			else if ((e.getAction().equals(Action.LEFT_CLICK_BLOCK))
					&& (p.hasPermission("tcprotect.magicwand"))){
				TCProtect.flSels.put(p, b.getLocation());
				p.sendMessage(AQUA + "Set the first magic wand location to (" + GOLD
						+ b.getLocation().getBlockX() + AQUA + ", " + GOLD
						+ b.getLocation().getBlockY() + AQUA + ", " + GOLD
						+ b.getLocation().getBlockZ() + AQUA + ").");
				e.setCancelled(true);
				return;
			}
		}

		if (p.getItemInHand().getTypeId() == TCProtect.infoWandID){
			if (e.getAction().equals(Action.RIGHT_CLICK_AIR)){
				r = TCProtect.rm.getRegion(p.getLocation());
			}
			else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
				r = TCProtect.rm.getRegion(b.getLocation());
			}
			if (p.hasPermission("tcprotect.infowand")){
				if (r == null){
					p.sendMessage(RED + "There is no region at that block's location!");
				}
				else {
					p.sendMessage(AQUA + "--------------- [" + GOLD + r.getName() + AQUA
							+ "] ---------------");
					p.sendMessage(r.info());
					p.sendMessage(r.getFlagInfo());
				}
				e.setCancelled(true);
				return;
			}
		}

		if (b.getType().equals(Material.CHEST)){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canChest(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't open this chest!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Opened locked chest in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if (b.getType().equals(Material.DISPENSER)){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canChest(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't open this dispenser!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Opened locked dispenser in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if ((b.getType().equals(Material.FURNACE))
				|| (b.getType().equals(Material.BURNING_FURNACE))){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canChest(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't open this furnace!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Opened locked furnace in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if ((b.getType().equals(Material.HOPPER))){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canChest(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't open this hopper!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Opened locked hopper in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if (b.getType().equals(Material.LEVER)){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canLever(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't toggle this lever!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Toggled locked lever in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if (b.getType().equals(Material.STONE_BUTTON)){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canButton(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't activate this button!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Activated locked button in " + r.getCreator()
							+ "'s region.");
				}
			}

		}
		else if (b.getType().equals(Material.WOODEN_DOOR)){
			r = TCProtect.rm.getRegion(b.getLocation());
			if (r == null) {
                return;
            }
			if (!r.canDoor(p)){
				if (!TCProtect.ph.hasPerm(p, "tcprotect.bypass")){
					p.sendMessage(RED + "You can't open this door!");
					e.setCancelled(true);
				}
				else {
					p.sendMessage(YELLOW + "Opened locked door in " + r.getCreator() + "'s region.");
				}
			}
		}

		if (((itemInHand.equals(Material.FLINT_AND_STEEL))
				|| (itemInHand.equals(Material.WATER_BUCKET))
				|| (itemInHand.equals(Material.LAVA_BUCKET)) || (itemInHand
						.equals(Material.PAINTING))) && (!TCProtect.rm.canBuild(p, b, b.getWorld()))){
			p.sendMessage(RED + "You can't use that here!");
			e.setCancelled(true);
		}
	}
}