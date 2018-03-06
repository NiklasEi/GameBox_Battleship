package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.rules.GameRuleRewards;

/**
 * Created by Niklas on 15.02.2017.
 */
public class GameRules extends GameRuleRewards {
    private boolean changeGridAfterHit, switchGridsAfterFireTimerRanOut;
    private int aircraftCarrier, battleship, cruiser, destroyer;

    public GameRules(double cost, double reward, int tokens, int aircraftCarrier, int battleship, int cruiser, int destroyer, boolean changeGridAfterHit, boolean switchGridsAfterFireTimerRanOut, String key, boolean saveStats){
        super(key, saveStats, SaveType.WINS, cost, reward, tokens);
        this.changeGridAfterHit = changeGridAfterHit;
        this.switchGridsAfterFireTimerRanOut = switchGridsAfterFireTimerRanOut;
        this.aircraftCarrier = aircraftCarrier;
        this.battleship = battleship;
        this.cruiser = cruiser;
        this.destroyer = destroyer;
    }

    public boolean isChangeGridAfterHit() {
        return changeGridAfterHit;
    }

    public boolean isSwitchGridsAfterFireTimerRanOut() {
        return switchGridsAfterFireTimerRanOut;
    }

    public int getAircraftCarrier() {
        return aircraftCarrier;
    }

    public int getBattleship() {
        return battleship;
    }

    public int getCruiser() {
        return cruiser;
    }

    public int getDestroyer() {
        return destroyer;
    }
}
