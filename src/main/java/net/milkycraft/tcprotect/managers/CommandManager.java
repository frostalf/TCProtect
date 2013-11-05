package net.milkycraft.tcprotect.managers;

import static org.bukkit.ChatColor.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.milkycraft.tcprotect.DefineRegionBuilder;
import net.milkycraft.tcprotect.LargeChunkObject;
import net.milkycraft.tcprotect.TCProtect;
import net.milkycraft.tcprotect.RedefineRegionBuilder;
import net.milkycraft.tcprotect.Region;
import net.milkycraft.tcprotect.RegionBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {
	static final String NOT_IN_REGION_MESSAGE = RED
			+ "You need to be in a region or define a region to do that!";
	static final String NO_PERMISSION_MESSAGE = RED + "[TCProtect] You don't have permission to do that!";
	static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
	private static void sendNotInRegionMessage(Player p) {
		p.sendMessage(NOT_IN_REGION_MESSAGE);
	}

	private static void sendNoPermissionMessage(Player p) {
		p.sendMessage(NO_PERMISSION_MESSAGE);
	}

	@Override
	public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("protect")) {
			s.sendMessage(YELLOW
					+ "To protect your land purchase one of the potions listed below, once you have received the potion take it in your hand and throw it down on the land you would like to protect");
			s.sendMessage(DARK_RED + "" + BOLD + "/pot1 " + getByChar('7') + "--- "
					+ getByChar('a') + "$200 " + GOLD + "protects 10x10");
			s.sendMessage(getByChar('5') + "" + BOLD + "/pot2 " + getByChar('7') + "--- "
					+ getByChar('a') + "$500 " + GOLD + "protects 20x20");
			s.sendMessage(getByChar('1') + "" + BOLD + "/pot3 " + getByChar('7') + "--- "
					+ getByChar('a') + "$800 " + GOLD + "protects 40x40");
			return true;
		}
		if (label.equalsIgnoreCase("tcprotect") || label.equalsIgnoreCase("rp")
				|| label.equalsIgnoreCase("rprotect") || label.equalsIgnoreCase("redp")) {
			if (!(s instanceof Player)) {
				s.sendMessage("You can't use TCProtect from the console!");
			}
			Player player = (Player) s;
			if (args.length == 0) {
				player.sendMessage(AQUA
						+ "TCProtect version "
						+ Bukkit.getPluginManager().getPlugin("TCProtect").getDescription()
						.getVersion());
				player.sendMessage(AQUA + "[TCProtect] For more information about the commands, type [" + GOLD
						+ "/tcprotect ?" + AQUA + "].");
				player.sendMessage(AQUA + "[TCProtect] For a tutorial, type [" + GOLD + "/tcprotect tutorial"
						+ AQUA + "].");
			}
			else {
				if ((args[0].equalsIgnoreCase("?")) || (args[0].equalsIgnoreCase("help"))) {
					player.sendMessage(AQUA + "[TCProtect] Available commands to you: ");
					player.sendMessage(AQUA + "[TCProtect] ------------------------------------");
					if (TCProtect.ph.hasHelpPerm(player, "limit")) {
						player.sendMessage(GREEN + "/tcprotect limit");
					}
					if (TCProtect.ph.hasHelpPerm(player, "list")) {
						player.sendMessage(GREEN + "/tcprotect list");
					}
					if (TCProtect.ph.hasHelpPerm(player, "delete")) {
						player.sendMessage(GREEN + "/tcprotect delete");
					}
					if (TCProtect.ph.hasHelpPerm(player, "info")) {
						player.sendMessage(GREEN + "/tcprotect info");
					}
					if (TCProtect.ph.hasHelpPerm(player, "addmember")) {
						player.sendMessage(GREEN + "/tcprotect addmember (player)");
					}
					if (TCProtect.ph.hasHelpPerm(player, "addowner")) {
						player.sendMessage(GREEN + "/tcprotect addowner (player)");
					}
					if (TCProtect.ph.hasHelpPerm(player, "removemember")) {
						player.sendMessage(GREEN + "/tcprotect removemember (player)");
					}
					if (TCProtect.ph.hasHelpPerm(player, "removeowner")) {
						player.sendMessage(GREEN + "/tcprotect removeowner (player)");
					}
					if (TCProtect.ph.hasHelpPerm(player, "rename")) {
						player.sendMessage(GREEN + "/tcprotect rename (name)");
					}
					if (TCProtect.ph.hasPerm(player, "tcprotect.near")) {
						player.sendMessage(GREEN + "/tcprotect near");
					}
					if (TCProtect.ph.hasPerm(player, "tcprotect.lock")) {
						player.sendMessage(GREEN + "/tcprotect lock");
						player.sendMessage(GREEN + "/tcprotect unlock");
					}
					player.sendMessage(GREEN + "/tcprotect flag");
					player.sendMessage(AQUA + "[TCProtect] ------------------------------------");
				}

				if ((args[0].equalsIgnoreCase("limit"))
						|| (args[0].equalsIgnoreCase("limitremaining"))
						|| (args[0].equalsIgnoreCase("remaining"))) {
					if (TCProtect.ph.hasPerm(player, "tcprotect.own.limit")) {
						int limit = TCProtect.ph.getPlayerLimit(player);
						if ((limit < 0) || (TCProtect.ph.hasPerm(player, "tcprotect.unlimited"))) {
							player.sendMessage(AQUA + "[TCProtect] You have no limit!");
						}

						int currentUsed = TCProtect.rm.getTotalRegionSize(player.getName());
						player.sendMessage(AQUA + "[TCProtect] Your area: (" + GOLD + currentUsed + AQUA
								+ " / " + GOLD + limit + AQUA + ").");
					}
					player.sendMessage(RED + "[TCProtect] You don't have sufficient permission to do that.");
				}

				else if ((args[0].equalsIgnoreCase("time")) || (args[0].equalsIgnoreCase("ptime"))) {
					Region r = TCProtect.rm.getRegion(player.getLocation());
					if (r == null) {
						s.sendMessage(ChatColor.RED + "[TCProtect] Go stand inside a region to use this");
						return true;
					}
					s.sendMessage(ChatColor.GREEN + "This region was last used on " + ChatColor.GOLD + 
							sdf.format(new Date(r.lastUsed().longValue())));
				}

				else if ((args[0].equalsIgnoreCase("tutorial"))
						|| (args[0].equalsIgnoreCase("tut"))) {
					player.sendMessage(AQUA
							+ "Type /pot1, /pot2, or /pot3 to receive your protection potion");
					player.sendMessage(AQUA
							+ "Throw down the potion in the center of the area that you want to protect!");
				}

				else if ((args[0].equalsIgnoreCase("near")) || (args[0].equalsIgnoreCase("nr"))) {
					if (TCProtect.ph.hasPerm(player, "tcprotect.near")) {
						Set<?> regions = TCProtect.rm.getRegionsNear(player, 30, player.getWorld());
						if (regions.isEmpty()) {
							player.sendMessage(AQUA + "[TCProtect] There are no regions nearby.");
						} else {
							Iterator<?> i = regions.iterator();
							player.sendMessage(AQUA + "[TCProtect] Regions within 40 blocks: ");
							player.sendMessage(AQUA + "[TCProtect] ------------------------------------");
							while (i.hasNext()) {
								Region r = (Region) i.next();
								player.sendMessage(AQUA + "[TCProtect] Name: " + GOLD + r.getName() + AQUA
										+ ", Center: [" + GOLD + r.getCenterX() + AQUA + ", "
										+ GOLD + r.getCenterZ() + AQUA + "].");
							}
							player.sendMessage(AQUA + "[TCProtect] ------------------------------------");
						}
					} else {
						player.sendMessage(RED + "[TCProtect] You don't have permission to do that.");
					}
				} else if (args[0].equalsIgnoreCase("lock")) {
					if (TCProtect.ph.hasPerm((Player) s, "tcprotect.lock")) {
						Connection conn = null;
						Statement st = null;
						ResultSet rs = null;
						try {
							Class.forName("org.sqlite.JDBC");
							String dbPath = "jdbc:sqlite:" + TCProtect.pathData + File.separator
									+ "protections.db";
							conn = DriverManager.getConnection(dbPath);
							st = conn.createStatement();
							rs = st.executeQuery("SELECT * FROM protections WHERE owner = '"
									+ s.getName() + "'");
							int count = 0;
							while (rs.next()) {
								Region r = TCProtect.rm.getRegion(new Location(TCProtect.plugin
										.getServer().getWorld(rs.getString("world")), rs
										.getInt("x"), rs.getInt("y"), rs.getInt("z")));
								if (r != null) {
                                    if (!r.equals(TCProtect.rm.getRegion(((Player) s).getLocation()))) {
                                        continue;
                                    }
                                }
								count += 1;
							}
							YamlConfiguration yaml = new YamlConfiguration();
							File yamlFile = new File(TCProtect.pathConfig);
							yaml.load(yamlFile);
							if (count < yaml.getInt("lock-limit")) {
								TCProtect.lockPlayers.add(s.getName());
								s.sendMessage(DARK_AQUA + "[TCProtect] Please click a block to lock it.");
							} else {
                                s.sendMessage(RED + "[TCProtect] You have reached the limit for locked blocks!");
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
				} else if (args[0].equalsIgnoreCase("unlock")) {
					if (TCProtect.ph.hasPerm((Player) s, "tcprotect.lock")) {
						TCProtect.unlockPlayers.add(s.getName());
						s.sendMessage(DARK_AQUA + "[TCProtect] Please click a block to unlock it.");
					}
				}
				else if (args[0].equalsIgnoreCase("redefine")) {
					if (!player.hasPermission("tcprotect.admin.redefine")) {
						player.sendMessage(RED + "[TCProtect] You don't have permission to do that!");
					}
					String name = args[1];
					Region oldRect = TCProtect.rm.getRegion(name, player.getWorld());
					if (oldRect == null) {
						player.sendMessage(RED + "[TCProtect] That region doesn't exist!");
					}
					RedefineRegionBuilder rb = new RedefineRegionBuilder(player, oldRect,
							TCProtect.flSels.get(player), TCProtect.slSels.get(player));
					if (rb.ready()) {
						Region r = rb.build();
						player.sendMessage(GREEN + "Successfully created region: " + r.getName()
								+ ".");
						TCProtect.rm.remove(oldRect);
						TCProtect.rm.add(r, player.getWorld());
					}
				} else if ((args.length <= 3) && (args[0].equalsIgnoreCase("define"))) {
					if (!player.hasPermission("tcprotect.admin.define")) {
						player.sendMessage(RED + "[TCProtect] You don't have permission to do that!");
					}
					String name = args.length >= 2 ? args[1] : "";
					String creator = args.length == 3 ? args[2] : player.getName();
					RegionBuilder rb = new DefineRegionBuilder(player,
							TCProtect.flSels.get(player), TCProtect.slSels.get(player), name,
							creator);
					if (rb.ready()) {
						Region r = rb.build();
						player.sendMessage(GREEN + "Successfully created region: " + r.getName()
								+ ".");
						TCProtect.rm.add(r, player.getWorld());
					}
				} else if ((args[0].equalsIgnoreCase("delete"))
						|| (args[0].equalsIgnoreCase("del"))) {
					if (args.length == 1) {
						handleDelete(player, TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 2) {
						handleDelete(player, TCProtect.rm.getRegion(args[1], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("purge"))
						|| (args[0].equalsIgnoreCase("pur"))) {
					if (args.length == 1) {
						handlePurge(player, TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 2) {
						handlePurge(player, TCProtect.rm.getRegion(args[1], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("i")) || (args[0].equalsIgnoreCase("info"))) {
					if (args.length == 1) {
						handleInfo(player, TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 2) {
						handleInfo(player, TCProtect.rm.getRegion(args[1], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("am"))
						|| (args[0].equalsIgnoreCase("addmember"))) {
					if (args.length == 2) {
						handleAddMember(player, args[1],
								TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 3) {
						handleAddMember(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("ao"))
						|| (args[0].equalsIgnoreCase("addowner"))) {
					if (args.length == 2) {
						handleAddOwner(player, args[1],
								TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 3) {
						handleAddOwner(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("rm"))
						|| (args[0].equalsIgnoreCase("removemember"))) {
					if (args.length == 2) {
						handleRemoveMember(player, args[1],
								TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 3) {
						handleRemoveMember(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("ro"))
						|| (args[0].equalsIgnoreCase("removeowner"))) {
					if (args.length == 2) {
						handleRemoveOwner(player, args[1],
								TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 3) {
						handleRemoveOwner(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("rn")) || (args[0].equalsIgnoreCase("rename"))) {
					if (args.length == 2) {
						handleRename(player, args[1],
								TCProtect.rm.getRegion(player, player.getWorld()));
					} else if (args.length == 3) {
						handleRename(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
				} else if ((args[0].equalsIgnoreCase("fl")) || (args[0].equalsIgnoreCase("flag"))) {
					if (args.length == 2) {
						handleFlag(player, args[1], TCProtect.rm.getRegion(player.getLocation()));
					} else if (args.length == 3) {
						handleFlag(player, args[1],
								TCProtect.rm.getRegion(args[2], player.getWorld()));
					}
					else if (args.length == 1){
						player.sendMessage(AQUA
								+ "To use the command, type '/tcprotect (flag)'. This will toggle the specific flag if you have permission.");
						player.sendMessage(AQUA
								+ "Type '/tcprotect flag info' to show the status of flags in the region you're currently in.");
					}

				} else if ((args[0].equalsIgnoreCase("list")) || (args[0].equalsIgnoreCase("ls"))) {
					if (args.length == 1) {
						handleList(player, player.getName());
					} else if (args.length == 2) {
						handleList(player, args[1]);
					}
				} else if ((args[0].equalsIgnoreCase("resetflags"))) {
					if (s instanceof Player
							&& !TCProtect.ph.hasPerm((Player) s, "tcprotect.admin.resetflags")) {
						s.sendMessage(RED + "[TCProtect] You don't have permission to use this command.");
					}
					for (WorldRegionManager wrm : TCProtect.rm.regionManagers.values()) {
						if (wrm instanceof WorldFlatFileRegionManager) {
							for (LargeChunkObject l : ((WorldFlatFileRegionManager) wrm).regions
									.values()) {
								for (Region r : l.regions) {
									r.resetFlags();
								}
							}
						}
					}
					s.sendMessage("All region flags have been reset!");
				}
				else if (args[0].equalsIgnoreCase("purgeadd")){
					if (player.hasPermission("tcprotect.purge.bypass")){
						if (args.length >= 2){
							Region r = TCProtect.rm.getRegion(args[1], player.getWorld());
							if (r != null){
								try {
									File f = new File(TCProtect.plugin.getDataFolder(), "purgeexempt.yml");
									YamlConfiguration y = new YamlConfiguration();
									y.load(f);
									List<String> exempt = y.getStringList("exempt");
									exempt.add(r.getName());
									y.set("exempt", exempt);
									y.save(f);
									player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Successfully added region to exempt list!");
								}
								catch (Exception ex){
									ex.printStackTrace();
									player.sendMessage(ChatColor.RED + "[TCProtect] An exception occurred while adding your region.");
								}
							}
							else {
                                player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Specified region not found!");
                            }
						}
						else {
                            player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Missing arguments! Usage: /tcprotect purgeadd [region name]");
                        }
					}
					else {
                        player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] You lack sufficient permissions to perform this command");
                    }
				}
				else if (args[0].equalsIgnoreCase("purgeremove")){
					if (player.hasPermission("tcprotect.purge.bypass")){
						if (args.length >= 2){
							Region r = TCProtect.rm.getRegion(args[1], player.getWorld());
							if (r != null){
								try {
									File f = new File(TCProtect.plugin.getDataFolder(), "purgeexempt.yml");
									YamlConfiguration y = new YamlConfiguration();
									y.load(f);
									List<String> exempt = y.getStringList("exempt");
									exempt.remove(r.getName());
									y.set("exempt", exempt);
									y.save(f);
									player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Successfully removed region from exempt list!");
								}
								catch (Exception ex){
									ex.printStackTrace();
									player.sendMessage(ChatColor.RED + "[TCProtect] An exception occurred while removing your region.");
								}
							}
							else {
                                player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Specified region not found!");
                            }
						}
						else {
                            player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] Missing arguments! Usage: /tcprotect purgeremove [region name]");
                        }
					}
					else {
                        player.sendMessage(ChatColor.DARK_AQUA + "[TCProtect] You lack sufficient permissions to perform this command");
                    }
				}
			}
			return true;
		}
		return false;
	}

	private void handlePurge(Player p, Region r) {
		if (p.hasPermission("tcprotect.purge")) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			r.expire();
			p.sendMessage(AQUA + "[TCProtect] Region successfully purged.");
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleDelete(Player p, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "delete", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			p.sendMessage(AQUA + "[TCProtect] Region successfully deleted.");
			TCProtect.rm.remove(r);
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleInfo(Player p, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "info", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			p.sendMessage(r.info());
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleAddMember(Player p, String sVictim, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "addmember", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			Player pVictim = Bukkit.getPlayerExact(sVictim);
			if (r.isOwner(sVictim)) {
				r.removeOwner(sVictim);
				r.addMember(sVictim);
				if ((pVictim != null) && (pVictim.isOnline())) {
					pVictim.sendMessage(AQUA + "[TCProtect] You have been demoted to member in: " + GOLD
							+ r.getName() + AQUA + ", by: " + GOLD + p.getName() + AQUA + ".");
				}

				p.sendMessage(AQUA + "[TCProtect] Demoted player " + GOLD + sVictim + AQUA + " to member in "
						+ GOLD + r.getName() + AQUA + ".");
			} else if (!r.isMember(sVictim)) {
				r.addMember(sVictim);
				p.sendMessage(AQUA + "[TCProtect] Added " + GOLD + sVictim + AQUA + " as a member.");
				if ((pVictim != null) && (pVictim.isOnline())) {
                    pVictim.sendMessage(AQUA + "[TCProtect] You have been added as a member to region: " + GOLD
                            + r.getName() + AQUA + ", by: " + GOLD + p.getName() + AQUA + ".");
                }
			} else {
				p.sendMessage(RED + sVictim + " is already a member in this region.");
			}
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleAddOwner(Player p, String sVictim, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "addowner", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			Player pVictim = Bukkit.getPlayerExact(sVictim);
			if (!r.isOwner(sVictim)) {
				r.addOwner(sVictim);
				p.sendMessage(AQUA + "[TCProtect] Added " + GOLD + sVictim + AQUA + " as an owner.");
				if ((pVictim != null) && (pVictim.isOnline())) {
                    pVictim.sendMessage(AQUA + "[TCProtect] You have been added as an owner to region: " + GOLD
                            + r.getName() + AQUA + ", by: " + GOLD + p.getName() + AQUA + ".");
                }
			} else {
				p.sendMessage(RED + "[TCProtect] That player is already an owner in this region!");
			}
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleRemoveMember(Player p, String sVictim, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "removemember", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			Player pVictim = Bukkit.getPlayerExact(sVictim);
			if ((r.isMember(sVictim)) || (r.isOwner(sVictim))) {
				p.sendMessage(AQUA + "[TCProtect] Removed " + GOLD + sVictim + AQUA + " from this region.");
				r.removeMember(sVictim);
				if ((pVictim != null) && (pVictim.isOnline())) {
                    pVictim.sendMessage(AQUA + "[TCProtect] You have been removed as a member from region: "
                            + GOLD + r.getName() + AQUA + ", by: " + GOLD + p.getName() + AQUA
                            + ".");
                }
			} else {
				p.sendMessage(RED + sVictim + " isn't a member of this region.");
			}
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleRemoveOwner(Player p, String sVictim, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "removeowner", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			Player pVictim = Bukkit.getPlayerExact(sVictim);
			if (r.isOwner(sVictim)) {
				if (r.ownersSize() > 1) {
					p.sendMessage(AQUA + "[TCProtect] Made " + GOLD + sVictim + AQUA
							+ " a member in this region.");
					r.removeOwner(sVictim);
					r.addMember(sVictim);
					if ((pVictim != null) && (pVictim.isOnline())) {
                        pVictim.sendMessage(AQUA
                                + "You have been removed as an owner from region: " + GOLD
                                + r.getName() + AQUA + ", by: " + GOLD + p.getName() + AQUA + ".");
                    }
				} else {
					p.sendMessage(AQUA + "[TCProtect] You can't remove " + GOLD + sVictim + AQUA
							+ ", because they are the last owner in this region.");
				}
			} else {
                p.sendMessage(RED + sVictim + " isn't an owner in this region.");
            }
		} else {
			sendNoPermissionMessage(p);
		}
	}

	public static void handleRename(Player p, String newName, Region r) {
		if (TCProtect.ph.hasRegionPerm(p, "rename", r)) {
			if (r == null) {
				sendNotInRegionMessage(p);
				return;
			}
			if (TCProtect.rm.getRegion(newName, p.getWorld()) != null) {
				p.sendMessage(RED + "[TCProtect] That name is already taken, please choose another one.");
				return;
			}
			if ((newName.length() < 2) || (newName.length() > 16)) {
				p.sendMessage(RED + "[TCProtect] Invalid name. Please enter a 2-16 character name.");
				return;
			}
			if (newName.contains(" ")) {
				p.sendMessage(RED + "[TCProtect] The name of the region can't have a space in it.");
				return;
			}
			TCProtect.rm.rename(r, newName, p.getWorld());
			p.sendMessage(AQUA + "[TCProtect] Made " + GOLD + newName + AQUA + " the new name for this region.");
		} else {
			p.sendMessage(RED + "[TCProtect] You don't have sufficient permission to do that.");
		}
	}

	public static void handleFlag(Player p, String flag, Region r) {
		if (r == null) {
			sendNotInRegionMessage(p);
			return;
		}
		r.checkNullFlags();
		if (flag.equalsIgnoreCase("pvp")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.pvp")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					TCProtect.rm.setFlag(r, 0, !r.getFlag(0), p.getWorld());
					p.sendMessage(AQUA + "[TCProtect] Flag \"pvp\" has been set to " + r.getFlag(0) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("chest")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.chest")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(1, !r.getFlag(1));
					p.sendMessage(AQUA + "[TCProtect] Flag \"chest\" has been set to " + r.getFlag(1) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("lever")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.lever")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(2, !r.getFlag(2));
					p.sendMessage(AQUA + "[TCProtect] Flag \"lever\" has been set to " + r.getFlag(2) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("button")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.button")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(3, !r.getFlag(3));
					p.sendMessage(AQUA + "[TCProtect] Flag \"button\" has been set to " + r.getFlag(3) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("door")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.door")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(4, !r.getFlag(4));
					p.sendMessage(AQUA + "[TCProtect] Flag \"door\" has been set to " + r.getFlag(4) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("mobs")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.mobs")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(5, !r.getFlag(5));
					p.sendMessage(AQUA + "[TCProtect] Flag \"mobs\" has been set to " + r.getFlag(5) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("animals")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.animals")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(6, !r.getFlag(6));
					p.sendMessage(AQUA + "[TCProtect] Flag \"animals\" has been set to " + r.getFlag(6) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("potions")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.potions")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(7, !r.getFlag(7));
					p.sendMessage(AQUA + "[TCProtect] Flag \"potions\" has been set to " + r.getFlag(7) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("invincible")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.invincible")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(8, !r.getFlag(8));
					p.sendMessage(AQUA + "[TCProtect] Flag \"invincible\" has been set to " + r.getFlag(8)
							+ ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("crops")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.crops")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(9, !r.getFlag(9));
					p.sendMessage(AQUA + "[TCProtect] Flag \"crops\" has been set to " + r.getFlag(9) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("items")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.items")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(10, !r.getFlag(10));
					p.sendMessage(AQUA + "[TCProtect] Flag \"items\" has been set to " + r.getFlag(10) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("superprotect")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.superprotect")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.flag.superprotect"))) {
					r.setFlag(11, !r.getFlag(11));
					p.sendMessage(AQUA + "[TCProtect] Flag \"superprotect\" has been set to " + r.getFlag(11)
							+ ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if ((flag.equalsIgnoreCase("info")) || (flag.equalsIgnoreCase("i"))) {
			p.sendMessage(AQUA + "[TCProtect] Flag values: (" + r.getFlagInfo() + AQUA + ")");
		} else if (flag.equalsIgnoreCase("homes")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.homes")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(12, !r.getFlag(12));
					p.sendMessage(AQUA + "[TCProtect] Flag \"homes\" has been set to " + r.getFlag(12) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("snowform")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.snowform")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(13, !r.getFlag(13));
					p.sendMessage(AQUA + "[TCProtect] Flag \"snowform\" has been set to " + r.getFlag(13) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else if (flag.equalsIgnoreCase("icemelt")) {
			if (TCProtect.ph.hasPerm(p, "tcprotect.flag.icemelt")) {
				if ((r.isOwner(p)) || (TCProtect.ph.hasPerm(p, "tcprotect.admin.flag"))) {
					r.setFlag(14, !r.getFlag(14));
					p.sendMessage(AQUA + "[TCProtect] Flag \"icemelt\" has been set to " + r.getFlag(14) + ".");
				} else {
					p.sendMessage(AQUA
							+ "You don't have permission to toggle that flag in this region!");
				}
			} else {
				p.sendMessage(RED + "[TCProtect] You don't have permission to toggle that flag!");
			}
		} else {
			p.sendMessage(AQUA
					+ "List of flags: [pvp, chest, lever, button, door, mobs, animals, potions, invincible, crops, items, superprotect, homes]");
		}
	}

	public static void handleList(Player player, String name) {
		if (TCProtect.ph.hasPerm(player, "tcprotect.admin.list")) {
			Set<Region> regions = TCProtect.rm.getRegions(name);
			int length = regions.size();
			if (length == 0) {
				player.sendMessage(AQUA + "[TCProtect] That player has no regions!");
			} else {
				player.sendMessage(AQUA + "Regions created:");
				player.sendMessage(AQUA + "------------------------------------");
				Iterator<Region> i = regions.iterator();
				while (i.hasNext()) {
					player.sendMessage(AQUA + i.next().info());
				}
				player.sendMessage(AQUA + "------------------------------------");
			}
		}
		else {
            player.sendMessage(RED + "[TCProtect] You don't have sufficient permission to do that.");
        }
	}
}