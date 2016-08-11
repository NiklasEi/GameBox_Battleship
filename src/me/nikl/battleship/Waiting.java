package me.nikl.battleship;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.nikl.battleship.game.GameManager;

public class Waiting {
	private int age;
	private UUID first;
	private UUID second;
	private Language lang;

	public Waiting(GameManager manager, UUID first, UUID second){
		this.setFirst(first);
		this.setSecond(second);
		Player firstPlayer = Bukkit.getPlayer(first);
		Player secondPlayer = Bukkit.getPlayer(second);
		if(firstPlayer == null){
			return;
		}
		if(secondPlayer == null){
			firstPlayer.sendMessage(colored(Main.prefix + lang.CMD_PLAYER_OFFLINE));
			return;
		}
		this.lang = manager.getPlugin().lang;
		for(String message: lang.GAME_INVITE_FIRST){
			firstPlayer.sendMessage(colored(Main.prefix + message.replaceAll("%first%", firstPlayer.getName()).replaceAll("%second%", secondPlayer.getName()).replaceAll("%time%", manager.getPlugin().getInvitationValidFor() + "")));
		}
		for(String message: lang.GAME_INVITE_SECOND){
			secondPlayer.sendMessage(colored(Main.prefix + message.replaceAll("%first%", firstPlayer.getName()).replaceAll("%second%", secondPlayer.getName()).replaceAll("%time%", manager.getPlugin().getInvitationValidFor() + "")));
		}

		this.setAge(0);
		//manager.getTimer().addWaiting(this);
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
