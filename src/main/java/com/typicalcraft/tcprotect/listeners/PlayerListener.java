
package com.typicalcraft.tcprotect.listeners;

import com.typicalcraft.tcprotect.TCProtect;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Frostalf
 */
public class PlayerListener implements Listener {
    
    private TCProtect plugin;
    
    public PlayerListener(TCProtect plugin){
        this.plugin = plugin;
    }
    
    public void RegisterEvents(){
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

}
