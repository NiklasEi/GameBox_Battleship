package me.nikl.battleship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;



public class AcceptTimer extends BukkitRunnable{
	private GameManager manager;
	private Set<Waiting> waiting;
	private Main plugin;
	public AcceptTimer(GameManager manager){
		this.manager = manager;
		this.plugin = manager.getPlugin();
		this.waiting = new HashSet<Waiting>();
		
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
					first.sendMessage(colored(Main.prefix + " &4Your invite was not accepted"));
					if(manager.getPlugin().getEconEnabled()){
						Main.econ.depositPlayer(first, manager.getPlugin().getPrice());
						first.sendMessage(colored(Main.prefix + " &2Your money has been returned"));
					}
				}
				if(second != null){
					second.sendMessage(colored(Main.prefix + " &4The invite has expired"));
				}
				waitI.remove();
			}
		}
	}
	
	public void addWaiting(Waiting wait){
		this.waiting.add(wait);
	}
	
	private String colored(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public Boolean isFirst(UUID player){
		for(Waiting wait : waiting){
			if(wait.getFirst().equals(player)){
				return true;
			}
		}
		return false;
	}

	public Boolean isSecond(UUID player){
		for(Waiting wait : waiting){
			if(wait.getSecond().equals(player)){
				return true;
			}
		}
		return false;
	}
	
	public Waiting getWaiting(UUID second){
		for(Waiting wait : waiting){
			if(wait.getSecond().equals(second)){
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
			if(Main.econ.getBalance(firstPlayer) >= plugin.getPrice()){
				Main.econ.withdrawPlayer(firstPlayer, plugin.getPrice());
				firstPlayer.sendMessage(plugin.chatColor(Main.prefix + " &2You paid &4" + plugin.getPrice()));
				addWaiting(new Waiting(manager, first, second));
				return true;					
			} else {
				firstPlayer.sendMessage(plugin.chatColor(Main.prefix + " &4You do not have enough money!"));
				return false;
			}
		} else {
			addWaiting(new Waiting(manager, first, second));
			return true;
		}
	}
}
