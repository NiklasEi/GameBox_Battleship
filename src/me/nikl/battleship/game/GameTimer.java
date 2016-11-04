package me.nikl.battleship.game;

import org.bukkit.scheduler.BukkitRunnable;

import me.nikl.battleship.Main;

public class GameTimer extends BukkitRunnable{
	
	private Game game;
	private int time;
	private String state;
	private boolean newAttacker;
	
	GameTimer(Game game){
		this.game = game;	
		
		if(game.getState().equals(GameState.SETTING_SHIP1) || game.getState().equals(GameState.SETTING_SHIP2) || game.getState().equals(GameState.SETTING_SHIP3) || game.getState().equals(GameState.SETTING_SHIP4)){
			this.state = "ships";
			this.time = game.getShipSetTime();
		} else if(game.getState().equals(GameState.SECOND_TURN) || game.getState().equals(GameState.FIRST_TURN)){
			this.state = "fire";
			this.time = game.getFireTime();
		} else {
			return;
		}
		
		this.runTaskTimer(Main.getPlugin(Main.class), 20, 20);
	}
	GameTimer(Game game, boolean newAttacker){
		this.game = game;	
		this.newAttacker = newAttacker;
		this.state = "changing";
		this.time = game.getChangeTime();
		game.setState(GameState.CHANGING);
		
		this.runTaskTimer(Main.getPlugin(Main.class), 20, 20);	
	}
	

	@Override
	public void run() {
		if(!myRun()){
			if(state.equals("ships")){
				game.forceNextState();
				this.cancel();
			} else if(state.equals("fire")){
				game.fireTimeRanOut();
				this.cancel();
			} else if(state.equals("changing")){
				if(newAttacker){
					game.setState(GameState.FIRST_TURN);
				} else {
					game.setState(GameState.SECOND_TURN);
				}
				this.cancel();
			} else {
				this.cancel();
			}
		}
	}

	private boolean myRun() {
		if(time > 1){
			time--;
			if(state.equals("ships")){
				game.setShipSetState(time);
			} else if(state.equals("fire")){
				game.setFireState(time);
			} else if(state.equals("changing")){
				game.setChangingState(time);
			}
			return true;
		} else {
			return false;
		}
	}

}
