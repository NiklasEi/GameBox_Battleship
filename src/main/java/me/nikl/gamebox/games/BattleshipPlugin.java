package me.nikl.gamebox.games;

import me.nikl.gamebox.games.battleship.Battleship;
import me.nikl.gamebox.module.GameBoxModule;

public class BattleshipPlugin extends GameBoxModule {
    public static final String BATTLESHIP = "battleship";

    @Override
    public void onEnable() {
        registerGame(BATTLESHIP, Battleship.class, "bs");
    }

    @Override
    public void onDisable() {

    }
}
