package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class RadarConfig implements WeaponConfig {
    private double maxDistance = 40;
    private double maxAngleDistance = 40;
    private long cooldown = 40; // seconds
    private int glowingEffectLength = 4; // seconds

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("maxDistance", "maxAngleDistance", "cooldown", "glowingEffectLength");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "maxdistance":
                maxDistance = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "maxangledistance":
                maxAngleDistance = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "cooldown":
                cooldown = Long.parseLong(value);
                return WeaponSetResult.SUCCESS;
            case "glowingeffectlength":
                glowingEffectLength = Integer.parseInt(value);
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
        case "maxdistance":
            return maxDistance;
        case "maxangledistance":
            return maxAngleDistance;
        case "cooldown":
            return cooldown;
        case "glowingeffectlength":
            return glowingEffectLength;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("maxDistance", maxDistance);
        section.set("maxAngleDistance", maxAngleDistance);
        section.set("cooldown", cooldown);
        section.set("glowingEffectLength", glowingEffectLength);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        maxDistance = section.getDouble("maxDistance", maxDistance);
        maxAngleDistance = section.getDouble("maxAngleDistance", maxAngleDistance);
        cooldown = section.getLong("cooldown", cooldown);
        glowingEffectLength = section.getInt("glowingEffectLength", glowingEffectLength);
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double getMaxAngleDistance() {
        return maxAngleDistance;
    }

    public long getCooldown() {
        return cooldown;
    }

    public int getGlowingEffectLength() {
        return glowingEffectLength;
    }
}
