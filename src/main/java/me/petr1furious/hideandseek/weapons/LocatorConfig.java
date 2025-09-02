package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class LocatorConfig implements WeaponConfig {
    private long cooldown = 30; // seconds
    private double precision = 5;
    private int steps = 5;
    private int effectLength = 8; // seconds

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("cooldown", "precision", "steps", "effectLength");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "cooldown":
                cooldown = Long.parseLong(value);
                return WeaponSetResult.SUCCESS;
            case "precision":
                precision = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "steps":
                steps = Integer.parseInt(value);
                return WeaponSetResult.SUCCESS;
            case "effectlength":
                effectLength = Integer.parseInt(value);
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
        case "cooldown":
            return cooldown;
        case "precision":
            return precision;
        case "steps":
            return steps;
        case "effectlength":
            return effectLength;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("cooldown", cooldown);
        section.set("precision", precision);
        section.set("steps", steps);
        section.set("effectLength", effectLength);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        cooldown = section.getLong("cooldown", cooldown);
        precision = section.getDouble("precision", precision);
        steps = section.getInt("steps", steps);
        effectLength = section.getInt("effectLength", effectLength);
    }

    public long getCooldown() {
        return cooldown;
    }

    public double getPrecision() {
        return precision;
    }

    public int getSteps() {
        return steps;
    }

    public int getEffectLength() {
        return effectLength;
    }
}
