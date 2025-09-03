package me.petr1furious.hideandseek.weapons;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

public class FPVDroneConfig implements WeaponConfig {
    private int maxFlightTicks = 20 * 20; // 20s
    private double explosionPower = 4.0;

    public int getMaxFlightTicks() {
        return maxFlightTicks;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("maxFlightSeconds", "explosionPower");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name) {
            case "maxFlightSeconds" -> maxFlightTicks = (int) (Double.parseDouble(value) * 20.0);
            case "explosionPower" -> explosionPower = Double.parseDouble(value);
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
        default -> null;
        };
    }

    @Override
    public void save(ConfigurationSection section) {
        if (section == null)
            return;
        section.set("maxFlightSeconds", maxFlightTicks / 20.0);
        section.set("explosionPower", explosionPower);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        maxFlightTicks = (int) (section.getDouble("maxFlightSeconds", maxFlightTicks / 20.0) * 20.0);
        explosionPower = section.getDouble("explosionPower", explosionPower);
    }
}
