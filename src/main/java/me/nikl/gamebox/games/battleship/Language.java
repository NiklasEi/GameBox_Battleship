package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.game.GameLanguage;


public class Language extends GameLanguage {
	public String GAME_PAYED, GAME_NOT_ENOUGH_MONEY, GAME_WON_MONEY, GAME_WON_MONEY_GAVE_UP, GAME_WON, GAME_LOSE, GAME_GAVE_UP, GAME_OTHER_GAVE_UP,
		GAME_TOO_SLOW, GAME_WON_MONEY_TOO_SLOW, GAME_WON_TOO_SLOW;
	public String TITLE_SET_SHIP_1, TITLE_SET_SHIP_2, TITLE_SET_SHIP_3, TITLE_SET_SHIP_4, TITLE_ATTACKER, TITLE_DEFENDER, TITLE_WON, TITLE_LOST, TITLE_WAITING;
	public String TITLE_CHANGING;
	
	public Language(Battleship game){
		super(game);
	}

	@Override
	protected void loadMessages() {
		getGameMessages();
		getInvTitles();
	}
	
	private void getInvTitles() {
		this.TITLE_SET_SHIP_1 = getString("inventoryTitles.setShip1");
		this.TITLE_SET_SHIP_2 = getString("inventoryTitles.setShip2");
		this.TITLE_SET_SHIP_3 = getString("inventoryTitles.setShip3");
		this.TITLE_SET_SHIP_4 = getString("inventoryTitles.setShip4");
		this.TITLE_ATTACKER = getString("inventoryTitles.attacker");
		this.TITLE_DEFENDER = getString("inventoryTitles.defender");
		this.TITLE_WAITING = getString("inventoryTitles.waiting");
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
}

