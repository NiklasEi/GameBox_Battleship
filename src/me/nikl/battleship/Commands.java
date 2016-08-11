package me.nikl.battleship;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nikl.battleship.game.GameManager;

public class Commands implements CommandExecutor {
	
	private Main plugin;
	private GameManager manager;
	private Language lang;
	
	public Commands(Main plugin){
		this.plugin = plugin;
		this.manager = plugin.getManager();
		this.lang = plugin.lang;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if(args.length == 0){
			if(!(sender instanceof Player)){
				sender.sendMessage(plugin.chatColor(Main.prefix + this.lang.CMD_ONLY_PLAYER));
				return true;
			}
			/*
			 * Check for a running invitation
			 * If there is one let the second player pay if needed
			 * Then start the game
			 */
			Player player = (Player) sender;
			if(manager.isIngame(player.getUniqueId())){
				player.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_INGAME));
				return true;				
			}
			if(manager.getTimer().isSecond(player.getUniqueId())){
				Player firstplayer = Bukkit.getPlayer(manager.getTimer().getWaiting(player.getUniqueId()).getFirst());
				if(firstplayer == null){
					player.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_FIRST_OFFLINE));
					manager.getTimer().removeWait(manager.getTimer().getWaiting(player.getUniqueId()));
					return true;
				}
				
				if(plugin.getEconEnabled()){
					if(Main.econ.getBalance(player) >= plugin.getPrice()){
						Main.econ.withdrawPlayer(player, plugin.getPrice());
						sender.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_PAYED.replaceAll("%cost%", plugin.getPrice()+"")));
						manager.startGame(firstplayer.getUniqueId(), player.getUniqueId());
					} else {
						player.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_NOT_ENOUGH_MONEY));
						return true;
					}
				} else {
					manager.startGame(firstplayer.getUniqueId(), player.getUniqueId());
				}
				manager.getTimer().removeWait(manager.getTimer().getWaiting(player.getUniqueId()));
				player.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_INVITE_ACCEPT.replaceAll("%first%", firstplayer.getName())));
				return true;
			}
			for(String message : this.lang.CMD_HELP)
				sender.sendMessage(plugin.chatColor(Main.prefix + message));
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
			if(sender.hasPermission("battleship.reload")){
				plugin.reload();
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_RELOADED));
				return true;
			} else {
				for(String message : this.lang.CMD_HELP)
					sender.sendMessage(plugin.chatColor(Main.prefix + message));
				return true;
			}
		
		} else if(args.length == 1){
			/*
			 * Sender invites another player to a game
			 * Here the new Wait() is made and if needed the first player will pay for the game
			 */
			if(!(sender instanceof Player)){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_ONLY_PLAYER));
				return true;
			}
			Player player = (Player) sender;
			if(player.getName().equals(args[0])){
				player.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_NOT_YOURSELF));
				return true;
			}
			Player second =  Bukkit.getPlayer(args[0]);
			if(second == null){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_OFFLINE));
				return true;				
			}
			if(manager.isIngame(second.getUniqueId())){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_INGAME));
				return true;				
			}
			if(manager.getTimer().isSecond(second.getUniqueId())){
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_HAS_INVITE));
				return true;									
			}
			manager.getTimer().invite(player.getUniqueId(), second.getUniqueId());
			return true;
		}
		for(String message : this.lang.CMD_HELP)
			sender.sendMessage(plugin.chatColor(Main.prefix + message));
		return true;
	}

}
