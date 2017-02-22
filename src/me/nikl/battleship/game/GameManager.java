package me.nikl.battleship.game;

import java.util.*;
import java.util.logging.Level;

import me.nikl.battleship.Sounds;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.IGameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.nikl.battleship.AcceptTimer;
import me.nikl.battleship.Language;
import me.nikl.battleship.Main;

public class GameManager implements IGameManager{

	private Main plugin;
	private Set<Game> games;
	private AcceptTimer timer;
	private Language lang;

	private Map<String, GameRules> gameTypes = new HashMap<>();
	
	// sounds
	private Sound ownMissSound, othersMissSound, ownHitSound, othersHitSound, setShipSound, unSetShipSound, won, lost;

	public GameManager(Main plugin){
		this.plugin = plugin;
		this.timer = new AcceptTimer(this);
		this.games = new HashSet<>();
		this.lang = plugin.lang;
		
		
		this.ownMissSound = Sounds.SPLASH2.bukkitSound();
		this.othersMissSound = Sounds.SPLASH2.bukkitSound();
		
		this.ownHitSound = Sounds.ANVIL_LAND.bukkitSound();
		this.othersHitSound = Sounds.HURT_FLESH.bukkitSound();
		
		this.setShipSound = Sounds.ANVIL_LAND.bukkitSound();
		this.unSetShipSound = Sounds.SPLASH2.bukkitSound();
		
		this.won = Sounds.LEVEL_UP.bukkitSound();
		this.lost = Sounds.VILLAGER_NO.bukkitSound();
	}
	

	private Game getGame(UUID uuid) {
		for(Iterator<Game> gameI = games.iterator(); gameI.hasNext();){
			Game game = gameI.next();
			if(isIngame(uuid, game)){
				return game;
			}
		}
		return null;
	}
	
	public Main getPlugin(){
		return this.plugin;
	}

	public AcceptTimer getTimer() {
		return timer;
	}

	private boolean isIngame(UUID uuid, Game game) {
		if(isFirst(uuid, game) || isSecond(uuid, game)){
			return true;
		}		
		return false;
	}
	
	public boolean isFirst(UUID uuid, Game game){
		if(game.getFirstUUID().equals(uuid)){
			return true;
		}
		return false;
	}
	
	public boolean isSecond(UUID uuid, Game game){
		if(game.getSecondUUID().equals(uuid)){
			return true;
		}
		return false;
	}

	public void removeGame(Game game) {
		games.remove(game);		
	}
	
	String chatColor(String message){
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	@Override
	public boolean onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if(event.getSlot() >= event.getInventory().getSize() || event.getSlot() < 0) return false;
		if(!event.getAction().equals(InventoryAction.PICKUP_ALL) && !event.getAction().equals(InventoryAction.PICKUP_HALF)){
			return false;
		}
		Player player = (Player) event.getWhoClicked();
		Game game = getGame(player.getUniqueId());
		if(!getGame(player.getUniqueId()).isCurrentInventory(event.getClickedInventory())){
			//Bukkit.getConsoleSender().sendMessage("not current inv."); // XXX
			return false;
		}
		boolean isFirst = isFirst(player.getUniqueId(), game);
		int slot = event.getSlot();
		//Bukkit.getConsoleSender().sendMessage("State is: " + game.getState().toString()); // XXX
		switch(game.getState()){

			case SETTING_SHIP1:
				if(game.getShipsSet(isFirst, true)){
					return false;
				}
				if(game.isWater(event.getCurrentItem())){
					//player.sendMessage("clicked water"); // XXX
					game.setShip(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, 10f, 1f);
				} else if(game.isShip(event.getCurrentItem())){
					//player.sendMessage("clicked something else"); // XXX
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, 10f, 1f);
				}
				if(game.shipsSet(1, isFirst)){
					game.lockShips(isFirst);
					game.setShipsSet(isFirst, true);
					if(isFirst){
						game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
					} else {
						game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
					}
					game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
					if(game.getShipsSet(!isFirst, true)){
						game.setState(GameState.SETTING_SHIP2);
						//game.showInventory(!isFirst, true);
					}
				}
				//game.showInventory(isFirst, true);
				//e.getWhoClicked().sendMessage("number of games: " + games.size()); // XXX
				return true;

			case SETTING_SHIP2:
				if(game.getShipsSet(isFirst, true)){
					return false;
				}
				if(game.isWater(event.getCurrentItem())){
					game.setShip(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, 10f, 1f);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, 10f, 1f);
				}
				if(game.shipsSet(2, isFirst)){
					game.lockShips(isFirst);
					game.setShipsSet(isFirst, true);
					if(isFirst){
						game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
					} else {
						game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
					}
					game.setState("&2Battleship   &rWaiting...", isFirst, true);
					if(game.getShipsSet(!isFirst, true)){
						game.setState(GameState.SETTING_SHIP3);
						//game.showInventory(!isFirst, true);
					}
				}
				//game.showInventory(isFirst, true);
				return true;

