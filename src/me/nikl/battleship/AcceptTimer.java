package me.nikl.battleship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.nikl.battleship.game.GameManager;



public class AcceptTimer extends BukkitRunnable{
	private GameManager manager;
	private Set<Waiting> waiting;
	private Main plugin;
	private Language lang;
	public AcceptTimer(GameManager manager){
		this.manager = manager;
		this.plugin = manager.getPlugin();
		this.lang = plugin.lang;
		this.waiting = new HashSet<>();
		
		this.runTaskTimer(Main.getPlugin(Main.class), 0, 20);
	}

	@Override
	public void run() {
		for(Iterator<Waiting> waitI = waiting.iterator(); waitI.hasNext();){
			Waiting wait = waitI.next();
			if(wait.getAge()< manager.getPlugin().getInvitationValidFor()){
				wait.setAge(wait.getAge()+1);
			} else {
				Player first = Bukkit.getPlayer(wait.getFirst());
				Player second = Bukkit.getPlayer(wait.getSecond());
				if(first != null){
					first.sendMessage(colored(Main.prefix + lang.GAME_INVITE_EXPIRED));
					if(manager.getPlugin().getEconEnabled()){
						Main.econ.depositPlayer(first, 0);
						first.sendMessage(colored(Main.prefix + lang.GAME_INVITE_RETURNED_MONEY));
					}
				}
				if(second != null){
					second.sendMessage(colored(Main.prefix + lang.GAME_INVITE_EXPIRED));
				}
				waitI.remove();
			}
		}
	}
	
	private void addWaiting(Waiting wait){
		this.waiting.add(wait);
	}
	
	private String colored(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	/*public Boolean isFirst(UUID player){
		for(Waiting wait : waiting){
			if(wait.getFirst().equals(player)){
				return true;
			}
		}
		return false;
	}*/

	public int isSecond(UUID player){
		int count = 0;
		for(Waiting wait : waiting){
			if(wait.getSecond().equals(player)){
				count ++;
			}
		}
		return count;
	}
	
	public Waiting getWaiting(UUID second){
		for(Waiting wait : waiting){
			if(wait.getSecond().equals(second)){
				return wait;
			}
		}
		return null;		
	}
	
	public Waiting getWaiting(UUID first, UUID second){
		for(Waiting wait : waiting){
			if(wait.getFirst().equals(first) && wait.getSecond().equals(second)){
				return wait;
			}
		}
		return null;
	}
	
	public void removeWait(Waiting wait){
		waiting.remove(wait);
	}
	
	public boolean invite(UUID first, UUID second){
		Player firstPlayer = Bukkit.getPlayer(first);
		Player secondPlayer = Bukkit.getPlayer(second);
		if(firstPlayer == null || secondPlayer == null) return false;		
		
		if(plugin.getEconEnabled()){
			if(Main.econ.getBalance(firstPlayer) >= 0){
				Main.econ.withdrawPlayer(firstPlayer, 0);
				firstPlayer.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_PAYED.replaceAll("%cost%", 0+"")));
				addWaiting(new Waiting(manager, first, second));
				return true;					
			} else {
				firstPlayer.sendMessage(plugin.chatColor(Main.prefix + lang.GAME_NOT_ENOUGH_MONEY));
				return false;
			}
		} else {
			addWaiting(new Waiting(manager, first, second));
			return true;
		}
	}
}
