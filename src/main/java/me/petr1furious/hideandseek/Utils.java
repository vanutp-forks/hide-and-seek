package me.petr1furious.hideandseek;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class Utils {
    static public boolean playerPassable(Location location) {
        return location.getBlock().isPassable() && location.add(0, 1, 0).getBlock().isPassable();
    }

    static public Location getFirstSolidBlock(Location location) {
        var world = location.getWorld();
        var y = location.getBlockY();

        while (y > world.getMinHeight()) {
            if (!playerPassable(new Location(world, location.getBlockX(), y, location.getBlockZ()))) {
                break;
            }
            y--;
        }

        if (y == world.getMinHeight()) {
            y = world.getMaxHeight();
            while (y > world.getMinHeight()) {
                if (!playerPassable(new Location(world, location.getBlockX(), y, location.getBlockZ()))) {
                    break;
                }
                y--;
            }
        }

        if (y == world.getMinHeight()) {
            return null;
        }

        while (y < world.getMaxHeight()) {
            if (playerPassable(new Location(world, location.getBlockX(), y, location.getBlockZ()))) {
                break;
            }
            y++;
        }

        return new Location(world, location.getBlockX(), y, location.getBlockZ());
    }

    static public void teleportPlayerOnBlock(Player player) {
        var location = getFirstSolidBlock(player.getLocation());
        if (location != null) {
            location.add(0.5, 0, 0.5);
            location.setYaw(player.getLocation().getYaw());
            location.setPitch(player.getLocation().getPitch());
            player.teleport(location);
        }
    }

    static public void spawnExplosion(Location location, double explosionPower, Entity entity) {
        location.getWorld().createExplosion(entity, location, (float) explosionPower, false, true, false);
    }

    static public void spawnExplosion(Location location, double explosionPower, Entity entity,
        boolean excludeEntityDamage) {
        location.getWorld().createExplosion(entity, location, (float) explosionPower, false, true, excludeEntityDamage);
    }

    static public Entity getEntityShooter(Projectile projectile) {
        if (projectile.getShooter() instanceof Entity) {
            return (Entity) projectile.getShooter();
        }
        return null;
    }
}
