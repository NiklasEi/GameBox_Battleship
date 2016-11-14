package me.nikl.battleship;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.logging.Level;

import me.nikl.battleship.commands.Commands;
import me.nikl.battleship.commands.TopCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.nikl.battleship.game.GameManager;
import me.nikl.battleship.gui.HeadGUI;
import me.nikl.battleship.update.*;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{

	private GameManager manager;
	private FileConfiguration config, stats;
	private File con, sta;
	public static Economy econ = null;
	public static String prefix = "[&3Battleship&r]";
	public Boolean econEnabled;
	public Double reward, price;
	private int invitationValidFor;
	public HeadGUI headGUI;
	public Language lang;
	public boolean disabled;
	private InvTitle updater;
	
	@Override
	public void onEnable(){
        if (!setupUpdater()) {
            getLogger().severe("Your server version is not compatible with this plugin!");

            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
		this.disabled = false;
		this.con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
		this.sta = new File(this.getDataFolder().toString() + File.separatorChar + "stats.yml");

		reload();
		if(disabled) return;
	}
	
	private boolean setupUpdater() {
		String version;

	    try {
	        version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
	    } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
	        return false;
	    }
	
	    //getLogger().info("Your server is running version " + version);
	
	    if (version.equals("v1_10_R1")) {
	        updater = new Update_1_10_R1();
	        
	    } else if (version.equals("v1_9_R2")) {
	        updater = new Update_1_9_R2();
	        
	    } else if (version.equals("v1_9_R1")) {
	        updater = new Update_1_9_R1();
	        
	    } else if (version.equals("v1_8_R3")) {
	        updater = new Update_1_8_R3();
	        
	    } else if (version.equals("v1_8_R2")) {
	        updater = new Update_1_8_R2();
	        
	    } else if (version.equals("v1_8_R1")) {
	        updater = new Update_1_8_R1();
	    }
	    return updater != null;
	}

	public InvTitle getUpdater(){
		return this.updater;
	}
	private void getValuesFromConfig() {
		FileConfiguration config = getConfig();
		if(!config.isConfigurationSection("timers") || !config.isInt("timers.invitationTimer.validFor")){
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4No 'timers' section or invalid values in 'timers' section"));
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4Using default values!"));
			this.invitationValidFor = 15;
		} else {
			ConfigurationSection timer = config.getConfigurationSection("timers");
			this.invitationValidFor = timer.getInt("invitationTimer.validFor");
		}		
	}

	@Override
	public void onDisable(){
		if(stats!=null){
			try {
				this.stats.save(sta);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not save statistics", e);
			}		
		}
	}
	
    private boolean setupEconomy(){
    	if (getServer().getPluginManager().getPlugin("Vault") == null) {
    		return false;
    	}
    	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    	if (rsp == null) {
    		return false;
    	}
    	econ = (Economy)rsp.getProvider();
    	return econ != null;
    }
	
	public void reloadConfig(){
		try { 
			this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.con), "UTF-8")); 
		} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
		} 
 
		InputStream defConfigStream = this.getResource("config.yml"); 
		if (defConfigStream != null){		
			@SuppressWarnings("deprecation") 
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream); 
			this.config.setDefaults(defConfig); 
		} 
	} 
	
	public GameManager getManager() {
		return manager;
	}
	
	public void reload(){
		if(!con.exists()){
			this.saveResource("config.yml", false);
		}
		if(!sta.exists()){
			try {
				sta.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reloadConfig();
		
		// if this method was not called from onEnable stats is not null and has to be saved to the file first!
		if(stats!=null){
			try {
				this.stats.save(sta);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not save statistics", e);
			}
		}
		
		// load statsfile
		try {
			this.stats = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(this.sta), "UTF-8"));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		
		this.lang = new Language(this);
		
		this.econEnabled = false;
		if(getConfig().getBoolean("economy.enabled")){
			this.econEnabled = true;
			if (!setupEconomy()){
				Bukkit.getConsoleSender().sendMessage(chatColor(prefix + " &4No economy found!"));
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			this.price = getConfig().getDouble("economy.cost");
			this.reward = getConfig().getDouble("economy.reward");
			if(price == null || reward == null || price < 0. || reward < 0.){
				Bukkit.getConsoleSender().sendMessage(chatColor(prefix + " &4Wrong configuration in section economy!"));
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		
		getValuesFromConfig();
		
		this.setManager(new GameManager(this));
		this.getCommand("battleship").setExecutor(new Commands(this));
		this.headGUI = new HeadGUI(this);
		this.getCommand("battleshipGUI").setExecutor(headGUI);
		this.getCommand("battleshipTop").setExecutor(new TopCommand(this));
	}

	public void addWinToStatistics(UUID uuid) {
		if(this.stats == null) return;
		if(!stats.isInt(uuid.toString() + "." + "won")){
			stats.set(uuid.toString() + "." + "won", 1);
			return;
		}
		this.stats.set(uuid.toString() + "." + "won", (this.stats.getInt(uuid.toString() + "." + "won")+1));
	}
	
	public void addLoseToStatistics(UUID uuid) {
		if(this.stats == null) return;
		if(!stats.isInt(uuid.toString() + "." + "lost")){
			stats.set(uuid.toString() + "." + "lost", 1);
			return;
		}
		this.stats.set(uuid.toString() + "." + "lost", (this.stats.getInt(uuid.toString() + "." + "lost")+1));
	}
	
	public FileConfiguration getStatistics(){
		return this.stats;
	}
	
	public void setManager(GameManager manager) {
		this.manager = manager;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public void setConfig(FileConfiguration config) {
		this.config = config;
	}
	
    public String chatColor(String message){
    	return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public Boolean getEconEnabled(){
    	return this.econEnabled;
    }
    
    public Double getReward(){
    	return this.reward;
    }
    
    public Double getPrice(){
    	return this.price;
    }

	public int getInvitationValidFor() {
		return invitationValidFor;
	}
}
