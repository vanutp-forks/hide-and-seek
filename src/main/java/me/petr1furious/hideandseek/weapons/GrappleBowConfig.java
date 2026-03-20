package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class GrappleBowConfig implements WeaponConfig {
    private long cooldown = 6; // seconds
    private double pullStrength = 1.8;
    private double verticalBoost = 0.2;
    private double maxSpeed = 2.8;
    private boolean disableWhileGliding = true;

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("cooldown", "pullStrength", "verticalBoost", "maxSpeed", "disableWhileGliding");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "cooldown":
                cooldown = Long.parseLong(value);
                return WeaponSetResult.SUCCESS;
            case "pullstrength":
                pullStrength = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "verticalboost":
                verticalBoost = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "maxspeed":
                maxSpeed = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "disablewhilegliding":
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                    disableWhileGliding = Boolean.parseBoolean(value);
                    return WeaponSetResult.SUCCESS;
                }
                return WeaponSetResult.PARSE_ERROR;
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
        case "pullstrength":
            return pullStrength;
        case "verticalboost":
            return verticalBoost;
        case "maxspeed":
            return maxSpeed;
        case "disablewhilegliding":
            return disableWhileGliding;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("cooldown", cooldown);
        section.set("pullStrength", pullStrength);
        section.set("verticalBoost", verticalBoost);
        section.set("maxSpeed", maxSpeed);
        section.set("disableWhileGliding", disableWhileGliding);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        cooldown = section.getLong("cooldown", cooldown);
        pullStrength = section.getDouble("pullStrength", pullStrength);
        verticalBoost = section.getDouble("verticalBoost", verticalBoost);
        maxSpeed = section.getDouble("maxSpeed", maxSpeed);
        disableWhileGliding = section.getBoolean("disableWhileGliding", disableWhileGliding);
    }

    public long getCooldown() {
        return cooldown;
    }

    public double getPullStrength() {
        return pullStrength;
    }

    public double getVerticalBoost() {
        return verticalBoost;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public boolean isDisableWhileGliding() {
        return disableWhileGliding;
    }
}
