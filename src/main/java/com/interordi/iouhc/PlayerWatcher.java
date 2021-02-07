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
	boolean announceDeaths = false;
	boolean announceRevivals = false;
	boolean banOnDeath = false;
	
	private long respawnDelay = 60 * 24 * 7;	//In minutes
	
	
	public PlayerWatcher(IOUHC plugin) {
		this.deadPlayers = new HashMap< UUID, Date >();
		this.plugin = plugin;
		loadTheDead();
	}
	
	
	//The player's cooldown is over, reset his status
	public void respawn(Player p) {

		if (!banOnDeath)
			return;
		
		plugin.getLogger().info("Respawning " + p.getName());
		World w = Bukkit.getServer().getWorlds().get(0);
		
		p.teleport(w.getSpawnLocation());
		p.getInventory().clear();
		p.setGameMode(GameMode.SURVIVAL);
		
		p.sendMessage("You have been revived! Good luck!!");
		if (this.announceRevivals)
			plugin.getLogger().info("|IOBC|Player " + p.getName() + " has risen from the grave!!");
		
		this.deadPlayers.remove(p.getUniqueId());
		this.saveTheDead();
	}
	
	
	//Get the current list of dead players as stored in the file
	public void loadTheDead() {
		
		File statsFile = new File(this.filePath);

		try {
			if (!statsFile.exists())
				statsFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create the deaths file");
			e.printStackTrace();
			return;
		}

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
			//No deaths recorded yet, whatev'
			//plugin.getLogger().info("ERROR: Death YML section not found");
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
		if (dateDeath == null || !banOnDeath) {
			return;
		}
		
		long diffInMinutes = Math.abs(java.time.Duration.between(dateNow.toInstant(), dateDeath.toInstant()).toMinutes());
		
		if (diffInMinutes >= respawnDelay) {
			this.respawn(p);
		} else {
			//Process after a delay - allows users logging in to be fully in first
			Bukkit.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
				@Override
				public void run() {
					//Move player away
					p.sendMessage(plugin.colorize("&cYou are still dead! You'll be able to try again in " + plugin.getPlayerRespawnDelay(p) + "!"));
					plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "switch lobby " + p.getDisplayName());
				}
			}, 5*20L);
		}
	}
	
	
	//Get the respawn date and time of the current player
	public String getRespawnDelay(Player p) {
		Date dateNow = new Date();
		Date dateDeath = this.deadPlayers.get(p.getUniqueId());
		long diffInMinutes = Math.abs(java.time.Duration.between(dateNow.toInstant(), dateDeath.toInstant()).toMinutes());
		
		long remainingMinutes = respawnDelay - diffInMinutes;
		
		String delay = "";
		long days = 0;
		long hours = 0;
		long minutes = 0;
		
		if (remainingMinutes > 60 * 24) {
			days = remainingMinutes / (60 * 24);
			remainingMinutes -= days * 60 * 24;
		}
		if (remainingMinutes > 60) {
			hours = remainingMinutes / 60;
			remainingMinutes -= hours * 60;
		}
		minutes = remainingMinutes;
		
		if (days > 1)
			delay += days + " days ";
		else if (days == 1)
			delay += "1 day ";
		if (hours > 1)
			delay += hours + " hours ";
		else if (hours == 1)
			delay += "1 hour ";
		if (minutes > 1)
			delay += minutes + " minutes ";
		else if (minutes == 1)
			delay += "1 minute";
		
		return delay;
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


	public void setAnnounces(boolean deaths, boolean revivals) {
		this.announceDeaths = deaths;
		this.announceRevivals = revivals;
	}
	

	public void setBanOnDeath(boolean setting) {
		this.banOnDeath = setting;
	}
	
	
	@Override
	public void run() {
		//Check the status of each online player on a regular basis
		for (Player p : Bukkit.getOnlinePlayers()) {
			checkStatus(p);
		}
	}
}
