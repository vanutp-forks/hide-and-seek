package me.petr1furious.hideandseek.weapons;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public interface WeaponConfig {
    enum WeaponSetResult {
        SUCCESS, UNKNOWN_PROPERTY, PARSE_ERROR
    }

    List<String> getPropertyNames();

    WeaponSetResult setProperty(String name, String value);

    Object getPropertyValue(String name);

    void save(ConfigurationSection section);

    void load(ConfigurationSection section);
}
