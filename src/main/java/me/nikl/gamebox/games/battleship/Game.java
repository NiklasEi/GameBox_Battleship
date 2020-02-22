package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.GameBoxSettings;
import me.nikl.gamebox.games.BattleshipPlugin;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.nms.NmsUtility;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

public class Game {
    public boolean ruleFireAgainAfterHit;
    private ItemStack ownShip, ownWater, ownMiss, ownHit, othersCover, othersMiss, othersHit, lockedShip;
    private Inventory firstOwn, firstOthers, secondOwn, secondOthers;
    private int numCarrier, numBattleship, numCruiser, numDestroyer;
    private GameState state;
    private GameManager manager;
    private UUID firstUUID, secondUUID;
    private Player first, second;
    private boolean firstSeesOwn, secondSeesOwn;
    private boolean firstShipsSet, secondShipsSet;
    private FileConfiguration config;
    private Language lang;
    private NmsUtility updater;
    private GameTimer timer;
    private int shipSetTime, fireTime, changeTime;
    private int currentTime;
    private String firstCurrentState, secondCurrentState;
    private boolean switchGridsAfterFireTimerRanOut;
    private Sound yourTurnNotice;
    private GameRules rule;
    private float volume = 0.5f, pitch = 1f;
    private Battleship battleship;

    public Game(Battleship battleship, UUID firstUUID, UUID secondUUID, GameRules rule) {
        this.ownShip = battleship.ownShip;
        this.ownHit = battleship.ownHit;
        this.ownMiss = battleship.ownMiss;
        this.ownWater = battleship.ownWater;
        this.othersCover = battleship.othersCover;
        this.othersHit = battleship.othersHit;
        this.othersMiss = battleship.othersMiss;
        this.lockedShip = battleship.lockedShip;
        this.yourTurnNotice = Sound.NOTE_PIANO;
        this.updater = NmsFactory.getNmsUtility();
        this.setState(GameState.BUILDING);
        this.battleship = battleship;
        this.lang = (Language) battleship.getGameLang();
        this.config = battleship.getConfig();
        this.rule = rule;
        getValuesFromConfig();
        this.manager = (GameManager) battleship.getGameManager();
        this.firstUUID = firstUUID;
        this.secondUUID = secondUUID;
        this.first = Bukkit.getPlayer(firstUUID);
        this.second = Bukkit.getPlayer(secondUUID);
        if (first == null && second == null) {
            manager.removeGame(this);
            return;
        }
        if (second == null) {
            first.sendMessage(StringUtility.color(lang.PREFIX + " &4The other player is offline"));
            manager.removeGame(this);
            return;
        } else if (first == null) {
            second.sendMessage(StringUtility.color(lang.PREFIX + " &4The other player is offline"));
            manager.removeGame(this);
            return;
        }
        getShipNumbers();
        currentTime = shipSetTime;
        this.firstCurrentState = lang.TITLE_SET_SHIP_1.replaceAll("%count%", numCarrier + "");
        this.secondCurrentState = lang.TITLE_SET_SHIP_1.replaceAll("%count%", numCarrier + "");

        String title = ChatColor.translateAlternateColorCodes('&', firstCurrentState.replaceAll("%timer%", shipSetTime + ""));
        if (GameBoxSettings.checkInventoryLength && title.length() > 32) {
            title = "Title is too long!";
        }
        this.firstOwn = battleship.createInventory(54, title);
        this.firstOthers = battleship.createInventory(54, "Battleship");

        title = ChatColor.translateAlternateColorCodes('&', secondCurrentState.replaceAll("%timer%", shipSetTime + ""));
        if (GameBoxSettings.checkInventoryLength && title.length() > 32) {
            title = "Title is too long!";
        }
        this.secondOwn = battleship.createInventory(54, title);
        this.secondOthers = battleship.createInventory(54, "Battleship");

        BuildGrids();

        setState(GameState.SETTING_SHIP1);
        this.timer = new GameTimer(this);
        playSound(yourTurnNotice, first, second);
        try {
            GameBox.openingNewGUI = true;
            showInventory(true, true);
            showInventory(false, true);
        } finally {
            GameBox.openingNewGUI = false;
        }
    }

    private void playSound(Sound yourTurnNotice, Player... players) {
        for (Player player : players) {
            battleship.playSound(player, yourTurnNotice, volume, pitch);
        }
    }

