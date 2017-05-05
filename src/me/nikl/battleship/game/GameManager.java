package me.nikl.battleship.game;

import java.util.*;

import me.nikl.battleship.Sounds;
import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Permissions;
import me.nikl.gamebox.data.SaveType;
import me.nikl.gamebox.game.IGameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.nikl.battleship.Language;
import me.nikl.battleship.Main;

public class GameManager implements IGameManager{

	private Main plugin;
	private Set<Game> games;
	private Language lang;

	private Map<String, GameRules> gameTypes = new HashMap<>();
	
	// sounds
	private Sound ownMissSound, othersMissSound, ownHitSound, othersHitSound, setShipSound, unSetShipSound, won, lost;

	private float volume = 0.5f, pitch = 1f;

	public GameManager(Main plugin){
		this.plugin = plugin;
		this.games = new HashSet<>();
		this.lang = plugin.lang;
		
		
		this.ownMissSound = Sounds.SPLASH2.bukkitSound();
		this.othersMissSound = Sounds.SPLASH2.bukkitSound();
		
		this.ownHitSound = Sounds.ANVIL_LAND.bukkitSound();
		this.othersHitSound = Sounds.HURT_FLESH.bukkitSound();
		
		this.setShipSound = Sounds.ANVIL_LAND.bukkitSound();
		this.unSetShipSound = Sounds.CLICK.bukkitSound();
		
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

	private boolean isIngame(UUID uuid, Game game) {
		if(isFirst(uuid, game) || isSecond(uuid, game)){
			return true;
		}		
		return false;
	}
	
	public boolean isFirst(UUID uuid, Game game){
		if(game.getFirstUUID() != null && game.getFirstUUID().equals(uuid)){
			return true;
		}
		return false;
	}
	
	public boolean isSecond(UUID uuid, Game game){
		if(game.getSecondUUID() != null && game.getSecondUUID().equals(uuid)){
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
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, this.volume, pitch);
				} else if(game.isShip(event.getCurrentItem())){
					//player.sendMessage("clicked something else"); // XXX
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, volume, pitch);
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
					game.updateTitle(isFirst);
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
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, volume, pitch);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, volume, pitch);
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
					game.updateTitle(isFirst);
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
					if(Main.playSounds)	player.playSound(player.getLocation(), setShipSound, volume, pitch);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)	player.playSound(player.getLocation(), unSetShipSound, volume, pitch);
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
					game.updateTitle(isFirst);
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
					if(Main.playSounds)player.playSound(player.getLocation(), setShipSound, volume, pitch);
				} else if(game.isShip(event.getCurrentItem())){
					game.setWater(slot, isFirst);
					if(Main.playSounds)player.playSound(player.getLocation(), unSetShipSound, volume, pitch);
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
					game.updateTitle(isFirst);
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
							player.playSound(player.getLocation(), ownMissSound, volume, pitch);
							Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
							secondPlayer.playSound(secondPlayer.getLocation(), othersMissSound, volume, pitch);
						}
						game.changeAttacker(false);
						//game.setState(GameState.SECOND_TURN);
					} else {
						if(game.isWon(isFirst)){
							game.setState(GameState.FINISHED);
							game.won(isFirst);
							if(Main.playSounds) {
								player.playSound(player.getLocation(), won, volume, pitch);
								Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
								secondPlayer.playSound(secondPlayer.getLocation(), lost, volume, pitch);
							}
						} else {
							if(Main.playSounds) {
								player.playSound(player.getLocation(), ownHitSound, volume, pitch);
								Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
								secondPlayer.playSound(secondPlayer.getLocation(), othersHitSound, volume, pitch);
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
							player.playSound(player.getLocation(), ownMissSound, volume, pitch);
							Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
							firstPlayer.playSound(firstPlayer.getLocation(), othersMissSound, volume, pitch);
						}
						//game.setState(GameState.FIRST_TURN);
					} else {
						if(game.isWon(isFirst)){
							game.setState(GameState.FINISHED);
							game.won(isFirst);
							if(Main.playSounds) {
								player.playSound(player.getLocation(), won, volume, pitch);
								Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
								firstPlayer.playSound(firstPlayer.getLocation(), lost, volume, pitch);
							}
						} else {
							if(Main.playSounds) {
								player.playSound(player.getLocation(), ownHitSound, volume, pitch);
								Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
								firstPlayer.playSound(firstPlayer.getLocation(), othersHitSound, volume, pitch);
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
		if(!isInGame(event.getPlayer().getUniqueId())){
			return false;
		}
		if(getGame(event.getPlayer().getUniqueId()).getClosingInv()){
			return false;
		}
		Game game = getGame(event.getPlayer().getUniqueId());
		game.cancelTimer();
		boolean firstClosed = event.getPlayer().getUniqueId().equals(game.getFirstUUID());
		Player winner = firstClosed?game.getSecond():game.getFirst();
		Player loser = firstClosed?game.getFirst():game.getSecond();
		if((!firstClosed && game.getFirst() == null) || (firstClosed && game.getSecond() == null)){
			removeGame(game);
			return true;
		}

		// make sure the player is not counted as in game anymore
		if(firstClosed){
			game.setFirst(null);
			game.setFirstUUID(null);
		} else {
			game.setSecond(null);
			game.setSecondUUID(null);
		}

		if(game.getState() != GameState.FINISHED) {
			if(game.getState() == GameState.CHANGING || game.getState() == GameState.FIRST_TURN || game.getState() == GameState.SECOND_TURN ){
				// game started...
				// pay out the winner
				// otherwise pay the player that did not close the game, the money back they payed to play



				if(plugin.getEconEnabled()){
					if(!winner.hasPermission(Permissions.BYPASS_ALL.getPermission()) && !winner.hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID))){
						Main.econ.depositPlayer(winner, game.getRule().getReward());
						winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getReward()+"").replaceAll("%loser%", loser.getName())));
					} else {
						winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
					}
				} else {
					winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON.replaceAll("%loser%", loser.getName())));
				}
				loser.sendMessage(chatColor(lang.PREFIX + lang.GAME_GAVE_UP));


			}
			plugin.getUpdater().updateInventoryTitle(winner, lang.TITLE_WON);
			game.setState(GameState.FINISHED);


			onGameEnd(winner, loser, game.getRule().getKey());
		}


		return true;
	}

	public void addWin(UUID uuid, String key){
		plugin.getGameBox().getStatistics().addStatistics(uuid, Main.gameID, key, 1., SaveType.WINS);
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
	public int startGame(Player[] player, boolean b, String... strings) {

		GameRules rule = gameTypes.get(strings[0]);
		if(rule == null){
			return GameBox.GAME_NOT_STARTED_ERROR;
		}

		double cost = rule.getCost();

		boolean firstCanPay = true;

		if (plugin.getEconEnabled() && !player[0].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player[0].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
			if (Main.econ.getBalance(player[0]) >= cost) {

			} else {
				player[0].sendMessage(plugin.chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
				firstCanPay = false;
			}
		}


		if (plugin.getEconEnabled() && !player[1].hasPermission(Permissions.BYPASS_ALL.getPermission()) && !player[1].hasPermission(Permissions.BYPASS_GAME.getPermission(Main.gameID)) && cost > 0.0) {
			if (Main.econ.getBalance(player[1]) >= cost) {

			} else {
				player[1].sendMessage(plugin.chatColor(lang.PREFIX + plugin.lang.GAME_NOT_ENOUGH_MONEY));
				if(firstCanPay){
					// only second player cannot pay
					return GameBox.GAME_NOT_ENOUGH_MONEY_2;
				} else {
					// both players cannot pay
					return GameBox.GAME_NOT_ENOUGH_MONEY;
				}
			}
		}

		if(!firstCanPay){
			// only first player cannot pay
			return GameBox.GAME_NOT_ENOUGH_MONEY_1;
		}

		// both players can pay!


		if (plugin.getEconEnabled()) {
			Main.econ.withdrawPlayer(player[0], cost);
			player[0].sendMessage(plugin.chatColor(lang.PREFIX + plugin.lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));


			Main.econ.withdrawPlayer(player[1], cost);
			player[1].sendMessage(plugin.chatColor(lang.PREFIX + plugin.lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
		}

		games.add(new Game(plugin, player[0].getUniqueId(), player[1].getUniqueId(), rule));
		return GameBox.GAME_STARTED;
	}

	@Override
	public void removeFromGame(UUID uuid) {
		Game game = getGame(uuid);
		game.cancelTimer();
		Player first = game.getFirst();
		Player second = game.getSecond();
		boolean firstClosed = uuid.equals(game.getFirstUUID());
		if((!firstClosed && first == null) || (firstClosed && second == null)){
			removeGame(game);
			return;
		}

		// make sure the player is not counted as in game anymore
		if(firstClosed){
			game.setFirst(null);
			game.setFirstUUID(null);
		} else {
			game.setSecond(null);
			game.setSecondUUID(null);
		}

		game.setState(GameState.FINISHED);
		plugin.getUpdater().updateInventoryTitle(firstClosed?second:first, lang.TITLE_WON);

		if(game.getRule().isSaveStats()){
			addWin(firstClosed?game.getSecondUUID():game.getFirstUUID(), game.getRule().getKey());
		}
		return;
	}

	public void setGameTypes(Map<String,GameRules> gameTypes) {
		this.gameTypes = gameTypes;
	}

	public void onGameEnd(Player winner, Player loser, String key) {

		GameRules rule = gameTypes.get(key);

		if(rule.isSaveStats()){
			addWin(winner.getUniqueId(), rule.getKey());
		}
		if(rule.getTokens() > 0){
			plugin.getGameBox().wonTokens(winner.getUniqueId(), rule.getTokens(), Main.gameID);
		}
	}
}
