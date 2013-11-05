package net.milkycraft.tcprotect;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class TCPermHandler {
	final Chat permission;

	public TCPermHandler() throws Exception {
		RegisteredServiceProvider<Chat> provider = Bukkit.getServer().getServicesManager()
				.getRegistration(Chat.class);
		if (provider == null) {
			throw new Exception(
					"This plugin requires a registered Permission provider to function!");
		}
		this.permission = (provider.getProvider());
	}

	public boolean hasPerm(Player p, String perm) {
		return p.hasPermission(perm);
	}

	public boolean hasPerm(String pl, String perm) {
		Player p = Bukkit.getServer().getPlayerExact(pl);
		if (p == null) {
			return false;
		}
		return p.hasPermission(perm);
	}

	public boolean hasRegionPerm(Player p, String s, Region poly) {
		String adminperm = "tcprotect.admin." + s;
		String userperm = "tcprotect.own." + s;
		if (poly == null) {
			return (hasPerm(p, adminperm)) || (hasPerm(p, userperm));
		}
		if (poly.isSuperProtect() && !poly.superUserCheck(p)) {
            return false;
        }
		return (hasPerm(p, adminperm)) || ((hasPerm(p, userperm)) && (poly.isOwner(p)));
	}

	public boolean hasHelpPerm(Player p, String s) {
		String adminperm = "tcprotect.admin." + s;
		String userperm = "tcprotect.own." + s;
		return (hasPerm(p, adminperm)) || (hasPerm(p, userperm));
	}

	public int getPlayerLimit(Player p) {
		return this.permission.getPlayerInfoInteger(p, "maxregionsize", TCProtect.limitAmount);
	}
}