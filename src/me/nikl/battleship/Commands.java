package me.nikl.battleship;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
				player.sendMessage(plugin.chatColor(Main.prefix + " &4You are ingame already!"));
				return true;				
			}
			if(manager.getTimer().isSecond(player.getUniqueId())){
				Player firstplayer = Bukkit.getPlayer(manager.getTimer().getWaiting(player.getUniqueId()).getFirst());
				if(firstplayer == null){
					player.sendMessage(plugin.chatColor(Main.prefix + " &4Whoever invited you is offline now..."));
					manager.getTimer().removeWait(manager.getTimer().getWaiting(player.getUniqueId()));
					return true;
				}
				
				if(plugin.getEconEnabled()){
					if(Main.econ.getBalance(player) >= plugin.getPrice()){
						Main.econ.withdrawPlayer(player, plugin.getPrice());
						sender.sendMessage(plugin.chatColor(Main.prefix + " &2You paid &4" + plugin.getPrice()));
						manager.startGame(firstplayer.getUniqueId(), player.getUniqueId());
					} else {
						player.sendMessage(plugin.chatColor(Main.prefix + " &4You do not have not enough money!"));
						return true;
					}
				} else {
					manager.startGame(firstplayer.getUniqueId(), player.getUniqueId());
				}
				manager.getTimer().removeWait(manager.getTimer().getWaiting(player.getUniqueId()));
				player.sendMessage(plugin.chatColor(Main.prefix + " &2You accepted " + firstplayer.getName() +"'s invitation"));
				return true;
			}
			sender.sendMessage(plugin.chatColor(Main.prefix + " &4To invite someone to a game do: /bs player"));
			return true;
		} else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
			if(sender.hasPermission("battleship.reload")){
				plugin.reload();
				sender.sendMessage(plugin.chatColor(Main.prefix + " &aPlugin was reloaded"));
				return true;
			} else {
				sender.sendMessage(plugin.chatColor(Main.prefix + " &4 You do not have the required permission!"));
				return true;
			}
		
		} else if(args.length == 1){
			/*
			 * Sender invites another player to a game
			 * Here the new Wait() is made and if needed the first player will pay for the game
			 */
			if(!(sender instanceof Player)){
				sender.sendMessage(plugin.chatColor(Main.prefix + this.lang.CMD_ONLY_PLAYER));
				return true;
			}
			Player player = (Player) sender;
			if(player.getName().equals(args[0])){
				player.sendMessage(plugin.chatColor(Main.prefix + " &4You cannot invite yourself to a game ^^"));
				return true;
			}
			Player second =  Bukkit.getPlayer(args[0]);
			if(second == null){
				sender.sendMessage(plugin.chatColor(Main.prefix + " &4This player is not online!"));
				return true;				
			}
			if(manager.isIngame(second.getUniqueId())){
				sender.sendMessage(plugin.chatColor(Main.prefix + " &4This player is already ingame!"));
				return true;				
			}
			if(manager.getTimer().isSecond(second.getUniqueId())){
				sender.sendMessage(plugin.chatColor(Main.prefix + " &4This player was already invited by someone!"));
				return true;									
			}
			manager.getTimer().invite(player.getUniqueId(), second.getUniqueId());
			return true;
		}
		sender.sendMessage(plugin.chatColor(Main.prefix + " Do &2/bs [player] &r to invite someone. Or use the gui with &2/bsgui"));
		return true;
	}

}
