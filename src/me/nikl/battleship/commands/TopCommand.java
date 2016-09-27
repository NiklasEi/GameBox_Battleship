package me.nikl.battleship.commands;

import me.nikl.battleship.Language;
import me.nikl.battleship.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by niklas on 9/27/16.
 */
public class TopCommand implements CommandExecutor{
	
	private Main plugin;
	private FileConfiguration stats;
	private Language lang;
	
	public TopCommand(Main plugin){
		this.plugin = plugin;
		this.stats = plugin.getStatistics();
		this.lang = plugin.lang;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return true;
	}
}
