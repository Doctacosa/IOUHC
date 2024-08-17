package com.interordi.iouhc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;


public class Scores {

	private Map< UUID, Integer > scoresPlayers;
	private String header;


	public Scores(String header) {
		this.scoresPlayers = new HashMap< UUID, Integer >();
		this.header = header;
	}


	//TODO: Keep top 3, only show online otherwise


	//Update a player's score on the global display
	public void updateScore(Player player, int score) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = board.getObjective("score");
		if (objective != null) {
			Score myScore = objective.getScore(player.getDisplayName());
			myScore.setScore(score);
		} else {
			Bukkit.getLogger().severe("No objective found!!");
		}
	}
	
	
	//Load the initial display of the scoreboard
	public void loadScores(Map< UUID, Integer > scores) {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective objective = board.getObjective("score");

		//If the objective doesn't exist yet, define it
		if (objective != null) {
			objective.unregister();
			objective = null;
		}

		scoresPlayers = scores;

		//Rebuild the scoreboard from the known data
		objective = board.registerNewObjective("score", "dummy", header);
		board.clearSlot(DisplaySlot.SIDEBAR);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (Map.Entry< UUID, Integer > score : scoresPlayers.entrySet()) {
			OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(score.getKey());
			String playerName = offPlayer.getName();
			if (!playerName.isEmpty()) {
				Score myScore = objective.getScore(playerName);
				
				int display = score.getValue();
				if (display > 0)
					myScore.setScore(display);
			}
		}
	}

}
