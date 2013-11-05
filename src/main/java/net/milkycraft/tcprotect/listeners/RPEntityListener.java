package net.milkycraft.tcprotect.listeners;

import net.milkycraft.tcprotect.TCProtect;
import net.milkycraft.tcprotect.Region;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

public class RPEntityListener implements Listener {
	TCProtect plugin;
	String noPvPMsg = ChatColor.RED + "You can't PvP in this region!";
	String noAnimalMsg = ChatColor.RED + "You can't kill animals in this region!";
	String noPotionMsg = ChatColor.RED + "You can't use potions in this region!";

	public RPEntityListener(TCProtect plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onProjectileLaunchEvent(ProjectileLaunchEvent e) {
		if (e.getEntity() instanceof ThrownPotion) {
			if (e.getEntity().getShooter() instanceof Player) {
				Player p = (Player) e.getEntity().getShooter();
				Region r = TCProtect.rm.getRegion(e.getEntity().getLocation());
				if (r == null) {
					return;
				}
				r.checkNullFlags();
				if (!r.canPotion(p)) {
					p.sendMessage(noPotionMsg);
					e.setCancelled(true);
				}
			}
		}
		if (e.getEntity() instanceof EnderPearl) {
			if (TCProtect.rm.getRegion(e.getEntity().getLocation()) == null) {
				return;
			}
			e.setCancelled(true);
			((Player) e.getEntity().getShooter()).getInventory().addItem(
					new ItemStack(Material.ENDER_PEARL));

		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (e.getCause() == TeleportCause.ENDER_PEARL) {
			if (TCProtect.rm.getRegion(e.getTo()) == null) {
				return;
			}
			e.setCancelled(true);
			e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplashEvent(PotionSplashEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			Player p = (Player) e.getEntity().getShooter();
			Region r = TCProtect.rm.getRegion(e.getEntity().getLocation());
			if (r != null && !r.canPotion(p)) {
				r.checkNullFlags();
				p.sendMessage(noPotionMsg);
				e.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityInteract(EntityInteractEvent e) {
		if (e.getBlock() == null) {
            return;
        }
		Region r = TCProtect.rm.getRegion(e.getBlock().getLocation());
		if (r == null) {
            return;
        }
		r.checkNullFlags();
		if (e.getBlock().getType() != Material.SOIL && e.getBlock().getType() != Material.CROPS) {
            return;
        }
		if (!r.canCrops((LivingEntity) e.getEntity())) {
            e.setCancelled(true);
        }
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null) {
            return;
        }
		if (e.getAction() != Action.PHYSICAL) {
            return;
        }
		Region r = TCProtect.rm.getRegion(e.getClickedBlock().getLocation());
		if (r == null) {
            return;
        }
		if (e.getClickedBlock().getType() != Material.SOIL
				&& e.getClickedBlock().getType() != Material.CROPS) {
            return;
        }
		if (!r.canCrops(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onItemPickup(PlayerPickupItemEvent e) {
		Region r = TCProtect.rm.getRegion(e.getItem().getLocation());
		if (r == null) {
            return;
        }
		r.checkNullFlags();
		if (!r.canItems(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent e) {
		Entity target = e.getTarget();
		if (target == null) {
            return;
        }
		Region r = TCProtect.rm.getRegion(target.getLocation());
		if ((r != null) && (!r.canMobs())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		Entity e = event.getEntity();
		if (e == null) {
            return;
        }
		if ((e instanceof Monster)) {
			Region r = TCProtect.rm.getRegion(e.getLocation());
			if ((r != null) && (!r.canMobs())
					&& (event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL))) {
                event.setCancelled(true);
            }
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) {
            return;
        }
		if (event.getEntity() instanceof Player) {
			Region r = TCProtect.rm.getRegion(event.getEntity().getLocation());
			if (r != null) {
				r.checkNullFlags();
                if(r.getFlag(0)){
                    event.setCancelled(true);
                }
				if (r.isInvincible()) {
					event.setCancelled(true);
				}
			}
		}
		if ((event instanceof EntityDamageByEntityEvent)) {
			EntityDamageByEntityEvent de = (EntityDamageByEntityEvent) event;
			Entity e1 = de.getEntity();
			Entity e2 = de.getDamager();
			if (e2 == null) {
				return;
			}
			if ((e2 instanceof Arrow)) {
				Arrow a = (Arrow) e2;
				e2 = a.getShooter();
				a = null;
				if (e2 == null) {
					return;
				}

			}

			Region r1 = TCProtect.rm.getRegion(e1.getLocation());
			Region r2 = TCProtect.rm.getRegion(e2.getLocation());
			if ((e1 instanceof Player)) {
				if ((e2 instanceof Player)) {
					Player p2 = (Player) e2;
					if (r1 != null) {
						if (r2 != null) {
							if ((!r1.canPVP(p2)) || (!r2.canPVP(p2))) {
								event.setCancelled(true);
								p2.sendMessage(noPvPMsg);
							}
						} else if (!r1.canPVP(p2)) {
							event.setCancelled(true);
							p2.sendMessage(noPvPMsg);
						}

					} else if ((r2 != null) && (!r2.canPVP(p2))) {
						event.setCancelled(true);
						p2.sendMessage(noPvPMsg);
					}

				}

			} else if ((e1 instanceof Animals)) {
				Region r = TCProtect.rm.getRegion(e1.getLocation());
				if (r != null) {
                    if ((e2 instanceof Player)) {
                        Player p = (Player) e2;
                        if(r.getFlag(0)){
                            event.setCancelled(true);
                        }
                        if (!r.canAnimals(p)) {
                            event.setCancelled(true);
                            p.sendMessage(noAnimalMsg);
                        }
                    } else if (!r.getFlag(6)) {
                        event.setCancelled(true);
                    }
                }
			}
		}
	}
}