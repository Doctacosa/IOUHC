package com.interordi.iouhc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iouhc.DeathListener;
import com.interordi.iouhc.PlayerWatcher;


public class IOUHC extends JavaPlugin {

	LoginListener thisLoginListener;
	DeathListener thisDeathListener;
	PlayerWatcher thisPlayerWatcher;
	TargetsListener thisTargets;
	
	
	public void onEnable() {
		thisLoginListener = new LoginListener(this);
		thisDeathListener = new DeathListener(this);
		thisPlayerWatcher = new PlayerWatcher(this);
		thisTargets = new TargetsListener(this);
		
		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();
		
		//Configuration file use (config.yml): http://wiki.bukkit.org/Configuration_API_Reference
		boolean announceDeaths = this.getConfig().getBoolean("announce-deaths");
		boolean announceRevivals = this.getConfig().getBoolean("announce-revivals");
		thisDeathListener.setAnnounces(announceDeaths, announceRevivals);
		thisPlayerWatcher.setAnnounces(announceDeaths, announceRevivals);
		
		//Check every minute for potential respawns
		getServer().getScheduler().scheduleSyncRepeatingTask(this, thisPlayerWatcher, 60*20L, 60*20L);
		
		getLogger().info("IOUHC enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOUHC disabled");
	}
	
	
	public void checkStatus(Player p) {
		this.thisPlayerWatcher.checkStatus(p);
	}


	public void setDeadPlayer(Player p) {
		thisPlayerWatcher.setDeadPlayer(p);
	}


	public String getPlayerRespawnDelay(Player p) {
		return thisPlayerWatcher.getRespawnDelay(p);
	}
	
	
	public String colorize(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