    private void getValuesFromConfig() {
        if (!config.isConfigurationSection("timers")) {
            Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4No 'timers' section or invalid values in 'timers' section"));
            Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4Using default values!"));
            this.shipSetTime = 30;
            this.fireTime = 10;
            this.changeTime = 3;
        } else {
            ConfigurationSection timer = config.getConfigurationSection("timers");
            if (!timer.isSet("shipSetTimer.countdown") || !timer.isInt("shipSetTimer.countdown")) {
                Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4Using default value for shipSetTimer"));
                this.shipSetTime = 30;
            } else {
                this.shipSetTime = timer.getInt("shipSetTimer.countdown");
            }
            if (!timer.isSet("fireTimer.countdown") || !timer.isInt("fireTimer.countdown")) {
                Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4Using default value for fireTimer!"));
                this.fireTime = 10;
            } else {
                this.fireTime = timer.getInt("fireTimer.countdown");
            }
            if (!timer.isSet("changingGrids.countdown") || !timer.isInt("changingGrids.countdown")) {
                Bukkit.getConsoleSender().sendMessage(chatColor(lang.PREFIX + " &4Using default value for changingGrids!"));
                this.changeTime = 3;
            } else {
                this.changeTime = timer.getInt("changingGrids.countdown");
            }
        }
        this.ruleFireAgainAfterHit = !rule.isChangeGridAfterHit();
        this.switchGridsAfterFireTimerRanOut = rule.isSwitchGridsAfterFireTimerRanOut();
    }


    GameTimer getTimer() {
        return this.timer;
    }


    void setFirstCurrentState(String firstCurrentState) {
        this.firstCurrentState = firstCurrentState;
    }


    void setSecondCurrentState(String secondCurrentState) {
        this.secondCurrentState = secondCurrentState;
    }

