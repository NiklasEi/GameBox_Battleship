package me.nikl.battleship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_PLAYER_OFFLINE, CMD_PLAYER_INGAME, CMD_PLAYER_HAS_INVITE, CMD_NOT_YOURSELF, CMD_FIRST_OFFLINE, CMD_RELOADED;
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY, GAME_WON_MONEY_GAVE_UP, GAME_WON, GAME_INVITE_ACCEPT, GAME_LOOSE, GAME_GAVE_UP, GAME_OTHER_GAVE_UP,
		GAME_TOO_SLOW, GAME_WON_MONEY_TOO_SLOW, GAME_WON_TOO_SLOW, GAME_INVITE_EXPIRED, GAME_INVITE_RETURNED_MONEY;
	public List<String> CMD_HELP, GAME_INVITE_FIRST, GAME_INVITE_SECOND;
	
	public Language(Main plugin){
		this.plugin = plugin;
		if(!getLangFile()){
			plugin.disabled = true;
			return;
		}
		getCommandMessages();
		getGameMessages();
	}
	
	private void getGameMessages() {
		this.GAME_INVITE_FIRST = getStringList("game.invite.messageToFirstPlayer");
		this.GAME_INVITE_SECOND = getStringList("game.invite.messageToSecondPlayer");	
		
		this.GAME_PAYED = getString("game.econ.payed");	
		this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");	
		this.GAME_WON_MONEY = getString("game.econ.wonMoney");	
		this.GAME_WON_MONEY_GAVE_UP = getString("game.econ.wonMoneyGaveUp");
		this.GAME_WON_MONEY_TOO_SLOW = getString("game.econ.wonMoneyTooSlow");		
		this.GAME_WON = getString("game.won");		
		this.GAME_INVITE_ACCEPT = getString("game.invite.inviteAccept");	
		this.GAME_LOOSE = getString("game.lost");	
		this.GAME_GAVE_UP = getString("game.gaveUp");	
		this.GAME_OTHER_GAVE_UP = getString("game.otherGaveUp");	
		this.GAME_TOO_SLOW = getString("game.tooSlow");	
		this.GAME_WON_TOO_SLOW = getString("game.otherTooSlow");	
		this.GAME_INVITE_EXPIRED = getString("game.invite.expired");
		this.GAME_INVITE_RETURNED_MONEY = getString("game.invite.returnedMoney");
		
	}

	private void getCommandMessages() {
		
		this.CMD_NO_PERM = getString("commandMessages.noPermission");
		this.CMD_ONLY_PLAYER = getString("commandMessages.onlyAsPlayer");
		this.CMD_PLAYER_OFFLINE = getString("commandMessages.playerIsOffline");
		this.CMD_PLAYER_INGAME = getString("commandMessages.playerAlreadyIngame");
		this.CMD_PLAYER_HAS_INVITE = getString("commandMessages.playerHasInviteAlready");
		this.CMD_NOT_YOURSELF = getString("commandMessages.cannotInviteYourself");
		this.CMD_FIRST_OFFLINE = getString("commandMessages.firstPlayerIsOffline");
		this.CMD_RELOADED = getString("commandMessages.pluginReloaded");
		

		this.CMD_HELP = getStringList("commandMessages.help");		
	}

	private List<String> getStringList(String path) {
		if(!langFile.isList(path)){
			return Arrays.asList(" &4StringList missing in Language file (" + path +")");
		}
		return langFile.getStringList(path);
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			return " &4String missing in language file! (" + path + ")";
		}
		return langFile.getString(path);
	}

	private boolean getLangFile() {
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;			
		}
		File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + plugin.getConfig().getString("langFile"));
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;			
		}
		if(!languageFile.exists()){
			languageFile.mkdir();
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file not found! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		}
		try { 
			this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8")); 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Error in language file! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Error in language file! Disabling plugin!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getServer().getPluginManager().disablePlugin(plugin);
			return false;
		} 
		return true;
		
	}
	
}

