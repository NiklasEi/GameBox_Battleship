package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameSettings;
import me.nikl.gamebox.games.BattleshipPlugin;

/**
 * @author Niklas Eicker
 */
public class Battleship extends me.nikl.gamebox.game.Game {
    public Battleship(GameBox gameBox) {
        super(gameBox, BattleshipPlugin.BATTLESHIP);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {

    }

    @Override
    public void loadSettings() {
        gameSettings.setGameType(GameSettings.GameType.TWO_PLAYER);
    }

    @Override
    public void loadLanguage() {
        gameLang = new Language(this);
    }

    @Override
    public void loadGameManager() {
        gameManager = new GameManager(this);
    }
}
