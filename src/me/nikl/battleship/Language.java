package me.nikl.battleship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;
	
	public String CMD_NO_PERM, CMD_ONLY_PLAYER, CMD_PLAYER_OFFLINE, CMD_PLAYER_INGAME, CMD_PLAYER_HAS_INVITE, CMD_NOT_YOURSELF;
	public List<String> CMD_HELP, CMD_INVITE_FIRST, CMD_INVITE_SECOND;
	
	public Language(Main plugin){
		this.plugin = plugin;
		if(!getLangFile()){
			plugin.disabled = true;
			return;
		}
		getCommandMessages();
	}
	
	private void getCommandMessages() {
		
		this.CMD_NO_PERM = langFile.getString("commandMessages.noPermission");
		this.CMD_ONLY_PLAYER = langFile.getString("commandMessages.onlyAsPlayer");
		this.CMD_PLAYER_OFFLINE = langFile.getString("commandMessages.playerIsOffline");
		this.CMD_PLAYER_INGAME = langFile.getString("commandMessages.playerAlreadyIngame");
		this.CMD_PLAYER_HAS_INVITE = langFile.getString("commandMessages.playerHasInviteAlready");
		this.CMD_NOT_YOURSELF = langFile.getString("commandMessages.cannotInviteYourself");
		

		this.CMD_HELP = langFile.getStringList("commandMessages.help");
		this.CMD_INVITE_FIRST = langFile.getStringList("commandMessages.invite.messageToFirstPlayer");
		this.CMD_INVITE_SECOND = langFile.getStringList("commandMessages.invite.messageToSecondPlayer");
		
	}

	private boolean getLangFile() {
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		if(!plugin.getConfig().isString("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Main.prefix + " &4Language file is missing in the config!"));
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

