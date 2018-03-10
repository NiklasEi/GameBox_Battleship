package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.data.toplist.SaveType;
import me.nikl.gamebox.game.exceptions.GameStartException;
import me.nikl.gamebox.game.manager.EasyManager;
import me.nikl.gamebox.game.rules.GameRule;
import me.nikl.gamebox.games.BattleshipPlugin;
import me.nikl.gamebox.nms.NmsFactory;
import me.nikl.gamebox.utility.Permission;
import me.nikl.gamebox.utility.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameManager extends EasyManager {
    private Battleship game;
    private Set<Game> games;
    private Language lang;
    private Map<String, GameRules> gameTypes = new HashMap<>();
    private Sound ownMissSound;
    private Sound othersMissSound;
    private Sound ownHitSound;
    private Sound othersHitSound;
    private Sound setShipSound;
    private Sound unSetShipSound;
    private Sound won;
    private Sound lost;
    private float volume = 0.5f, pitch = 1f;

    public GameManager(Battleship game) {
        this.game = game;
        this.games = new HashSet<>();
        this.lang = (Language) game.getGameLang();
        this.ownMissSound = Sound.SPLASH2;
        this.othersMissSound = Sound.SPLASH2;
        this.ownHitSound = Sound.ANVIL_LAND;
        this.othersHitSound = Sound.HURT_FLESH;
        this.setShipSound = Sound.ANVIL_LAND;
        this.unSetShipSound = Sound.CLICK;
        this.won = Sound.LEVEL_UP;
        this.lost = Sound.VILLAGER_NO;
    }


    private Game getGame(UUID uuid) {
        for (Iterator<Game> gameI = games.iterator(); gameI.hasNext(); ) {
            Game game = gameI.next();
            if (isIngame(uuid, game)) {
                return game;
            }
        }
        return null;
    }

    private boolean isIngame(UUID uuid, Game game) {
        if (isFirst(uuid, game) || isSecond(uuid, game)) {
            return true;
        }
        return false;
    }

    public boolean isFirst(UUID uuid, Game game) {
        if (game.getFirstUUID() != null && game.getFirstUUID().equals(uuid)) {
            return true;
        }
        return false;
    }

    public boolean isSecond(UUID uuid, Game game) {
        if (game.getSecondUUID() != null && game.getSecondUUID().equals(uuid)) {
            return true;
        }
        return false;
    }

    public void removeGame(Game game) {
        games.remove(game);
    }

    String chatColor(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getSlot() >= event.getInventory().getSize() || event.getSlot() < 0) return;
        if (!event.getAction().equals(InventoryAction.PICKUP_ALL) && !event.getAction().equals(InventoryAction.PICKUP_HALF)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Game game = getGame(player.getUniqueId());
        if (!getGame(player.getUniqueId()).isCurrentInventory(event.getInventory())) {
            //Bukkit.getConsoleSender().sendMessage("not current inv."); // XXX
            return;
        }
        boolean isFirst = isFirst(player.getUniqueId(), game);
        int slot = event.getSlot();
        switch (game.getState()) {
            case SETTING_SHIP1:
                if (game.getShipsSet(isFirst, true)) {
                    return;
                }
                if (game.isWater(event.getCurrentItem())) {
                    game.setShip(slot, isFirst);
                    this.game.playSound(player, setShipSound, this.volume, pitch);
                } else if (game.isShip(event.getCurrentItem())) {
                    game.setWater(slot, isFirst);
                    this.game.playSound(player, unSetShipSound, volume, pitch);
                }
                if (game.shipsSet(1, isFirst)) {
                    game.lockShips(isFirst);
                    game.setShipsSet(isFirst, true);
                    if (isFirst) {
                        game.setFirstCurrentState(lang.TITLE_WAITING);
                    } else {
                        game.setSecondCurrentState(lang.TITLE_WAITING);
                    }
                    game.setState(lang.TITLE_WAITING, isFirst, true);
                    game.updateTitle(isFirst);
                    if (game.getShipsSet(!isFirst, true)) {
                        game.setState(GameState.SETTING_SHIP2);
                    }
                }
                return;

            case SETTING_SHIP2:
                if (game.getShipsSet(isFirst, true)) {
                    return;
                }
                if (game.isWater(event.getCurrentItem())) {
                    game.setShip(slot, isFirst);
                    this.game.playSound(player, setShipSound, volume, pitch);
                } else if (game.isShip(event.getCurrentItem())) {
                    game.setWater(slot, isFirst);
                    this.game.playSound(player, unSetShipSound, volume, pitch);
                }
                if (game.shipsSet(2, isFirst)) {
                    game.lockShips(isFirst);
                    game.setShipsSet(isFirst, true);
                    if (isFirst) {
                        game.setFirstCurrentState(lang.TITLE_WAITING);
                    } else {
                        game.setSecondCurrentState(lang.TITLE_WAITING);
                    }
                    game.setState(lang.TITLE_WAITING, isFirst, true);
                    game.updateTitle(isFirst);
                    if (game.getShipsSet(!isFirst, true)) {
                        game.setState(GameState.SETTING_SHIP3);
                    }
                }
                return;

            case SETTING_SHIP3:
                if (game.getShipsSet(isFirst, true)) {
                    return;
                }
                if (game.isWater(event.getCurrentItem())) {
                    game.setShip(slot, isFirst);
                    this.game.playSound(player, setShipSound, volume, pitch);
                } else if (game.isShip(event.getCurrentItem())) {
                    game.setWater(slot, isFirst);
                    this.game.playSound(player, unSetShipSound, volume, pitch);
                }
                if (game.shipsSet(3, isFirst)) {
                    game.lockShips(isFirst);
                    game.setShipsSet(isFirst, true);
                    if (isFirst) {
                        game.setFirstCurrentState(lang.TITLE_WAITING);
                    } else {
                        game.setSecondCurrentState(lang.TITLE_WAITING);
                    }
                    game.setState(lang.TITLE_WAITING, isFirst, true);
                    game.updateTitle(isFirst);
                    if (game.getShipsSet(!isFirst, true)) {
                        game.setState(GameState.SETTING_SHIP4);
                    }
                }
                return;

            case SETTING_SHIP4:
                if (game.getShipsSet(isFirst, true)) {
                    return;
                }
                if (game.isWater(event.getCurrentItem())) {
                    game.setShip(slot, isFirst);
                    this.game.playSound(player, setShipSound, volume, pitch);
                } else if (game.isShip(event.getCurrentItem())) {
                    game.setWater(slot, isFirst);
                    this.game.playSound(player, unSetShipSound, volume, pitch);
                }
                if (game.shipsSet(4, isFirst)) {
                    game.lockShips(isFirst);
                    game.setShipsSet(isFirst, true);
                    if (isFirst) {
                        game.setFirstCurrentState(lang.TITLE_WAITING);
                    } else {
                        game.setSecondCurrentState(lang.TITLE_WAITING);
                    }
                    game.setState(lang.TITLE_WAITING, isFirst, true);
                    game.updateTitle(isFirst);
                    if (game.getShipsSet(!isFirst, true)) {
                        // if this is true, all ships are set and the game can start
                        game.unLockShips();
                        game.readyToStart();
                        game.setState(GameState.FIRST_TURN);
                        return;
                    }
                }
                return;

            case BUILDING:
                return;

            case FINISHED:
                return;

            case FIRST_TURN:
                if (!isFirst) return;
                if (game.isCover(event.getCurrentItem())) {
                    if (!game.fire(isFirst, slot)) {
                        this.game.playSound(player, ownMissSound, volume, pitch);
                        Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
                        this.game.playSound(secondPlayer, othersMissSound, volume, pitch);

                        game.changeAttacker(false);
                    } else {
                        if (game.isWon(isFirst)) {
                            game.setState(GameState.FINISHED);
                            game.won(isFirst);
                            this.game.playSound(player, won, volume, pitch);
                            Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
                            this.game.playSound(secondPlayer, lost, volume, pitch);

                        } else {
                            this.game.playSound(player, ownHitSound, volume, pitch);
                            Player secondPlayer = Bukkit.getPlayer(game.getSecondUUID());
                            this.game.playSound(secondPlayer, othersHitSound, volume, pitch);

                            if (!game.ruleFireAgainAfterHit) {
                                game.changeAttacker(false);
                            }
                        }
                    }
                    return;
                }
                return;

            case SECOND_TURN:
                if (isFirst) return;
                if (game.isCover(event.getCurrentItem())) {
                    if (!game.fire(isFirst, slot)) {
                        game.changeAttacker(true);
                        this.game.playSound(player, ownMissSound, volume, pitch);
                        Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
                        this.game.playSound(firstPlayer, othersMissSound, volume, pitch);

                    } else {
                        if (game.isWon(isFirst)) {
                            game.setState(GameState.FINISHED);
                            game.won(isFirst);
                            this.game.playSound(player, won, volume, pitch);
                            Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
                            this.game.playSound(firstPlayer, lost, volume, pitch);

                        } else {
                            this.game.playSound(player, ownHitSound, volume, pitch);
                            Player firstPlayer = Bukkit.getPlayer(game.getFirstUUID());
                            this.game.playSound(firstPlayer, othersHitSound, volume, pitch);

                            if (!game.ruleFireAgainAfterHit) {
                                game.changeAttacker(true);
                            }
                        }
                    }
                    return;
                }
                return;

            default:
                return;
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!isInGame(event.getPlayer().getUniqueId())) {
            return;
        }
        Game game = getGame(event.getPlayer().getUniqueId());
        game.cancelTimer();
        boolean firstClosed = event.getPlayer().getUniqueId().equals(game.getFirstUUID());
        Player winner = firstClosed ? game.getSecond() : game.getFirst();
        Player loser = firstClosed ? game.getFirst() : game.getSecond();
        if ((!firstClosed && game.getFirst() == null) || (firstClosed && game.getSecond() == null)) {
            removeGame(game);
            return;
        }

        // make sure the player is not counted as in game anymore
        if (firstClosed) {
            game.setFirst(null);
            game.setFirstUUID(null);
        } else {
            game.setSecond(null);
            game.setSecondUUID(null);
        }

        if (game.getState() != GameState.FINISHED) {
            if (game.getState() == GameState.CHANGING || game.getState() == GameState.FIRST_TURN || game.getState() == GameState.SECOND_TURN) {
                if (this.game.getSettings().isEconEnabled()) {
                    if (!Permission.BYPASS_GAME.hasPermission(winner, BattleshipPlugin.BATTLESHIP)) {
                        GameBox.econ.depositPlayer(winner, game.getRule().getMoneyToWin());
                        winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON_MONEY_GAVE_UP.replaceAll("%reward%", game.getRule().getMoneyToWin() + "").replaceAll("%loser%", loser.getName())));
                    } else {
                        winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_OTHER_GAVE_UP.replaceAll("%loser%", loser.getName())));
                    }
                } else {
                    winner.sendMessage(chatColor(lang.PREFIX + lang.GAME_WON.replaceAll("%loser%", loser.getName())));
                }
                loser.sendMessage(chatColor(lang.PREFIX + lang.GAME_GAVE_UP));


            }
            NmsFactory.getNmsUtility().updateInventoryTitle(winner, lang.TITLE_WON);
            game.setState(GameState.FINISHED);
            onGameEnd(winner, loser, game.getRule().getKey());
        }
        return;
    }

    public void addWin(UUID uuid, String key) {
        game.getGameBox().getDataBase().addStatistics(uuid, BattleshipPlugin.BATTLESHIP, key, 1., SaveType.WINS);
    }

    @Override
    public boolean isInGame(UUID uuid) {
        for (Game game : games) {
            if (isFirst(uuid, game) || isSecond(uuid, game)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void startGame(Player[] player, boolean b, String... strings) throws GameStartException {
        GameRules rule = gameTypes.get(strings[0]);
        if (rule == null) {
            throw new GameStartException(GameStartException.Reason.ERROR);
        }
        double cost = rule.getCost();
        boolean firstCanPay = true;
        if (game.getSettings().isEconEnabled() && !Permission.BYPASS_GAME.hasPermission(player[0], BattleshipPlugin.BATTLESHIP) && cost > 0.0) {
            if (GameBox.econ.getBalance(player[0]) >= cost) {
            } else {
                player[0].sendMessage(chatColor(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
                firstCanPay = false;
            }
        }
        if (game.getSettings().isEconEnabled() && !Permission.BYPASS_GAME.hasPermission(player[0], BattleshipPlugin.BATTLESHIP) && cost > 0.0) {
            if (GameBox.econ.getBalance(player[1]) >= cost) {
            } else {
                player[1].sendMessage(chatColor(lang.PREFIX + lang.GAME_NOT_ENOUGH_MONEY));
                if (firstCanPay) {
                    // only second player cannot pay
                    throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY_SECOND_PLAYER);
                } else {
                    // both players cannot pay
                    throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY);
                }
            }
        }
        if (!firstCanPay) {
            // only first player cannot pay
            throw new GameStartException(GameStartException.Reason.NOT_ENOUGH_MONEY_FIRST_PLAYER);
        }
        // both players can pay!
        if (game.getSettings().isEconEnabled()) {
            GameBox.econ.withdrawPlayer(player[0], cost);
            player[0].sendMessage(chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
            GameBox.econ.withdrawPlayer(player[1], cost);
            player[1].sendMessage(chatColor(lang.PREFIX + lang.GAME_PAYED.replaceAll("%cost%", String.valueOf(cost))));
        }
        games.add(new Game(game, player[0].getUniqueId(), player[1].getUniqueId(), rule));
    }

    @Override
    public void removeFromGame(UUID uuid) {
        Game game = getGame(uuid);
        game.cancelTimer();
        Player first = game.getFirst();
        Player second = game.getSecond();
        boolean firstClosed = uuid.equals(game.getFirstUUID());
        if ((!firstClosed && first == null) || (firstClosed && second == null)) {
            removeGame(game);
            return;
        }

        // make sure the player is not counted as in game anymore
        if (firstClosed) {
            game.setFirst(null);
            game.setFirstUUID(null);
        } else {
            game.setSecond(null);
            game.setSecondUUID(null);
        }

        game.setState(GameState.FINISHED);
        NmsFactory.getNmsUtility().updateInventoryTitle(firstClosed ? second : first, lang.TITLE_WON);

        if (game.getRule().isSaveStats()) {
            addWin(firstClosed ? game.getSecondUUID() : game.getFirstUUID(), game.getRule().getKey());
        }
        return;
    }

    @Override
    public void loadGameRules(ConfigurationSection buttonSec, String buttonID) {
        double cost = buttonSec.getDouble("cost", 0.);
        double reward = buttonSec.getDouble("reward", 0.);
        int tokens = buttonSec.getInt("tokens", 0);
        boolean saveStats = buttonSec.getBoolean("saveStats", false);
        boolean changeGridAfterHit = buttonSec.getBoolean("changeGridAfterHit", false);
        boolean switchGridsAfterFireTimerRanOut = buttonSec.getBoolean("switchGridsAfterFireTimerRanOut", false);
        int aircraftCarrier = buttonSec.getInt("aircraftCarrier", 1);
        int battleship = buttonSec.getInt("battleship", 1);
        int cruiser = buttonSec.getInt("cruiser", 1);
        int destroyer = buttonSec.getInt("destroyer", 1);

        gameTypes.put(buttonID, new GameRules(cost, reward, tokens, aircraftCarrier, battleship, cruiser, destroyer, changeGridAfterHit, switchGridsAfterFireTimerRanOut, buttonID, saveStats));
    }

    @Override
    public Map<String, ? extends GameRule> getGameRules() {
        return gameTypes;
    }

    public void onGameEnd(Player winner, Player loser, String key) {
        GameRules rule = gameTypes.get(key);
        if (rule.isSaveStats()) {
            addWin(winner.getUniqueId(), rule.getKey());
        }
        if (rule.getTokenToWin() > 0) {
            game.getGameBox().wonTokens(winner.getUniqueId(), rule.getTokenToWin(), BattleshipPlugin.BATTLESHIP);
        }
    }
}
