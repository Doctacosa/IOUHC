package com.interordi.iouhc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.interordi.iouhc.utilities.CommandTargets;
import com.interordi.iouhc.utilities.Commands;
import com.interordi.iouhc.utilities.Scores;


public class IOUHC extends JavaPlugin {

	LoginListener thisLoginListener;
	DeathListener thisDeathListener;
	PlayerWatcher thisPlayerWatcher;
	TargetsListener thisTargets;
	Scores thisScores;

	private Set< String > targetsActive = new HashSet< String >();
	private Map< String, String > targetsLabels = new HashMap< String, String >();
	
	
	public void onEnable() {
		thisScores = new Scores("Score");

		thisLoginListener = new LoginListener(this);
		thisDeathListener = new DeathListener(this);
		thisPlayerWatcher = new PlayerWatcher(this);
		thisTargets = new TargetsListener(this);

		//Always ensure we've got a copy of the config in place (does not overwrite existing)
		this.saveDefaultConfig();
		
		//Configuration file use (config.yml): http://wiki.bukkit.org/Configuration_API_Reference
		boolean announceDeaths = this.getConfig().getBoolean("announce-deaths");
		boolean announceRevivals = this.getConfig().getBoolean("announce-revivals");
		boolean banOnDeath = this.getConfig().getBoolean("ban-on-death", true);
		thisDeathListener.setAnnounces(announceDeaths, announceRevivals);
		thisPlayerWatcher.setAnnounces(announceDeaths, announceRevivals);
		thisDeathListener.setBanOnDeath(banOnDeath);
		thisPlayerWatcher.setBanOnDeath(banOnDeath);

		List< String > targetsData = this.getConfig().getStringList("targets");
		if (targetsData != null && !targetsData.isEmpty()) {
			targetsActive.addAll(targetsData);

		} else {
			getLogger().warning("ERROR: No targets found!!");
		}

		ConfigurationSection cfgLabels = this.getConfig().getConfigurationSection("labels");
		if (cfgLabels != null) {
			for (String key : cfgLabels.getKeys(false)) {
				targetsLabels.put(key, cfgLabels.getString(key));
			}
		}

		thisTargets.setTargets(targetsActive);

		
		//Check every minute for potential respawns
		getServer().getScheduler().scheduleSyncRepeatingTask(this, thisPlayerWatcher, 60*20L, 60*20L);
		
		getLogger().info("IOUHC enabled");
	}
	
	
	public void onDisable() {
		getLogger().info("IOUHC disabled");
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Get the list of potential targets if a selector was used
		CommandTargets results = Commands.findTargets(Bukkit.getServer(), sender, cmd, label, args);
		
		boolean result = false;
		if (results.position != -1) {
			//Run the command for each target identified by the selector
			for (String target : results.targets) {
				args[results.position] = target;
				
				result = runCommand(sender, cmd, label, args);
			}
		} else {
			//Run the command as-is
			result = runCommand(sender, cmd, label, args);
		}
		
		return result;
	}
	
	
	//Actually run the entered command
	public boolean runCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("target")) {
			
			//Only players can run this command
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
				return true;
			}
			
			Player player = (Player)sender;
			thisTargets.targetsOutput(player);

			return true;
		}
		return false;
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
	
	
	public Scores getScores() {
		return thisScores;
	}


	public Map< String, String > getTargetsLabels() {
		return targetsLabels;
	}


	public Set< String > getTargetsActive() {
		return targetsActive;
	}


	public String colorize(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
