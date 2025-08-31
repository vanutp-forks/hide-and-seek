package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class OreshnikConfig implements WeaponConfig {
    private int wavesCount = 6;
    private int arrowsCount = 100;
    private double explosionPower = 1.8;
    private int wavesDelay = 13; // ticks
    private double range = 0.5; // radius range for arrows scatter

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("wavesCount", "arrowsCount", "explosionPower", "wavesDelay", "range");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "wavescount":
                wavesCount = Integer.parseInt(value);
                return WeaponSetResult.SUCCESS;
            case "arrowscount":
                arrowsCount = Integer.parseInt(value);
                return WeaponSetResult.SUCCESS;
            case "explosionpower":
                explosionPower = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "wavesdelay":
                wavesDelay = Integer.parseInt(value);
                return WeaponSetResult.SUCCESS;
            case "range":
                range = Double.parseDouble(value);
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
        case "wavescount":
            return wavesCount;
        case "arrowscount":
            return arrowsCount;
        case "explosionpower":
            return explosionPower;
        case "wavesdelay":
            return wavesDelay;
        case "range":
            return range;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("wavesCount", wavesCount);
        section.set("arrowsCount", arrowsCount);
        section.set("explosionPower", explosionPower);
        section.set("wavesDelay", wavesDelay);
        section.set("range", range);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        wavesCount = section.getInt("wavesCount", wavesCount);
        arrowsCount = section.getInt("arrowsCount", arrowsCount);
        explosionPower = section.getDouble("explosionPower", explosionPower);
        wavesDelay = section.getInt("wavesDelay", wavesDelay);
        range = section.getDouble("range", range);
    }

    public int getWavesCount() {
        return wavesCount;
    }

    public int getArrowsCount() {
        return arrowsCount;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public int getWavesDelay() {
        return wavesDelay;
    }

    public double getRange() {
        return range;
    }
}
