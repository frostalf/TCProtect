package net.milkycraft.tcprotect;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public abstract class RegionBuilder {
	Region r = null;

	public boolean ready() {
		return this.r != null;
	}

	public Region build() {
		return this.r;
	}

	void setErrorSign(SignChangeEvent e, String error) {
		e.setLine(0, ChatColor.RED + "[RP] Error");
		setError(e.getPlayer(), error);
	}

	void setError(Player p, String error) {
		p.sendMessage(ChatColor.RED + "[RP] There was an error creating that region! (" + error
				+ ")");
	}
}