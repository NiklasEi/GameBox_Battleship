package me.nikl.battleship.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.meta.SkullMeta;

import me.nikl.battleship.GameManager;
import me.nikl.battleship.Language;
import me.nikl.battleship.Main;

public class GUIListener implements Listener{
	
	private Main plugin;
	private GameManager manager;
	private Language lang;
	
	public GUIListener(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.manager = plugin.getManager();
		
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	

	@EventHandler
	public void onInvClick(InventoryClickEvent e){
		if(e.getClickedInventory() == null || e.getCurrentItem() == null){
			return;
		}
		if(!plugin.headGUI.guiIsOpen(e.getWhoClicked().getUniqueId())){
			return;
		}
		e.setCancelled(true);
		if(e.getCurrentItem().getItemMeta() instanceof SkullMeta){
			Player target = Bukkit.getPlayer(((SkullMeta) e.getCurrentItem().getItemMeta()).getOwner());
			if(target == null){
				e.getWhoClicked().sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_OFFLINE));
				return;
			}
			if(manager.isIngame(target.getUniqueId())){
				e.getWhoClicked().sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_INGAME));
				return;				
			}
			if(manager.getTimer().isSecond(target.getUniqueId())){
				e.getWhoClicked().sendMessage(plugin.chatColor(Main.prefix + lang.CMD_PLAYER_HAS_INVITE));
				return;									
			}
			manager.getTimer().invite(e.getWhoClicked().getUniqueId(), target.getUniqueId());
			e.getWhoClicked().closeInventory();
			return;
			
		}
		if(e.getCurrentItem().getType() == Material.BARRIER){
			e.getWhoClicked().closeInventory();
			return;
		}
		if(e.getCurrentItem().getType() == Material.STAINED_GLASS_PANE && e.getCurrentItem().getDurability() == (short) 13){
			plugin.headGUI.getGUI(e.getWhoClicked().getUniqueId()).showNextPage();
			return;
		}
		if(e.getCurrentItem().getType() == Material.STAINED_GLASS_PANE && e.getCurrentItem().getDurability() == (short) 14){
			plugin.headGUI.getGUI(e.getWhoClicked().getUniqueId()).showLastPage();
			return;
		}
	}

	
	
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e){
		if(!plugin.headGUI.guiIsOpen(e.getPlayer().getUniqueId())){
			return;
		}
		if(plugin.headGUI.getGUI(e.getPlayer().getUniqueId()).isChangingInv()){
			return;			
		}
		plugin.headGUI.removeGUI(plugin.headGUI.getGUI(e.getPlayer().getUniqueId()));
		
	}
}
