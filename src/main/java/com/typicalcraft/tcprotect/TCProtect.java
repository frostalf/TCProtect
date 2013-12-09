
package com.typicalcraft.tcprotect;

import com.typicalcraft.tcprotect.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Frostalf
 */
public class TCProtect extends JavaPlugin {
    
    private PlayerListener PlayerListener = new PlayerListener(this);
    
    
    @Override
    public void onEnable(){
        
        PlayerListener.RegisterEvents();
    }

}
