package me.petr1furious.hideandseek.weapons;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

public class FPVDroneConfig implements WeaponConfig {
    private int maxFlightTicks = 20 * 20; // 20s
    private double explosionPower = 4.0;
    private float soundVolume = 6.0f;

    public int getMaxFlightTicks() {
        return maxFlightTicks;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("maxFlightSeconds", "explosionPower", "soundVolume");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name) {
            case "maxFlightSeconds" -> maxFlightTicks = (int) (Double.parseDouble(value) * 20.0);
            case "explosionPower" -> explosionPower = Double.parseDouble(value);
            case "soundVolume" -> soundVolume = Float.parseFloat(value);
            default -> {
                return WeaponSetResult.UNKNOWN_PROPERTY;
            }
            }
            return WeaponSetResult.SUCCESS;
        } catch (Exception e) {
            return WeaponSetResult.PARSE_ERROR;
        }
    }

    @Override
    public Object getPropertyValue(String name) {
        return switch (name) {
        case "maxFlightSeconds" -> maxFlightTicks / 20.0;
        case "explosionPower" -> explosionPower;
        case "soundVolume" -> soundVolume;
        default -> null;
        };
    }

    @Override
    public void save(ConfigurationSection section) {
        if (section == null)
            return;
        section.set("maxFlightSeconds", maxFlightTicks / 20.0);
        section.set("explosionPower", explosionPower);
        section.set("soundVolume", soundVolume);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        maxFlightTicks = (int) (section.getDouble("maxFlightSeconds", maxFlightTicks / 20.0) * 20.0);
        explosionPower = section.getDouble("explosionPower", explosionPower);
        soundVolume = (float) section.getDouble("soundVolume", soundVolume);
    }
}