			case SETTING_SHIP3:
				if(game.getShipsSet(isFirst, true)){
					return false;
				}
				if(game.isWater(event.getCurrentItem())){
					game.setShip(slot, isFirst);
					if(Main.playSounds)	player.playSound(player.getLocation(), setShipSound, 10f, 1f);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)	player.playSound(player.getLocation(), unSetShipSound, 10f, 1f);
				}
				if(game.shipsSet(3, isFirst)){
					game.lockShips(isFirst);
					game.setShipsSet(isFirst, true);
					if(isFirst){
						game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
					} else {
						game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
					}
					game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
					if(game.getShipsSet(!isFirst, true)){
						game.setState(GameState.SETTING_SHIP4);
						//game.showInventory(!isFirst, true);
					}
				}
				//game.showInventory(isFirst, true);
				return true;

			case SETTING_SHIP4:
				if(game.getShipsSet(isFirst, true)){
					return false;
				}
				if(game.isWater(event.getCurrentItem())){
					game.setShip(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, 10f, 1f);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, 10f, 1f);
				}
				if(game.shipsSet(4, isFirst)){
					game.lockShips(isFirst);
					game.setShipsSet(isFirst, true);
					if(isFirst){
						game.setFirstCurrentState("&2Battleship   " + "&rWaiting...           &2");
					} else {
						game.setSecondCurrentState("&2Battleship   " + "&rWaiting...           &2");
					}
					game.setState("&2Battleship   " + "&rWaiting...", isFirst, true);
					if(game.getShipsSet(!isFirst, true)){
						// if this is true, all ships are set and the game can start
						game.unLockShips();
						game.readyToStart();
						game.setState(GameState.FIRST_TURN);
						return true;
					}
				}
				//game.showInventory(isFirst, true);
				return true;

			case BUILDING:
				return false;

			case FINISHED:
				return false;

			case FIRST_TURN:
				if(!isFirst) return false;
				if(game.isCover(event.getCurrentItem())){
					if(!game.fire(isFirst, slot)){
						if(Main.playSounds) {
							player.playSound(player.getLocation(), ownMissSound, 10f, 1f);
							Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
							secondPlayer.playSound(secondPlayer.getLocation(), othersMissSound, 10f, 1f);
						}
						game.changeAttacker(false);
						//game.setState(GameState.SECOND_TURN);
					} else {
						if(game.isWon(isFirst)){
							game.setState(GameState.FINISHED);
							game.won(isFirst);
							if(Main.playSounds) {
								player.playSound(player.getLocation(), won, 10f, 1f);
								Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
								secondPlayer.playSound(secondPlayer.getLocation(), lost, 10f, 1f);
							}
						} else {
							if(Main.playSounds) {
								player.playSound(player.getLocation(), ownHitSound, 10f, 1f);
								Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
								secondPlayer.playSound(secondPlayer.getLocation(), othersHitSound, 10f, 1f);
							}
							if(!game.ruleFireAgainAfterHit){
								game.changeAttacker(false);
							}
						}
					}
					return true;
				}
				return false;

			case SECOND_TURN:
				if(isFirst) return false;
				if(game.isCover(event.getCurrentItem())){
					if(!game.fire(isFirst, slot)){
						game.changeAttacker(true);
						if(Main.playSounds) {
							player.playSound(player.getLocation(), ownMissSound, 10f, 1f);
							Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
							firstPlayer.playSound(firstPlayer.getLocation(), othersMissSound, 10f, 1f);
						}
						//game.setState(GameState.FIRST_TURN);
					} else {
						if(game.isWon(isFirst)){
							game.setState(GameState.FINISHED);
							game.won(isFirst);
							if(Main.playSounds) {
								player.playSound(player.getLocation(), won, 10f, 1f);
								Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
								firstPlayer.playSound(firstPlayer.getLocation(), lost, 10f, 1f);
							}
						} else {
							if(Main.playSounds) {
								player.playSound(player.getLocation(), ownHitSound, 10f, 1f);
								Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
								firstPlayer.playSound(firstPlayer.getLocation(), othersHitSound, 10f, 1f);
							}
							if(!game.ruleFireAgainAfterHit){
								game.changeAttacker(true);
							}
						}
					}
					return true;
				}
				return false;

			default:
				return false;

		}
	}

	@Override
	public boolean onInventoryClose(InventoryCloseEvent event) {
		//Bukkit.getConsoleSender().sendMessage("called inventoryClose"); // XXX
		if(!isInGame(event.getPlayer().getUniqueId())){
			//Bukkit.getConsoleSender().sendMessage("not ingame"); // XXX
			return false;
		}
		if(getGame(event.getPlayer().getUniqueId()).getClosingInv()){
			//Bukkit.getConsoleSender().sendMessage("the game closed this!"); // XXX
			return false;
		}
		if(!getGame(event.getPlayer().getUniqueId()).isCurrentInventory(event.getInventory())){
			return false;
		}
		Game game = getGame(event.getPlayer().getUniqueId());
		game.cancelTimer();
		Player first = Bukkit.getPlayer(game.getFirstUUID());
		Player second = Bukkit.getPlayer(game.getSecondUUID());
		boolean winner;
		if(first == null || second == null){
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4Error on inv. close"));
			Bukkit.getConsoleSender().sendMessage(chatColor(Main.prefix + " &4Deleting game..."));
			removeGame(game);
			if(first != null){
				first.closeInventory();
			}
			if(second != null){
				second.closeInventory();
			}
		}

		// if first player closed second player won and the other way around
		winner = !event.getPlayer().getUniqueId().equals(game.getFirstUUID());

		removeGame(getGame(event.getPlayer().getUniqueId()));
		if(!game.getState().equals(GameState.FINISHED)){
			if(!winner){
				// second won the game because first closed
			} else {
				// first won the game because second closed
			}

		} else {
			if(!winner){
				second.closeInventory();
			} else {
				first.closeInventory();
			}

		}
		return true;
	}

	@Override
	public boolean isInGame(UUID uuid) {
		for(Game game : games){
			if(isFirst(uuid, game) || isSecond(uuid, game)){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean startGame(Player[] players, boolean b, String... strings) {

		// TodO
		GameRules rule = gameTypes.get(strings[0]);
		if(rule == null){
			Bukkit.getLogger().log(Level.WARNING, "could not start a game");
			return false;
		}


		GameBox.debug("trying to start a game with: " + players.length + " players and the args " + Arrays.asList(strings));
		games.add(new Game(plugin, players[0].getUniqueId(), players[1].getUniqueId(), rule));
		return true;
	}

	@Override
	public void removeFromGame(UUID uuid) {

	}

	public void setGameTypes(Map<String,GameRules> gameTypes) {
		this.gameTypes = gameTypes;
	}
}
