package com.interordi.iouhc;

import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


public class DeathListener implements Listener {
	
	IOUHC plugin;
	boolean announceDeaths = false;
	boolean announceRevivals = false;
	
	
	public DeathListener(IOUHC plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		@SuppressWarnings("unused")
		EntityType entity = event.getEntityType();
		//this.plugin.getServer().broadcastMessage("Entity death to " + entity + " - " + event.getEntity().getName());
		
		if (event.getEntity() instanceof Player) {
			Player p = (Player)event.getEntity();
			
			if (p.getGameMode() == GameMode.SURVIVAL) {
				//If we want to announce deaths, broadcast it
				if (this.announceDeaths)
					plugin.getLogger().info("|IOBC|Player " + p.getName() + " has been defeated!!");
				
				//Set respawn timer
				this.plugin.setDeadPlayer(p);
				
				//Move player away
				p.sendMessage(this.plugin.colorize("&cYou have lost! You'll be able to try again in " + this.plugin.getPlayerRespawnDelay(p) + "!"));
				this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), "switch lobby " + p.getDisplayName());
			}
			
			@SuppressWarnings("unused")
			EntityDamageEvent entityDamageCause = p.getLastDamageCause();
		} else {
			
		}
		
	}


	public void setAnnounces(boolean deaths, boolean revivals) {
		this.announceDeaths = deaths;
		this.announceRevivals = revivals;
	}
	
}
