package com.interordi.iouhc;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class PlayerWatcher implements Runnable {

	IOUHC plugin;
	private String filePath = "plugins/IOUHC/dead.yml";
	Map< UUID, Date > deadPlayers;
	
	private long respawnDelay = 60 * 24 * 7;	//In minutes
	
	
	public PlayerWatcher(IOUHC plugin) {
		this.deadPlayers = new HashMap< UUID, Date >();
		this.plugin = plugin;
		loadTheDead();
	}
	
	
	//The player's cooldown is over, reset his status
	public void respawn(Player p) {
		
		plugin.getLogger().info("Respawning " + p.getName());
		World w = Bukkit.getServer().getWorlds().get(0);
		
		p.teleport(w.getSpawnLocation());
		p.getInventory().clear();
		p.setGameMode(GameMode.SURVIVAL);
		
		p.sendMessage("You have been revived! Good luck!!");
		
		this.deadPlayers.remove(p.getUniqueId());
		this.saveTheDead();
	}
	
	
	//Get the current list of dead players as stored in the file
	public void loadTheDead() {
		
		File statsFile = new File(this.filePath);
		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		ConfigurationSection deathsData = statsAccess.getConfigurationSection("deaths");
		if (deathsData == null) {
			plugin.getLogger().info("ERROR: Death YML section not found");
			return;	//Nothing yet, exit
		}
		Set< String > cs = deathsData.getKeys(false);
		if (cs == null) {
			plugin.getLogger().info("ERROR: Couldn't get player keys");
			return;	//No players found, exit
		}
		
		if (cs.size() == 0) {
			plugin.getLogger().info("ERROR: Death YML section not found");
		}
		
		
		//Loop on each player
		for (String temp : cs) {
			UUID uuid = UUID.fromString(temp);
			String dateTemp = deathsData.getString(temp);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date;
			try {
				date = format.parse(dateTemp);
				deadPlayers.put(uuid, date);
			} catch (ParseException e) {
				plugin.getLogger().info("Invalid date: " + dateTemp);
			}
		}
	}
	
	
	//Update the list to the file
	public void saveTheDead() {
		File statsFile = new File(this.filePath);
		FileConfiguration statsAccess = YamlConfiguration.loadConfiguration(statsFile);
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		statsAccess.set("deaths", "");
		
		for (Map.Entry< UUID , Date > entry : this.deadPlayers.entrySet()) {
			UUID uuid = entry.getKey();
			Date date = entry.getValue();
			
			statsAccess.set("deaths." + uuid, format.format(date));
		}
		
		try {
			statsAccess.save(statsFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	//Check the status of the current player and respawn them as needed
	public void checkStatus(Player p) {
		Date dateNow = new Date();
		Date dateDeath = this.deadPlayers.get(p.getUniqueId());
		
		//Not dead, exit
		if (dateDeath == null) {
			return;
		}
		
		long diffInMinutes = Math.abs(java.time.Duration.between(dateNow.toInstant(), dateDeath.toInstant()).toMinutes());
		
		if (diffInMinutes >= respawnDelay) {
			this.respawn(p);
		}
		
	}


	//Register the death of a player
	public void setDeadPlayer(Player p) {
		this.deadPlayers.put(p.getUniqueId(), new Date());
		this.saveTheDead();
	}
	
	
	//Return the respawn delay, in minutes
	public long getRespawnDelay() {
		return this.respawnDelay;
	}


	@Override
	public void run() {
		//Check the status of each online player on a regular basis
		for (Player p : Bukkit.getOnlinePlayers()) {
			checkStatus(p);
		}
	}
}
