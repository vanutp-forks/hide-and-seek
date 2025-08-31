package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class InfiniteCrossbowConfig implements WeaponConfig {
    private boolean enableExplosions = true;
    private double explosionPower = 2.0;
    private int maxLoadedProjectiles = 1;

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("enableExplosions", "explosionPower", "maxLoadedProjectiles");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "enableexplosions":
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    enableExplosions = Boolean.parseBoolean(value);
                    return WeaponSetResult.SUCCESS;
                }
                return WeaponSetResult.PARSE_ERROR;
            case "explosionpower":
                explosionPower = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "maxloadedprojectiles":
                maxLoadedProjectiles = Integer.parseInt(value);
                return WeaponSetResult.SUCCESS;
            default:
                return WeaponSetResult.UNKNOWN_PROPERTY;
            }
        } catch (NumberFormatException e) {
            return WeaponSetResult.PARSE_ERROR;
        }
    }

    @Override
    public Object getPropertyValue(String name) {
        switch (name.toLowerCase()) {
        case "enableexplosions":
            return enableExplosions;
        case "explosionpower":
            return explosionPower;
        case "maxloadedprojectiles":
            return maxLoadedProjectiles;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("enableExplosions", enableExplosions);
        section.set("explosionPower", explosionPower);
        section.set("maxLoadedProjectiles", maxLoadedProjectiles);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        enableExplosions = section.getBoolean("enableExplosions", enableExplosions);
        explosionPower = section.getDouble("explosionPower", explosionPower);
        maxLoadedProjectiles = section.getInt("maxLoadedProjectiles", maxLoadedProjectiles);
    }

    public boolean isEnableExplosions() {
        return enableExplosions;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public int getMaxLoadedProjectiles() {
        return maxLoadedProjectiles;
    }
}
