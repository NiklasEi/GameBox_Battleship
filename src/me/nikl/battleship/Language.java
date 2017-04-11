package me.nikl.battleship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {
	private Main plugin;
	private FileConfiguration langFile;

	public String NAME, PREFIX;
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY, GAME_WON_MONEY_GAVE_UP, GAME_WON, GAME_LOSE, GAME_GAVE_UP, GAME_OTHER_GAVE_UP,
		GAME_TOO_SLOW, GAME_WON_MONEY_TOO_SLOW, GAME_WON_TOO_SLOW;
	public String TITLE_SET_SHIP_1, TITLE_SET_SHIP_2, TITLE_SET_SHIP_3, TITLE_SET_SHIP_4, TITLE_ATTACKER, TITLE_DEFENDER, TITLE_WON, TITLE_LOST;
	
	public List<String> GAME_HELP;
	private YamlConfiguration defaultLang;
	public String TITLE_CHANGING;
	
	public Language(Main plugin){
		this.plugin = plugin;
		PREFIX = "[&3Battleship&r]";
		if(!getLangFile()){
			Bukkit.getPluginManager().disablePlugin(plugin);
			plugin.disabled = true;
			return;
		}
		getGameMessages();
		getInvTitles();

		NAME = getString("name");
		PREFIX = getString("prefix");
	}
	
	private void getInvTitles() {
		this.TITLE_SET_SHIP_1 = getString("inventoryTitles.setShip1");
		this.TITLE_SET_SHIP_2 = getString("inventoryTitles.setShip2");
		this.TITLE_SET_SHIP_3 = getString("inventoryTitles.setShip3");
		this.TITLE_SET_SHIP_4 = getString("inventoryTitles.setShip4");
		this.TITLE_ATTACKER = getString("inventoryTitles.attacker");
		this.TITLE_DEFENDER = getString("inventoryTitles.defender");
		this.TITLE_WON = getString("inventoryTitles.won");		
		this.TITLE_LOST = getString("inventoryTitles.lost");

		this.TITLE_CHANGING = getString("inventoryTitles.changingGrids");
		
	}

	private void getGameMessages() {
		this.GAME_PAYED = getString("game.econ.payed");	
		this.GAME_NOT_ENOUGH_MONEY = getString("game.econ.notEnoughMoney");	
		this.GAME_WON_MONEY = getString("game.econ.wonMoney");	
		this.GAME_WON_MONEY_GAVE_UP = getString("game.econ.wonMoneyGaveUp");
		this.GAME_WON_MONEY_TOO_SLOW = getString("game.econ.wonMoneyTooSlow");		
		this.GAME_WON = getString("game.won");
		this.GAME_LOSE = getString("game.lost");
		this.GAME_GAVE_UP = getString("game.gaveUp");	
		this.GAME_OTHER_GAVE_UP = getString("game.otherGaveUp");	
		this.GAME_TOO_SLOW = getString("game.tooSlow");	
		this.GAME_WON_TOO_SLOW = getString("game.otherTooSlow");

		this.GAME_HELP = getStringList("gameHelp");

	}

	private List<String> getStringList(String path) {
		List<String> toReturn;
		if(!langFile.isList(path)){
			toReturn = defaultLang.getStringList(path);
			for(int i = 0; i<toReturn.size(); i++){
				toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
			}
			return toReturn;
		}
		toReturn = langFile.getStringList(path);
		for(int i = 0; i<toReturn.size(); i++){
			toReturn.set(i, ChatColor.translateAlternateColorCodes('&',toReturn.get(i)));
		}
		return toReturn;
	}

	private String getString(String path) {
		if(!langFile.isString(path)){
			return ChatColor.translateAlternateColorCodes('&',defaultLang.getString(path));
		}
		return ChatColor.translateAlternateColorCodes('&',langFile.getString(path));
	}

	private boolean getLangFile() {
		try {
			String fileName = "language/lang_en.yml";
			this.defaultLang =  YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(fileName), "UTF-8"));
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		File defaultEn = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_en.yml");
		if(!defaultEn.exists()){
			defaultEn.getParentFile().mkdirs();
			plugin.saveResource("language" + File.separatorChar + "lang_en.yml", false);
		}
		File defaultDe = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + "lang_de.yml");
		if(!defaultDe.exists()){
			plugin.saveResource("language" + File.separatorChar + "lang_de.yml", false);
		}
		
		if(!plugin.getConfig().isSet("langFile")){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is missing in the config!"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " Add the following to your config:"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " langFile: 'lang_en.yml'"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
			this.langFile = defaultLang;
		} else {
			if(!plugin.getConfig().isString("langFile")){
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file is invalid (no String)!"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
				Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
				this.langFile = defaultLang;
			} else {
				String fileName = plugin.getConfig().getString("langFile");
				if(fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("default.yml")){
					this.langFile = defaultLang;
					return true;
				}
				File languageFile = new File(plugin.getDataFolder().toString() + File.separatorChar + "language" + File.separatorChar + fileName);
				if(!languageFile.exists()){
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Language file not found!"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
					this.langFile = defaultLang;
				} else {
					try { 
						this.langFile = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(languageFile), "UTF-8")); 
					} catch (UnsupportedEncodingException | FileNotFoundException e) { 
						e.printStackTrace(); 
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Error while loading language file!"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&4*******************************************************"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Using default language file"));
						this.langFile = defaultLang;
					}
				}
			}
		}
		int count = 0;
		for(String key : defaultLang.getKeys(true)){
			if(defaultLang.isString(key)){
				if(!this.langFile.isString(key)){// there is a message missing
					if(count == 0){
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
						Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Missing message(s) in your language file!"));
					}
					Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " " + key));
					count++;
				}
			}
		}
		if(count > 0){
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Game will use default messages for these paths"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + ""));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Please get an updated language file"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4Or add the listed paths by hand"));
			Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', PREFIX + " &4*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*"));
		}
		return true;
		
	}
	
}

