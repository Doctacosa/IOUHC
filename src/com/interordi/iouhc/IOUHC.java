package com.interordi.iouhc;

import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iouhc.DeathListener;

public class IOUHC extends JavaPlugin {

	DeathListener thisDeathListener;
	
	public void onEnable() {
		thisDeathListener = new DeathListener(this);
		
		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();
		
		//Configuration file use (config.yml): http://wiki.bukkit.org/Configuration_API_Reference
		thisDeathListener.setAnnounceDeaths(this.getConfig().getBoolean("announce-deaths"));
		
		getLogger().info("IOUHC enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOUHC disabled");
	}
	
	


}
