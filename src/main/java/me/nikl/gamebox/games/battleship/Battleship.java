package me.nikl.gamebox.games.battleship;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.game.GameSettings;
import me.nikl.gamebox.games.BattleshipPlugin;
import me.nikl.gamebox.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * @author Niklas Eicker
 */
public class Battleship extends me.nikl.gamebox.game.Game {
    protected ItemStack ownShip, ownWater, ownMiss, ownHit, othersCover, othersMiss, othersHit, lockedShip;

    public Battleship(GameBox gameBox) {
        super(gameBox, BattleshipPlugin.BATTLESHIP);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void init() {
        if (!getMaterials()) {
            setDefaultMaterials();
        }
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

    private boolean getMaterials() {
        boolean worked = true;
        Material mat = null;
        int data = 0;
        for (String key : Arrays.asList("yourGrid.ship", "yourGrid.lockedShip", "yourGrid.miss", "yourGrid.hit", "yourGrid.water", "othersGrid.cover", "othersGrid.miss", "othersGrid.hit")) {
            // get the material information
            String value;
            if (!config.isSet("materials." + key + ".material")) {
                if (!config.isSet("materials." + key) || !config.isString("materials." + key)) {
                    return false;
                } else {
                    // TODO: 9/26/16 get rid of this in later versions or find a better solution
                    // version 1.6 or older
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', gameLang.PREFIX + " &4You are using an outdated config file!"));
                    value = config.getString("materials." + key);
                }
            } else {
                value = config.getString("materials." + key + ".material");
            }
            String[] obj = value.split(":");


            if (obj.length == 2) {
                try {
                    mat = Material.matchMaterial(obj[0]);
                } catch (Exception e) {
                    worked = false; // material name doesn't exist
                }

                try {
                    data = Integer.valueOf(obj[1]);
                } catch (NumberFormatException e) {
                    worked = false; // data not a number
                }
            } else {
                try {
                    mat = Material.matchMaterial(value);
                } catch (Exception e) {
                    worked = false; // material name doesn't exist
                }
            }
            if (mat == null) return false;
            if (key.equals("yourGrid.ship")) {
                this.ownShip = new ItemStack(mat, 1);
                if (obj.length == 2) ownShip.setDurability((short) data);
                ItemMeta meta = ownShip.getItemMeta();
                meta.setDisplayName("Ship");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                ownShip.setItemMeta(meta);

            } else if (key.equals("yourGrid.lockedShip")) {
                this.lockedShip = new ItemStack(mat, 1);
                if (obj.length == 2) lockedShip.setDurability((short) data);
                ItemMeta meta = lockedShip.getItemMeta();
                meta.setDisplayName("Locked ship");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                lockedShip.setItemMeta(meta);

            } else if (key.equals("yourGrid.miss")) {
                this.ownMiss = new ItemStack(mat, 1);
                if (obj.length == 2) ownMiss.setDurability((short) data);
                ItemMeta meta = ownMiss.getItemMeta();
                meta.setDisplayName("Yeah! A miss!");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                ownMiss.setItemMeta(meta);

            } else if (key.equals("yourGrid.hit")) {
                this.ownHit = new ItemStack(mat, 1);
                if (obj.length == 2) ownHit.setDurability((short) data);
                ItemMeta meta = ownHit.getItemMeta();
                meta.setDisplayName("Damn! A hit...");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                ownHit.setItemMeta(meta);

            } else if (key.equals("yourGrid.water")) {
                this.ownWater = new ItemStack(mat, 1);
                if (obj.length == 2) ownWater.setDurability((short) data);
                ItemMeta meta = ownWater.getItemMeta();
                meta.setDisplayName("Water");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                ownWater.setItemMeta(meta);

            } else if (key.equals("othersGrid.cover")) {
                this.othersCover = new ItemStack(mat, 1);
                if (obj.length == 2) othersCover.setDurability((short) data);
                ItemMeta meta = othersCover.getItemMeta();
                meta.setDisplayName("Cover");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                othersCover.setItemMeta(meta);

            } else if (key.equals("othersGrid.miss")) {
                this.othersMiss = new ItemStack(mat, 1);
                if (obj.length == 2) othersMiss.setDurability((short) data);
                ItemMeta meta = othersMiss.getItemMeta();
                meta.setDisplayName("That did not hit...");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                othersMiss.setItemMeta(meta);

            } else if (key.equals("othersGrid.hit")) {
                this.othersHit = new ItemStack(mat, 1);
                if (obj.length == 2) othersHit.setDurability((short) data);
                ItemMeta meta = othersHit.getItemMeta();
                meta.setDisplayName("Booom! Gotcha");
                // if a name was specified use it instead of default
                if (config.isSet("materials." + key + ".name") || config.isString("materials." + key + ".name"))
                    meta.setDisplayName(StringUtility.color(config.getString("materials." + key + ".name")));
                othersHit.setItemMeta(meta);
            }
        }
        return worked;
    }

    private void setDefaultMaterials() {
        Bukkit.getConsoleSender().sendMessage(StringUtility.color(gameLang.PREFIX + " &4Failed to load materials from config"));
        Bukkit.getConsoleSender().sendMessage(StringUtility.color(gameLang.PREFIX + " &4Using default materials"));

        this.ownShip = new ItemStack(Material.IRON_BLOCK);
        ItemMeta metaownShip = ownShip.getItemMeta();
        metaownShip.setDisplayName("Ship");
        ownShip.setItemMeta(metaownShip);
        ownShip.setAmount(1);

        this.lockedShip = new ItemStack(Material.BEDROCK);
        ItemMeta metaLockedShip = lockedShip.getItemMeta();
        metaLockedShip.setDisplayName("Ship");
        lockedShip.setItemMeta(metaLockedShip);
        lockedShip.setAmount(1);

        this.ownWater = new ItemStack(Material.STAINED_GLASS_PANE);
        ownWater.setDurability((short) 11);
        ItemMeta metaownWater = ownWater.getItemMeta();
        metaownWater.setDisplayName("Water");
        ownWater.setItemMeta(metaownWater);
        ownWater.setAmount(1);

        this.ownMiss = new ItemStack(Material.WOOL);
        ownMiss.setDurability((short) 13);
        ItemMeta metaownMiss = ownMiss.getItemMeta();
        metaownMiss.setDisplayName("Yeah! A miss!");
        ownMiss.setItemMeta(metaownMiss);
        ownMiss.setAmount(1);

        this.ownHit = new ItemStack(Material.WOOL);
        ownHit.setDurability((short) 14);
        ItemMeta metaownHit = ownHit.getItemMeta();
        metaownHit.setDisplayName("Damn! A hit...");
        ownHit.setItemMeta(metaownHit);
        ownHit.setAmount(1);

        this.othersCover = new ItemStack(Material.WOOL);
        othersCover.setDurability((short) 7);
        ItemMeta metaothersCover = othersCover.getItemMeta();
        metaothersCover.setDisplayName("Cover");
        othersCover.setItemMeta(metaothersCover);
        othersCover.setAmount(1);

        this.othersMiss = new ItemStack(Material.STAINED_GLASS_PANE);
        othersMiss.setDurability((short) 11);
        ItemMeta metaothersMiss = othersMiss.getItemMeta();
        metaothersMiss.setDisplayName("That did not hit...");
        othersMiss.setItemMeta(metaothersMiss);
        othersMiss.setAmount(1);

        this.othersHit = new ItemStack(Material.IRON_BLOCK);
        ItemMeta metaothersHit = othersHit.getItemMeta();
        metaothersHit.setDisplayName("Booom! Gotcha");
        othersHit.setItemMeta(metaothersHit);
        othersHit.setAmount(1);
    }
}
