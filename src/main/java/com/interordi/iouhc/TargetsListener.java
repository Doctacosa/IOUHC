package com.interordi.iouhc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.*;

public class TargetsListener implements Listener {

	IOUHC plugin;
	private String filePath = "plugins/IOUHC/targets.yml";
	
	
	public TargetsListener(IOUHC plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		String worldName = event.getPlayer().getWorld().getName();
		
		if (worldName.equals("world_challenge_nether")) {
			this.saveTargets(event.getPlayer(), "nether", "access the Nether");
		}
	}
	
	
	@EventHandler
	public void onCraftItemEvent(CraftItemEvent event) {
		ItemStack result = event.getRecipe().getResult();
		Player player = (Player)event.getWhoClicked();
		
		if (result.getType() == Material.ENCHANTING_TABLE) {
			this.saveTargets(player, "enchantment_table", "crafted an enchantment table");
		} else if (result.getType() == Material.BOOKSHELF) {
			this.saveTargets(player, "bookshelf", "crafted a bookshelf");
		}
	}
	
	
	@EventHandler
	public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		
		if (event.getItem().getItemStack().getType() == Material.BLAZE_ROD) {
			this.saveTargets(player, "blaze_rod", "picked up a blaze rod");
		}
	}
	
	
	//Update the list to the file
	public void saveTargets(Player player, String target, String label) {
		File statsFile = new File(this.filePath);
		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		if (!statsAccess.contains("targets"))
			statsAccess.set("targets", "");
		
		if (!statsAccess.contains("targets." + player.getUniqueId())) {
			statsAccess.set("targets." + player.getUniqueId() + ".name", player.getCustomName());
			statsAccess.set("targets." + player.getUniqueId() + ".targets", "");
		}
		
		String statsTargets = "targets." + player.getUniqueId() + ".targets";
		
		List< String > targets = statsAccess.getStringList(statsTargets);
		
		if (targets == null || !targets.contains(target)) {
			
			if (targets == null)
				statsAccess.set(statsTargets, target);
			else {
				targets.add(target);
				statsAccess.set(statsTargets, targets);
			}
			
			player.sendMessage("§a§lTARGET REACHED: §r§a" + label + "!");
		
			try {
				statsAccess.save(statsFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
