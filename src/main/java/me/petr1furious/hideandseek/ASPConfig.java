package me.petr1furious.hideandseek;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class ASPConfig {
    boolean enable = false;
    boolean resetWorldOnStart = true;
    @Nullable
    String templateWorldName = null;
    @Nullable
    String gameWorldName = null;

    public void load(ConfigurationSection section) {
        if (section == null) {
            enable = false;
            resetWorldOnStart = true;
            templateWorldName = null;
            gameWorldName = null;
            return;
        }
        enable = section.getBoolean("enable", false);
        resetWorldOnStart = section.getBoolean("resetWorldOnStart", true);
        templateWorldName = section.getString("templateWorldName");
        gameWorldName = section.getString("gameWorldName");
        if (enable && (templateWorldName == null || gameWorldName == null)) {
            throw new IllegalStateException("templateWorldName and gameWorldName must be set if ASP support is enabled");
        }
    }

    public void save(ConfigurationSection section) {
        section.set("enable", enable);
        section.set("resetWorldOnStart", resetWorldOnStart);
        section.set("templateWorldName", templateWorldName);
        section.set("gameWorldName", gameWorldName);
    }
}
