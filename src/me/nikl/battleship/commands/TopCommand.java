package me.nikl.battleship.commands;

import me.nikl.battleship.Language;
import me.nikl.battleship.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by niklas on 9/27/16.
 *
 *
 */
public class TopCommand implements CommandExecutor{
	
	private Main plugin;
	private FileConfiguration stats;
	private Language lang;
	private String structure;
	
	public TopCommand(Main plugin){
		this.plugin = plugin;
		this.stats = plugin.getStatistics();
		this.lang = plugin.lang;
		this.structure = lang.CMD_TOP_STRUCTURE;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("battleship.top")){
			sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_NO_PERM));
			return true;
		}
		Map<UUID, Integer> times = new HashMap<>();
		for(String uuid : stats.getKeys(false)) {
			if(stats.isInt(uuid + ".won"))
				times.put(UUID.fromString(uuid), stats.getInt(uuid + ".won"));
		}
		if(times.size() == 0){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + lang.CMD_NO_TOP_LIST));
			return true;
		}
		int length = (times.size() > 10? 10 : times.size());
		String[] messages = new String[length];
		UUID bestRecord = null;
		int record;
		for(int i = 0; i<length;i++){
			record = 0;
			for(UUID current : times.keySet()){
				if(record == 0){
					record = times.get(current);
					bestRecord = current;
					continue;
				}
				if(times.get(current) > record){
					record = times.get(current);
					bestRecord = current;
				}
			}
			// remove the entry that will be put into messages[] now
			times.remove(bestRecord);
			
			//Get the name of the current top player
			String name;
			if(bestRecord == null){
				name = "PlayerNotFound";
			} else {
				name = (Bukkit.getOfflinePlayer(bestRecord) == null ? "PlayerNotFound" : (Bukkit.getOfflinePlayer(bestRecord).getName() == null ? "PlayerNotFound" : Bukkit.getOfflinePlayer(bestRecord).getName()));
			}
			
			// put the current top player in the String array
			messages[i] = structure.replaceAll("%rank%", (i+1)+"").replaceAll("%name%", name).replaceAll("%won%", Integer.toString(record));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_HEAD));
		for(String message : messages){
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.CMD_TOP_TAIL));
		return true;
	}
}
