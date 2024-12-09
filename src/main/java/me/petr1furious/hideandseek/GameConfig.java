package me.petr1furious.hideandseek;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class GameConfig {
    FileConfiguration config;

    private Vector gameCenter;
    private String gameWorld;
    private int gameRadius;

    private boolean enableExplosions;
    private double explosionPower;

    private boolean enableLifts;
    private Material liftMaterial;

    private int maxLoadedCrossbowProjectiles;

    private int oreshnikWavesCount;
    private int oreshnikArrowsCount;
    private double oreshnikExplosionPower;
    private int oreshnikWavesDelay;
    private double oreshnikRange;

    private double himarsFireworkSpeed;

    private ItemStack[] gameInventory;
    private boolean enableGameInventory;

    private long himarsCooldown;
    private int explosionIncreasePerBlocks;

    public GameConfig(FileConfiguration config) {
        this.config = config;
        load();
    }

    public void load() {
        try {
            gameCenter = config.isVector("gameCenter") ? config.getVector("gameCenter") : new Vector(0, 0, 0);
        } catch (Exception e) {
            gameCenter = new Vector(0, 0, 0);
        }

        gameWorld = config.getString("gameWorld", "world");
        gameRadius = config.getInt("gameRadius", 200);

        enableExplosions = config.getBoolean("enableExplosions", true);
        explosionPower = config.getDouble("explosionPower", 2.0);

        enableLifts = config.getBoolean("enableLifts", true);
        try {
            liftMaterial = Material.valueOf(config.getString("liftMaterial", "LIGHT_GRAY_CONCRETE").toUpperCase());
        } catch (IllegalArgumentException e) {
            liftMaterial = Material.LIGHT_GRAY_CONCRETE;
        }

        maxLoadedCrossbowProjectiles = config.getInt("maxLoadedCrossbowProjectiles", 1);

        oreshnikWavesCount = config.getInt("oreshnikWavesCount", 5);
        oreshnikArrowsCount = config.getInt("oreshnikArrowsCount", 50);
        oreshnikExplosionPower = config.getDouble("oreshnikExplosionPower", 3.0);
        oreshnikWavesDelay = config.getInt("oreshnikWavesDelay", 20);
        oreshnikRange = config.getDouble("oreshnikRange", 0.5);

        himarsCooldown = config.getLong("himarsCooldown", 10);
        explosionIncreasePerBlocks = config.getInt("explosionIncreasePerBlocks", 15);
        himarsFireworkSpeed = config.getDouble("himarsFireworkSpeed", 1.5);

        enableGameInventory = config.getBoolean("enableGameInventory", false);
        gameInventory = config.getList("gameInventory", new ArrayList<ItemStack>()).toArray(new ItemStack[0]);

    }

    public void save() {
        config.set("gameCenter", gameCenter);
        config.set("gameWorld", gameWorld);
        config.set("gameRadius", gameRadius);
        config.set("enableExplosions", enableExplosions);
        config.set("explosionPower", explosionPower);
        config.set("enableLifts", enableLifts);
        config.set("liftMaterial", liftMaterial.toString());
        config.set("maxLoadedCrossbowProjectiles", maxLoadedCrossbowProjectiles);
        config.set("oreshnikWavesCount", oreshnikWavesCount);
        config.set("oreshnikArrowsCount", oreshnikArrowsCount);
        config.set("oreshnikExplosionPower", oreshnikExplosionPower);
        config.set("oreshnikWavesDelay", oreshnikWavesDelay);
        config.set("oreshnikRange", oreshnikRange);
        config.set("himarsCooldown", himarsCooldown);
        config.set("explosionIncreasePerBlocks", explosionIncreasePerBlocks);
        config.set("himarsFireworkSpeed", himarsFireworkSpeed);
        config.set("enableGameInventory", enableGameInventory);
        config.set("gameInventory", gameInventory);
    }

    public Vector getGameCenter() {
        return gameCenter;
    }

    public String getGameWorld() {
        return gameWorld;
    }

    public int getGameRadius() {
        return gameRadius;
    }

    public boolean isEnableExplosions() {
        return enableExplosions;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public boolean isEnableLifts() {
        return enableLifts;
    }

    public Material getLiftMaterial() {
        return liftMaterial;
    }

    public int getMaxLoadedCrossbowProjectiles() {
        return maxLoadedCrossbowProjectiles;
    }

    public int getOreshnikWavesCount() {
        return oreshnikWavesCount;
    }

    public int getOreshnikArrowsCount() {
        return oreshnikArrowsCount;
    }

    public double getOreshnikExplosionPower() {
        return oreshnikExplosionPower;
    }

    public int getOreshnikWavesDelay() {
        return oreshnikWavesDelay;
    }

    public double getOreshnikRange() {
        return oreshnikRange;
    }

    public long getHimarsCooldown() {
        return himarsCooldown;
    }

    public int getExplosionIncreasePerBlocks() {
        return explosionIncreasePerBlocks;
    }

    public double getHimarsFireworkSpeed() {
        return himarsFireworkSpeed;
    }

    public ItemStack[] getGameInventory() {
        return gameInventory;
    }

    public boolean isEnableGameInventory() {
        return enableGameInventory;
    }

    public void setGameCenter(Vector gameCenter) {
        this.gameCenter = gameCenter;
        save();
    }

    public void setGameWorld(String gameWorld) {
        this.gameWorld = gameWorld;
        save();
    }

    public void setGameRadius(int gameRadius) {
        this.gameRadius = gameRadius;
        save();
    }

    public void setEnableExplosions(boolean enableExplosions) {
        this.enableExplosions = enableExplosions;
        save();
    }

    public void setExplosionPower(double explosionPower) {
        this.explosionPower = explosionPower;
        save();
    }

    public void setEnableLifts(boolean enableLifts) {
        this.enableLifts = enableLifts;
        save();
    }

    public void setMaxLoadedCrossbowProjectiles(int maxLoadedCrossbowProjectiles) {
        this.maxLoadedCrossbowProjectiles = maxLoadedCrossbowProjectiles;
        save();
    }

    public void setOreshnikWavesCount(int oreshnikWavesCount) {
        this.oreshnikWavesCount = oreshnikWavesCount;
        save();
    }

    public void setOreshnikArrowsCount(int oreshnikArrowsCount) {
        this.oreshnikArrowsCount = oreshnikArrowsCount;
        save();
    }

    public void setOreshnikExplosionPower(double oreshnikExplosionPower) {
        this.oreshnikExplosionPower = oreshnikExplosionPower;
        save();
    }

    public void setOreshnikWavesDelay(int oreshnikWavesDelay) {
        this.oreshnikWavesDelay = oreshnikWavesDelay;
        save();
    }

    public void setOreshnikRange(double oreshnikRange) {
        this.oreshnikRange = oreshnikRange;
        save();
    }

    public void setHimarsCooldown(long himarsCooldown) {
        this.himarsCooldown = himarsCooldown;
        save();
    }

    public void setExplosionIncreasePerBlocks(int explosionIncreasePerBlocks) {
        this.explosionIncreasePerBlocks = explosionIncreasePerBlocks;
        save();
    }

    public void setHimarsFireworkSpeed(double himarsFireworkSpeed) {
        this.himarsFireworkSpeed = himarsFireworkSpeed;
        save();
    }

    public void setGameInventory(ItemStack[] gameInventory) {
        this.gameInventory = gameInventory;
        save();
    }

    public void setEnableGameInventory(boolean enableGameInventory) {
        this.enableGameInventory = enableGameInventory;
        save();
    }
}