    /**
     * First argument defines the player
     * Second argument defines the inventory own/others
     *
     * Only use this function to show a new inv. so the game always knows who is seeing which inv.
     */
    void showInventory(boolean isFirst, boolean ownInv) {
        try {
            setClosingInv(true);
            if (isFirst) {
                setFirstSeesOwn(ownInv);
                if (ownInv) {
                    first.openInventory(firstOwn);
                } else {
                    first.openInventory(firstOthers);
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
            } else {
                setSecondSeesOwn(ownInv);
                if (ownInv) {
                    second.openInventory(secondOwn);
                } else {
                    second.openInventory(secondOthers);
                }
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
            }
        } finally {
            setClosingInv(false);
        }
    }

    private void BuildGrids() {
        for (int i = 0; i < 54; i++) {
            firstOwn.setItem(i, ownWater);
            firstOthers.setItem(i, othersCover);
            secondOwn.setItem(i, ownWater);
            secondOthers.setItem(i, othersCover);
        }
    }

    private void getShipNumbers() {
        this.numCarrier = rule.getAircraftCarrier();
        if (numCarrier < 1 || numCarrier > 2) {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Not valid number of carriers in config!");
            this.numCarrier = 1;
        }
        this.numBattleship = rule.getBattleship();
        if (numBattleship < 1 || numBattleship > 2) {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Not valid number of battleships in config!");
            this.numBattleship = 1;
        }
        this.numCruiser = rule.getCruiser();
        if (numCruiser < 1 || numCruiser > 2) {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Not valid number of cruisers in config!");
            this.numCruiser = 1;
        }
        this.numDestroyer = rule.getDestroyer();
        if (numDestroyer < 1 || numDestroyer > 2) {
            Bukkit.getConsoleSender().sendMessage(lang.PREFIX + " Not valid number of destroyers in config!");
            this.numDestroyer = 1;
        }
    }


    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
        switch (state) {
            case SETTING_SHIP2:
                this.timer.cancel();
                currentTime = shipSetTime;
                setShipsSet(true, false); // reset shipset info for both players
                setShipsSet(false, false);
                setFirstCurrentState(lang.TITLE_SET_SHIP_2.replaceAll("%count%", numBattleship + ""));
                setSecondCurrentState(lang.TITLE_SET_SHIP_2.replaceAll("%count%", numBattleship + ""));
                try {
                    GameBox.openingNewGUI = true;
                    showInventory(true, true);
                    showInventory(false, true);
                } finally {
                    GameBox.openingNewGUI = false;
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                this.timer = new GameTimer(this);
                playSound(yourTurnNotice, first, second);
                break;

            case SETTING_SHIP3:
                this.timer.cancel();
                currentTime = shipSetTime;
                setShipsSet(true, false); // reset shipset info for both players
                setShipsSet(false, false);
                setFirstCurrentState(lang.TITLE_SET_SHIP_3.replaceAll("%count%", numCruiser + ""));
                setSecondCurrentState(lang.TITLE_SET_SHIP_3.replaceAll("%count%", numCruiser + ""));
                try {
                    GameBox.openingNewGUI = true;
                    showInventory(true, true);
                    showInventory(false, true);
                } finally {
                    GameBox.openingNewGUI = false;
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                this.timer = new GameTimer(this);
                playSound(yourTurnNotice, first, second);
                break;

            case SETTING_SHIP4:
                this.timer.cancel();
                currentTime = shipSetTime;
                setShipsSet(true, false); // reset shipset info for both players
                setShipsSet(false, false);
                setFirstCurrentState(lang.TITLE_SET_SHIP_4.replaceAll("%count%", numDestroyer + ""));
                setSecondCurrentState(lang.TITLE_SET_SHIP_4.replaceAll("%count%", numDestroyer + ""));
                try {
                    GameBox.openingNewGUI = true;
                    showInventory(true, true);
                    showInventory(false, true);
                } finally {
                    GameBox.openingNewGUI = false;
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                this.timer = new GameTimer(this);
                playSound(yourTurnNotice, first, second);
                break;

            case FIRST_TURN:
                this.timer.cancel();
                currentTime = fireTime;
                setFirstCurrentState(lang.TITLE_ATTACKER);
                setSecondCurrentState(lang.TITLE_DEFENDER);
                try {
                    GameBox.openingNewGUI = true;
                    showInventory(true, false);
                    showInventory(false, true);
                } finally {
                    GameBox.openingNewGUI = false;
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                this.timer = new GameTimer(this);
                playSound(yourTurnNotice, first);
                break;

            case SECOND_TURN:
                this.timer.cancel();
                currentTime = fireTime;
                setFirstCurrentState(lang.TITLE_DEFENDER);
                setSecondCurrentState(lang.TITLE_ATTACKER);
                try {
                    GameBox.openingNewGUI = true;
                    showInventory(true, true);
                    showInventory(false, false);
                } finally {
                    GameBox.openingNewGUI = false;
                }
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                this.timer = new GameTimer(this);
                playSound(yourTurnNotice, second);
                break;

            case FINISHED:
                this.timer.cancel();
                break;

            case CHANGING:
                this.timer.cancel();
                currentTime = changeTime;
                setFirstCurrentState(lang.TITLE_CHANGING);
                setSecondCurrentState(lang.TITLE_CHANGING);
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                break;

            default:
                break;
        }
    }

    public UUID getFirstUUID() {
        return firstUUID;
    }

    public void setFirstUUID(UUID uuid) {
        this.firstUUID = uuid;
    }

    public UUID getSecondUUID() {
        return secondUUID;
    }

    public void setSecondUUID(UUID uuid) {
        this.secondUUID = uuid;
    }

    public void setState(String state, boolean isFirst, boolean own) {
        if (isFirst) {
            if (own) {
                this.firstOwn = setState(state, this.firstOwn);
            } else {
                this.firstOthers = setState(state, this.firstOthers);
            }
        } else {
            if (own) {
                this.secondOwn = setState(state, this.secondOwn);
            } else {
                this.secondOthers = setState(state, this.secondOthers);
            }
        }
    }

    public Inventory setState(String state, Inventory inv) {
        Inventory newInv = battleship.createInventory(54, ChatColor.translateAlternateColorCodes('&', "&2Please Wait"));
        try {
            newInv = battleship.createInventory(54, ChatColor.translateAlternateColorCodes('&', state));
        } catch (Exception e) {
        }
        newInv.setContents(inv.getContents());
        return newInv;
    }

    public boolean isWater(ItemStack currentItem) {
        return ownWater.getType().equals(currentItem.getType()) && ownWater.getData().equals(currentItem.getData());
    }

    public boolean isShip(ItemStack currentItem) {
        return ownShip.getType().equals(currentItem.getType()) && ownShip.getData().equals(currentItem.getData());
    }

    public void setShip(int slot, boolean isFirst) {
        if (isFirst) {
            firstOwn.setItem(slot, ownShip);
        } else {
            secondOwn.setItem(slot, ownShip);
        }
    }

    public void setWater(int slot, boolean isFirst) {
        if (isFirst) {
            firstOwn.setItem(slot, ownWater);
        } else {
            secondOwn.setItem(slot, ownWater);
        }
    }

//    public boolean isCurrentInventory(Inventory inventory) {
//        boolean isItOpen = false;
//        if (secondSeesOwn) {
//            isItOpen = inventoryEquals(secondOwn, inventory);
//        } else {
//            isItOpen = inventoryEquals(secondOthers, inventory);
//        }
//        if (isItOpen) return true;
//        if (firstSeesOwn) {
//            isItOpen = inventoryEquals(firstOwn, inventory);
//        } else {
//            isItOpen = inventoryEquals(firstOthers, inventory);
//        }
//        return isItOpen;
//    }

//    private boolean inventoryEquals(Inventory inventory1, Inventory inventory2) {
//        if (!inventory1.getType().equals(inventory2.getType())) return false;
//        if (!inventory1.getTitle().equals(inventory2.getTitle())) return false;
//        int slots = inventory1.getSize();
//        for (int slot = 0; slot < slots; slot++) {
//            if (!inventory1.getItem(slot).equals(inventory2.getItem(slot))) return false;
//        }
//        return true;
//    }

    public void setFirstSeesOwn(boolean firstSeesOwn) {
        this.firstSeesOwn = firstSeesOwn;
    }

    public void setSecondSeesOwn(boolean secondSeesOwn) {
        this.secondSeesOwn = secondSeesOwn;
    }

    boolean shipsSet(int i) {
        return (shipsSet(i, firstOwn) && shipsSet(i, secondOwn));
    }

    boolean shipsSet(int i, boolean isFirst) {
        if (isFirst) {
            return shipsSet(i, firstOwn);
        } else {
            return shipsSet(i, secondOwn);
        }
    }

    boolean shipsSet(int i, Inventory inv) {
        int count;
        int length;
        // get number of ships that must have been set
        if (i == 1) {
            count = numCarrier;
            length = 5;
        } else if (i == 2) {
            count = numBattleship;
            length = 4;
        } else if (i == 3) {
            count = numCruiser;
            length = 3;
        } else if (i == 4) {
            count = numDestroyer;
            length = 2;
        } else {
            return false;
        }

        return checkForShips(inv, count, length);
    }

    private boolean checkForShips(Inventory inv, int count, int length) {
        int ships[] = new int[count * length];
        int anzShipBlock = 0;
        for (int i = 0; i < count * length; i++) {
            ships[i] = -1;
        }
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                if (inv.getItem(slot).equals(ownShip)) {
                    if (anzShipBlock >= count * length) return false;
                    ships[anzShipBlock] = slot;
                    anzShipBlock++;
                }
            }
        }
        if (anzShipBlock != count * length) return false;
        // Check whether every single ship-block has length direct ship-block neighboors
        for (int slot : ships) {
            int row = slot / 9;
            int column = slot % 9;
            int anzLeftRight = 0, anzUpDown = 0;
            int rightAnz = 0, leftAnz = 0;
            int upAnz = 0, downAnz = 0;
            boolean consRight = true, consLeft = true;
            boolean consUp = true, consDown = true;
            while (column + rightAnz + 1 < 9 && consRight) {
                if (isIn(ships, slot + rightAnz + 1)) {
                    rightAnz++;
                } else {
                    consRight = false;
                }
            }
            while (column - leftAnz - 1 >= 0 && consLeft) {
                if (isIn(ships, slot - leftAnz - 1)) {
                    leftAnz++;
                } else {
                    consLeft = false;
                }
            }
            while (row + upAnz + 1 < 6 && consUp) {
                if (isIn(ships, slot + upAnz * 9 + 9)) {
                    upAnz++;
                } else {
                    consUp = false;
                }
            }
            while (row - downAnz - 1 >= 0 && consDown) {
                if (isIn(ships, slot - downAnz * 9 - 9)) {
                    downAnz++;
                } else {
                    consDown = false;
                }
            }
            anzLeftRight = rightAnz + leftAnz;
            anzUpDown = upAnz + downAnz;
            if (anzLeftRight < length - 1 && anzUpDown < length - 1) return false;
        }
        return true;
    }

    private boolean isIn(int[] array, int search) {
        for (int slot : array) {
            if (slot == search) {
                return true;
            }
        }
        return false;
    }

    public void lockShips(boolean isFirst) {
        if (isFirst) {
            for (int slot = 0; slot < 54; slot++) {
                if (firstOwn.getItem(slot).equals(ownShip)) {
                    firstOwn.setItem(slot, lockedShip);
                }
            }
        } else {
            for (int slot = 0; slot < 54; slot++) {
                if (secondOwn.getItem(slot).equals(ownShip)) {
                    secondOwn.setItem(slot, lockedShip);
                }
            }
        }
    }

    public void lockShips() {
        for (int slot = 0; slot < 54; slot++) {
            if (firstOwn.getItem(slot).equals(ownShip)) {
                firstOwn.setItem(slot, lockedShip);
            }
            if (secondOwn.getItem(slot).equals(ownShip)) {
                secondOwn.setItem(slot, lockedShip);
            }
        }
    }

    public void unLockShips() {
        for (int slot = 0; slot < 54; slot++) {
            if (firstOwn.getItem(slot).equals(lockedShip)) {
                firstOwn.setItem(slot, ownShip);
            }
            if (secondOwn.getItem(slot).equals(lockedShip)) {
                secondOwn.setItem(slot, ownShip);
            }
        }
    }

    public void readyToStart() {
        this.timer.cancel();
        firstOwn = setState(lang.TITLE_DEFENDER.replaceAll("%timer%", fireTime + ""), firstOwn);
        secondOwn = setState(lang.TITLE_DEFENDER.replaceAll("%timer%", fireTime + ""), secondOwn);
        firstOthers = setState(lang.TITLE_ATTACKER.replaceAll("%timer%", fireTime + ""), firstOthers);
        secondOthers = setState(lang.TITLE_ATTACKER.replaceAll("%timer%", fireTime + ""), secondOthers);
    }

    public boolean isCover(ItemStack currentItem) {
        return othersCover.getType().equals(currentItem.getType()) && othersCover.getData().equals(currentItem.getData());
    }

    public boolean isWon(boolean isFirst) {
        Inventory inv;
        if (isFirst) {
            inv = secondOwn;
        } else {
            inv = firstOwn;
        }
        for (int slot = 0; slot < firstOwn.getSize(); slot++) {
            if (inv.getItem(slot).equals(ownShip)) {
                return false;
            }
        }
        return true;
    }

    public boolean fire(boolean isFirst, int slot) {
        if (isFirst) {
            if (secondOwn.getItem(slot).equals(ownShip)) {
                firstOthers.setItem(slot, othersHit);
                secondOwn.setItem(slot, ownHit);
                this.timer.cancel();
                this.timer = new GameTimer(this);
                currentTime = fireTime;
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                return true;
            } else {
                firstOthers.setItem(slot, othersMiss);
                secondOwn.setItem(slot, ownMiss);
                return false;
            }
        } else {
            if (firstOwn.getItem(slot).equals(ownShip)) {
                secondOthers.setItem(slot, othersHit);
                firstOwn.setItem(slot, ownHit);
                this.timer.cancel();
                this.timer = new GameTimer(this);
                currentTime = fireTime;
                updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", currentTime + "")));
                updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", currentTime + "")));
                return true;
            } else {
                secondOthers.setItem(slot, othersMiss);
                firstOwn.setItem(slot, ownMiss);
                return false;
            }
        }

    }

    public void setClosingInv(boolean closingInv) {
        GameBox.openingNewGUI = closingInv;
    }

    public void won(boolean isFirst) {
        Player loser;
        Player winner;
        if (isFirst) {
            winner = Bukkit.getPlayer(this.getFirstUUID());
            loser = Bukkit.getPlayer(this.getSecondUUID());
            updater.updateInventoryTitle(first, chatColor(lang.TITLE_WON));
            updater.updateInventoryTitle(second, chatColor(lang.TITLE_LOST));
        } else {
            loser = Bukkit.getPlayer(this.getFirstUUID());
            winner = Bukkit.getPlayer(this.getSecondUUID());
            updater.updateInventoryTitle(first, chatColor(lang.TITLE_LOST));
            updater.updateInventoryTitle(second, chatColor(lang.TITLE_WON));
        }
        if (battleship.getSettings().isEconEnabled()) {
            winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY.replaceAll("%reward%", rule.getMoneyToWin() + "")));
        } else {
            winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON));
        }
        loser.sendMessage(chatColor(lang.PREFIX + lang.GAME_LOSE));
        manager.onGameEnd(winner, loser, rule.getKey());
    }

    public void setShipsSet(boolean isFirst, boolean b) {
        if (isFirst) {
            this.firstShipsSet = b;
        } else {
            this.secondShipsSet = b;
        }
    }

    public boolean getShipsSet(boolean isFirst, boolean b) {
        if (isFirst) {
            return this.firstShipsSet;
        } else {
            return this.secondShipsSet;
        }
    }

    public int getShipSetTime() {
        return shipSetTime;
    }

    public void setShipSetState(int time) {
        currentTime = time;
        updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", time + "")));
        updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", time + "")));
    }

    public void forceNextState() {
        if (!firstShipsSet) {
            forceSetShips(true);
        }
        if (!secondShipsSet) {
            forceSetShips(false);
        }
        lockShips();
        if (this.getState().equals(GameState.SETTING_SHIP1)) {
            this.setState(GameState.SETTING_SHIP2);
        } else if (this.getState().equals(GameState.SETTING_SHIP2)) {
            this.setState(GameState.SETTING_SHIP3);
        } else if (this.getState().equals(GameState.SETTING_SHIP3)) {
            this.setState(GameState.SETTING_SHIP4);
        } else if (this.getState().equals(GameState.SETTING_SHIP4)) {
            unLockShips();
            readyToStart();
            this.setState(GameState.FIRST_TURN);
        }
    }

    private void forceSetShips(boolean isFirst) {
        if (isFirst) {
            for (int slot = 0; slot < 54; slot++) {
                if (firstOwn.getItem(slot).equals(ownShip)) {
                    firstOwn.setItem(slot, ownWater);
                }
            }
            firstOwn = setShips(firstOwn);
            firstShipsSet = true;
        } else {
            for (int slot = 0; slot < 54; slot++) {
                if (secondOwn.getItem(slot).equals(ownShip)) {
                    secondOwn.setItem(slot, ownWater);
                }
            }
            secondOwn = setShips(secondOwn);
            secondShipsSet = true;
        }
    }

    /*
     * Set the ships when the timer ran out
     */
    private Inventory setShips(Inventory inv) {
        int count;
        int length;
        // get number of ships that must have been set
        if (this.getState().equals(GameState.SETTING_SHIP1)) {
            count = numCarrier;
            length = 5;
        } else if (this.getState().equals(GameState.SETTING_SHIP2)) {
            count = numBattleship;
            length = 4;
        } else if (this.getState().equals(GameState.SETTING_SHIP3)) {
            count = numCruiser;
            length = 3;
        } else {
            count = numDestroyer;
            length = 2;
        }
        for (int row = 0; row < 9 - length + 1; row++) {
            for (int column = 0; column < 6 - length + 1; column++) {

            }
        }
        Random random = new Random();
        int shipsSet = 0;
        int slot, row, column, downAnz, rightAnz;
        while (shipsSet < count) {
            slot = random.nextInt(54);
            row = slot / 9;
            column = slot % 9;
            if (!(row < 6 - length + 1)) continue; // ship wont fit here
            if (!(column < 9 - length + 1)) continue; // ship wont fit here
            downAnz = 0;
            rightAnz = 0;
            boolean consDown = true, consRight = true;
            if (random.nextDouble() < 0.5) {
                while (consDown && downAnz < length) {
                    if (inv.getItem(slot + downAnz * 9).equals(ownWater)) {
                        downAnz++;
                    } else {
                        consDown = false;
                    }
                }
                if (consDown) {
                    for (int i = 0; i < length; i++) {
                        inv.setItem(slot + i * 9, lockedShip);
                    }
                    shipsSet++;
                    continue;
                }
                while (column + rightAnz + 1 < 9 && consRight) {
                    if (inv.getItem(slot + rightAnz).equals(ownWater)) {
                        rightAnz++;
                    } else {
                        consRight = false;
                    }
                }
                if (consRight) {
                    for (int i = 0; i < length; i++) {
                        inv.setItem(slot + i, lockedShip);
                    }
                    shipsSet++;
                    continue;
                }

            } else {
                while (column + rightAnz + 1 < 9 && consRight) {
                    if (inv.getItem(slot + rightAnz).equals(ownWater)) {
                        rightAnz++;
                    } else {
                        consRight = false;
                    }
                }
                if (consRight) {
                    for (int i = 0; i < length; i++) {
                        inv.setItem(slot + i, lockedShip);
                    }
                    shipsSet++;
                    continue;
                }
                while (consDown && downAnz < length) {
                    if (inv.getItem(slot + downAnz * 9).equals(ownWater)) {
                        downAnz++;
                    } else {
                        consDown = false;
                    }
                }
                if (consDown) {
                    for (int i = 0; i < length; i++) {
                        inv.setItem(slot + i * 9, lockedShip);
                    }
                    shipsSet++;
                    continue;
                }
            }
        }
        return inv;
    }

    public void cancelTimer() {
        this.timer.cancel();
    }

    public int getFireTime() {
        return fireTime;
    }

    public void fireTimeRanOut() {
        boolean isFirst;
        isFirst = this.state == GameState.FIRST_TURN;

        if (switchGridsAfterFireTimerRanOut) {
            this.changeAttacker(!isFirst);
            return;
        }
        Player loser = isFirst ? Bukkit.getPlayer(this.getFirstUUID()) : Bukkit.getPlayer(this.getSecondUUID());
        Player winner = !isFirst ? Bukkit.getPlayer(this.getFirstUUID()) : Bukkit.getPlayer(this.getSecondUUID());
        cancelTimer();
        if (loser == null || winner == null) return;
        playSound(Sound.VILLAGER_NO, loser);
        playSound(Sound.LEVEL_UP, winner);
        if (!getState().equals(GameState.FINISHED)) {
            this.setState(GameState.FINISHED);
            if (battleship.getSettings().isEconEnabled()) {
                if (!Permission.BYPASS_GAME.hasPermission(winner, BattleshipPlugin.BATTLESHIP)) {
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY_TOO_SLOW.replaceAll("%reward%", rule.getMoneyToWin() + "").replaceAll("%loser%", loser.getName())));
                } else {
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_TOO_SLOW.replaceAll("%loser%", loser.getName())));
                }
            } else {
                winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_TOO_SLOW.replaceAll("%loser%", loser.getName())));
            }
            loser.sendMessage(chatColor(lang.PREFIX + lang.GAME_TOO_SLOW));
        }
        NmsFactory.getNmsUtility().updateInventoryTitle(winner, lang.TITLE_WON);
        NmsFactory.getNmsUtility().updateInventoryTitle(loser, lang.TITLE_LOST);
        manager.onGameEnd(winner, loser, rule.getKey());
    }

    private String chatColor(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public void setFireState(int time) {
        if (state.equals(GameState.FIRST_TURN)) {
            currentTime = time;
            updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", time + "")));
            updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", time + "")));
        } else if (state.equals(GameState.SECOND_TURN)) {
            currentTime = time;
            updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", time + "")));
            updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", time + "")));
        }
    }

    public int getChangeTime() {
        return this.changeTime;
    }

    public void changeAttacker(boolean newAttacker) {
        this.cancelTimer();
        this.timer = new GameTimer(this, newAttacker);
    }

    public void setChangingState(int time) {
        if (firstSeesOwn) {
            currentTime = time;
            updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", time + "")));
            updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", time + "")));
        } else {
            currentTime = time;
            updater.updateInventoryTitle(first, chatColor(firstCurrentState.replaceAll("%timer%", time + "")));
            updater.updateInventoryTitle(second, chatColor(secondCurrentState.replaceAll("%timer%", time + "")));
        }
    }

    public GameRules getRule() {
        return rule;
    }

    public Player getFirst() {
        return this.first;
    }

    public void setFirst(Player first) {
        this.first = first;
    }

    public Player getSecond() {
        return this.second;
    }

    public void setSecond(Player second) {
        this.second = second;
    }

    public void updateTitle(boolean isFirst) {
        Player player = isFirst ? first : second;
        updater.updateInventoryTitle(player, chatColor(isFirst ? firstCurrentState : secondCurrentState));
    }
}
