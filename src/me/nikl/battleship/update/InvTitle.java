package me.nikl.battleship.update;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public interface InvTitle {

	
	public void updateTitle(Player player, String newTitle);
	
	ItemStack removeGlow(ItemStack item);
	
	ItemStack addGlow(ItemStack item);
}
