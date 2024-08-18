package com.interordi.iouhc;

import org.bukkit.event.player.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LoginListener implements Listener {
	
	private IOUHC plugin;
	
	public LoginListener(IOUHC plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		this.plugin.checkStatus(event.getPlayer());
		plugin.getScores().refreshDisplay();
	}


	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event) {
		plugin.getScores().refreshDisplay();
	}
}
