package me.nikl.battleship.game;

/**
 * Created by Niklas on 15.02.2017.
 */
public class GameRules {
    private double cost, reward;
    private boolean saveStats, changeGridAfterHit, switchGridsAfterFireTimerRanOut;
    private int aircraftCarrier, battleship, cruiser, destroyer;
    private String key;

    public GameRules(double cost, double reward, int aircraftCarrier, int battleship, int cruiser, int destroyer, boolean changeGridAfterHit, boolean switchGridsAfterFireTimerRanOut, String key, boolean saveStats){
        this.cost = cost;
        this.reward = reward;
        this.saveStats = saveStats;
        this.changeGridAfterHit = changeGridAfterHit;
        this.switchGridsAfterFireTimerRanOut = switchGridsAfterFireTimerRanOut;
        this.aircraftCarrier = aircraftCarrier;
        this.battleship = battleship;
        this.cruiser = cruiser;
        this.destroyer = destroyer;
        this.key = key;
    }

    public double getCost() {
        return cost;
    }

    public double getReward() {
        return reward;
    }

    public boolean isSaveStats() {
        return saveStats;
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

    public String getKey() {
        return key;
    }
}
