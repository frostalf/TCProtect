package net.milkycraft.tcprotect.listeners;

import net.milkycraft.tcprotect.TCProtect;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class RPWorldListener implements Listener {
	TCProtect plugin;

	public RPWorldListener(TCProtect plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		World w = e.getWorld();
		try {
			TCProtect.rm.load(w);
			TCProtect.logger.debug("World loaded: " + w.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e) {
		World w = e.getWorld();
		try {
			TCProtect.rm.unload(w);
			TCProtect.logger.debug("World unloaded: " + w.getName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}