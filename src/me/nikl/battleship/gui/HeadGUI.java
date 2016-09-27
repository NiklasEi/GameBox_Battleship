package me.nikl.battleship.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.nikl.battleship.Language;
import me.nikl.battleship.Main;

public class HeadGUI implements CommandExecutor {
	private Main plugin;
	private Set<GUI> guis;
	private Language lang;
	private FileConfiguration stats;
	
	public HeadGUI(Main plugin){
		this.plugin = plugin;
		this.lang = plugin.lang;
		this.stats = plugin.getStatistics();
		this.guis = new HashSet<GUI>();
		new GUIListener(plugin);
	}
	
	public void removeGUI(GUI gui){
		this.guis.remove(gui);
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {

		if(!(sender instanceof Player)){
			sender.sendMessage(plugin.chatColor(Main.prefix + this.lang.CMD_ONLY_PLAYER));
			return false;
		}
		Player player = (Player) sender;
		if(args.length == 0){
			if(Bukkit.getOnlinePlayers().size() > 1){
				guis.add(new GUI(player.getUniqueId()));
				return true;
			} else {
				sender.sendMessage(plugin.chatColor(Main.prefix + lang.CMD_ONLY_ONE_ONLINE));
				return true;
			}
		}

		
		
		for(String message : this.lang.CMD_HELP)
			sender.sendMessage(plugin.chatColor(Main.prefix + message));
		return true;
	}


	
	public class GUI {
		private UUID owner;
		private Inventory[] inv;
		private int currentPage;
		private boolean changingInv;
		
		
		public GUI(UUID owner){
			this.owner = owner;
			this.setChangingInv(false);
			build();
			open();
		}
		
		
		
	    private void build() {
	    	int headsPerPage = 45; // 45
	    	Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			int pageNum = (players.size() - 1) / headsPerPage; // last row of inventory stays empty for buttons (exit, forward, backward)
			if(!(((players.size() - 1) % headsPerPage) == 0 )){
				pageNum++;
			}
			
			this.inv = new Inventory[pageNum];
			
			for(int invPage = 0; invPage < pageNum; invPage ++){
				this.inv[invPage] = Bukkit.createInventory(null, 54, lang.TITLE_GUI+ "   " + (invPage+1));
			}
			
			
			int playerNumber = 0, page = 0;
			for(Player player : players){
				if(player.getUniqueId().equals(owner)){
					continue;
				}
				if(playerNumber >= (page+1)*headsPerPage){ // previous page is full => switch to next page
					page++;
				}
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
				ArrayList<String> skullLore = new ArrayList<String>();
				
				skullLore.add(ChatColor.GOLD + "Click to invite ");
				if(stats.isSet(player.getUniqueId().toString())){
					skullLore.add(ChatColor.BLUE + "");
					skullLore.add(ChatColor.BLUE + "Wins: " + stats.getString(player.getUniqueId().toString()));
				} else {
					skullLore.add(ChatColor.BLUE + "");
					skullLore.add(ChatColor.BLUE + "Wins: 0");
				}
				
				skullMeta.setOwner(player.getName());
				skullMeta.setLore(skullLore);
				skullMeta.setDisplayName(ChatColor.BLUE + player.getName());
				
				skull.setItemMeta(skullMeta);
				

				this.inv[page].setItem(playerNumber - page*headsPerPage, skull);
				
				
				
				
				playerNumber++;
			}
			
			
			for(int invPage = 0; invPage < pageNum; invPage ++){
				if(invPage == 0){
					if(!(invPage == pageNum - 1)){
						setForwardItem(invPage);
					}
				} else {
					setBackItem(invPage);
				}
				setExitItem(invPage);
			}
		}
	
	
	
		private void setExitItem(int invPage) {
			ItemStack exitButton = new ItemStack(Material.BARRIER, 1);
			
			ItemMeta exitMeta = exitButton.getItemMeta();
			
			exitMeta.setDisplayName(ChatColor.RED + "Click to exit");
			
			exitButton.setItemMeta(exitMeta);
			
			this.inv[invPage].setItem(49, exitButton);
		}
	
	
	
		private void setBackItem(int invPage) {
			ItemStack backButton = new ItemStack(Material.STAINED_GLASS_PANE, 1);
			backButton.setDurability((short) 14);
			
			ItemMeta backMeta = backButton.getItemMeta();
			
			backMeta.setDisplayName(ChatColor.GOLD + "Go back");
			
			backButton.setItemMeta(backMeta);

			this.inv[invPage].setItem(47, backButton);		
		}
	
	
	
		private void setForwardItem(int invPage) {
			ItemStack forwardButton = new ItemStack(Material.STAINED_GLASS_PANE, 1);
			forwardButton.setDurability((short) 13);
			
			ItemMeta forwardMeta = forwardButton.getItemMeta();
			
			forwardMeta.setDisplayName(ChatColor.GOLD + "Next page");
			
			forwardButton.setItemMeta(forwardMeta);
			
			this.inv[invPage].setItem(51, forwardButton);		
		}
	
	
	
		public void showGUI(Player player) {
	        new GUI(player.getUniqueId()).open();
	    }
	
		public void show(int page) {
			this.currentPage = page;
			this.changingInv = true;
			Bukkit.getPlayer(owner).openInventory(this.inv[page]);	
			this.changingInv = false;	
		}
		
		public void showNextPage(){
			this.show(currentPage+1);
		}
		
		public void showLastPage(){
			this.show(currentPage-1);
		}
		
		public void open(){
			show(0);
		}



		public boolean isChangingInv() {
			return changingInv;
		}



		public void setChangingInv(boolean changingInv) {
			this.changingInv = changingInv;
		}
	}



	public boolean guiIsOpen(UUID owner) {
		for(GUI gui: guis){
			if(gui.owner.equals(owner)){
				return true;
			}
		}
		return false;
	}


	public GUI getGUI(UUID owner) {
		for(GUI gui: guis){
			if(gui.owner.equals(owner)){
				return gui;
			}
		}
		return null;
	}
}
