package me.petr1furious.hideandseek.weapons;

import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public class HimarsConfig implements WeaponConfig {
    private long cooldown = 10; // seconds
    private double explosionPowerPerBlock = 1 / 30.0;
    private double maxExplosionPower = 5.0;
    private double fireworkSpeedBlocksPerTick = 3.0;
    private double aimDirectionBlend = 0.85;

    @Override
    public List<String> getPropertyNames() {
        return Arrays.asList("cooldown", "explosionPowerPerBlock", "maxExplosionPower", "fireworkSpeedBlocksPerTick",
            "aimDirectionBlend");
    }

    @Override
    public WeaponSetResult setProperty(String name, String value) {
        try {
            switch (name.toLowerCase()) {
            case "cooldown":
                cooldown = Long.parseLong(value);
                return WeaponSetResult.SUCCESS;
            case "explosionpowerperblock":
                explosionPowerPerBlock = Float.parseFloat(value);
                return WeaponSetResult.SUCCESS;
            case "fireworkspeed":
            case "fireworkspeedblockspertick":
                fireworkSpeedBlocksPerTick = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "maxexplosionpower":
                maxExplosionPower = Double.parseDouble(value);
                return WeaponSetResult.SUCCESS;
            case "aimdirectionblend":
                aimDirectionBlend = Double.parseDouble(value);
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
        case "explosionpowerperblock":
            return explosionPowerPerBlock;
        case "maxexplosionpower":
            return maxExplosionPower;
        case "fireworkspeed":
        case "fireworkspeedblockspertick":
            return fireworkSpeedBlocksPerTick;
        case "aimdirectionblend":
            return aimDirectionBlend;
        default:
            return null;
        }
    }

    @Override
    public void save(ConfigurationSection section) {
        section.set("cooldown", cooldown);
        section.set("explosionPowerPerBlock", explosionPowerPerBlock);
        section.set("maxExplosionPower", maxExplosionPower);
        section.set("fireworkSpeedBlocksPerTick", fireworkSpeedBlocksPerTick);
        section.set("aimDirectionBlend", aimDirectionBlend);
    }

    @Override
    public void load(ConfigurationSection section) {
        if (section == null)
            return;
        cooldown = section.getLong("cooldown", cooldown);
        explosionPowerPerBlock = section.getDouble("explosionPowerPerBlock", explosionPowerPerBlock);
        maxExplosionPower = section.getDouble("maxExplosionPower", maxExplosionPower);
        fireworkSpeedBlocksPerTick = section.getDouble("fireworkSpeedBlocksPerTick",
            section.getDouble("fireworkSpeed", fireworkSpeedBlocksPerTick));
        aimDirectionBlend = section.getDouble("aimDirectionBlend", aimDirectionBlend);
    }

    public long getCooldown() {
        return cooldown;
    }

    public double getExplosionPowerPerBlock() {
        return explosionPowerPerBlock;
    }

    public double getMaxExplosionPower() {
        return maxExplosionPower;
    }

    public double getFireworkSpeedBlocksPerTick() {
        return fireworkSpeedBlocksPerTick;
    }

    public double getAimDirectionBlend() {
        return aimDirectionBlend;
    }
}
