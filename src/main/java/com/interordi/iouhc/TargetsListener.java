package com.interordi.iouhc;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.*;

public class TargetsListener implements Listener {

	IOUHC plugin;
	private String filePath = "plugins/IOUHC/targets.yml";
	private Set< String > activeTargets = new HashSet< String >();
	private Set< Material > itemsCheck = new HashSet< Material >();
	
	
	public TargetsListener(IOUHC plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		itemsCheck.add(Material.BLACK_BED);
		itemsCheck.add(Material.BLUE_BED);
		itemsCheck.add(Material.BROWN_BED);
		itemsCheck.add(Material.CYAN_BED);
		itemsCheck.add(Material.GRAY_BED);
		itemsCheck.add(Material.GREEN_BED);
		itemsCheck.add(Material.LIGHT_BLUE_BED);
		itemsCheck.add(Material.LIGHT_GRAY_BED);
		itemsCheck.add(Material.LIME_BED);
		itemsCheck.add(Material.MAGENTA_BED);
		itemsCheck.add(Material.ORANGE_BED);
		itemsCheck.add(Material.PINK_BED);
		itemsCheck.add(Material.PURPLE_BED);
		itemsCheck.add(Material.RED_BED);
		itemsCheck.add(Material.WHITE_BED);
		itemsCheck.add(Material.YELLOW_BED);
	}
	

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		String targetName = "nether";

		if (!activeTargets.contains(targetName))
			return;
		
		String worldName = event.getPlayer().getWorld().getName();
		
		if (worldName.endsWith("_nether")) {
			this.saveTargets(event.getPlayer(), targetName, "access the Nether");
		}
	}
	
	
	@EventHandler
	public void onCraftItemEvent(CraftItemEvent event) {

		ItemStack result = event.getRecipe().getResult();
		Player player = (Player)event.getWhoClicked();

		if (result.getType() == Material.ENCHANTING_TABLE && activeTargets.contains("enchantment_table")) {
			this.saveTargets(player, "enchantment_table", "crafted an enchantment table");
		} else if (result.getType() == Material.BOOKSHELF && activeTargets.contains("bookshelf")) {
			this.saveTargets(player, "bookshelf", "crafted a bookshelf");
		} else if (result.getType() == Material.ENDER_CHEST && activeTargets.contains("enderchest")) {
			this.saveTargets(player, "enderchest", "crafted an enderchest");
		}
	}
	
	
	@EventHandler
	public void onPlayerPickupItemEvent(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		
		String targetName = "blaze_rod";
		
		if (!activeTargets.contains(targetName))
			return;
		
		Player player = (Player)event.getEntity();
		
		if (event.getItem().getItemStack().getType() == Material.BLAZE_ROD) {
			this.saveTargets(player, targetName, "picked up a blaze rod");
		}
	}


	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {

		String targetName = "player_kill";

		if (!activeTargets.contains(targetName))
			return;
		
		if (!(event.getEntity() instanceof Player) || !(event.getEntity().getKiller() instanceof Player))
			return;

		Player player = (Player)event.getEntity().getKiller();
		
		this.saveTargets(player, targetName, "killed another player");
	}


	@EventHandler
	public void onPlayerLevelChangeEvent(PlayerLevelChangeEvent event) {

		String targetName = "level_30";
		
		if (!activeTargets.contains(targetName))
			return;
		
		if (event.getNewLevel() >= 30) {
			this.saveTargets(event.getPlayer(), targetName, "got to level 30");
		}
	}


	@EventHandler
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {

		String targetName = "bucket_lava";
		
		if (!activeTargets.contains(targetName))
			return;
		
		if (event.getBucket() == Material.LAVA_BUCKET) {
			this.saveTargets(event.getPlayer(), targetName, "emptied a lava bucket");
		}
	}


	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {

		if (itemsCheck.contains(event.getBlock().getType())) {
			String targetName = "item_" + event.getBlock().getType().toString().toLowerCase();
			if (!activeTargets.contains(targetName)) {
				return;
			}

			this.saveTargets(event.getPlayer(), targetName, "broke the right item");
		}
	}
	
	
	//Update the list to the file
	public void saveTargets(Player player, String target, String label) {
		File statsFile = new File(this.filePath);

		try {
			if (!statsFile.exists())
				statsFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create the targets file");
			e.printStackTrace();
			return;
		}

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
	

	//Set the active targets for the current cycle
	public void setTargets(Set< String > currentTargets) {
		System.out.println("Set targets");
		for (String target : currentTargets)
			System.out.println("- " + target);
			
		this.activeTargets = currentTargets;
	}
}
