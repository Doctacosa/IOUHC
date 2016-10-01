package com.interordi.iouhc;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;


public class DeathListener implements Listener {
	
	IOUHC plugin;
	boolean announceDeaths = false;
	
	
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
			
			//If we want to announce deaths, broadcast it
			if (this.announceDeaths)
				plugin.getLogger().info("|IOBC|Player " + p.getName() + " has been defeated!!");
			this.plugin.setDeadPlayer(p);
			
			@SuppressWarnings("unused")
			EntityDamageEvent entityDamageCause = p.getLastDamageCause();
		} else {
			
		}
		
	}


	public void setAnnounceDeaths(boolean value) {
		this.announceDeaths = value;
	}
	
}
