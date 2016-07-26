package me.nikl.battleship;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Waiting {
	private int age;
	private UUID first;
	private UUID second;

	public Waiting(GameManager manager, UUID first, UUID second){
		this.setFirst(first);
		this.setSecond(second);
		Player firstPlayer = Bukkit.getPlayer(first);
		Player secondPlayer = Bukkit.getPlayer(second);
		if(firstPlayer == null){
			return;
		}
		if(secondPlayer == null){
			firstPlayer.sendMessage(colored(Main.prefix + " &4This player is offline!"));
			return;
		}
		firstPlayer.sendMessage(colored(Main.prefix + " &2You invited " + secondPlayer.getName() + " to a game"));
		secondPlayer.sendMessage(colored(Main.prefix + " &2" + firstPlayer.getName() + " invited you to a game!"));
		secondPlayer.sendMessage(colored(Main.prefix + " &2Do /bs to accept.    Expires in &4"+ manager.getPlugin().getInvitationValidFor() +" seconds&2!"));

		this.setAge(0);
		manager.getTimer().addWaiting(this);
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public UUID getFirst() {
		return first;
	}

	public void setFirst(UUID first) {
		this.first = first;
	}

	public UUID getSecond() {
		return second;
	}

	public void setSecond(UUID second) {
		this.second = second;
	}

	private String colored(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
