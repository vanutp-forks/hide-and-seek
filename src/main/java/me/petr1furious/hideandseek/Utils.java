package me.petr1furious.hideandseek;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
        if (entity == null) {
            location.getWorld().createExplosion(location, (float) explosionPower, false, true);
        } else {
            location.getWorld().createExplosion(location, (float) explosionPower, false, true);
        }
    }

    static public void killWithExplosion(Entity target, Entity attacker) {
        if (target instanceof Damageable) {
            var damageable = (Damageable) target;
            var damageSource = DamageSource.builder(DamageType.EXPLOSION);
            if (attacker != null) {
                damageSource.withCausingEntity(attacker);
            }
            damageable.damage(1000, damageSource.build());
        } else {
            target.remove();
        }
    }

    static public Entity getEntityShooter(Arrow arrow) {
        if (arrow.getShooter() instanceof Entity) {
            return (Entity) arrow.getShooter();
        }
        return null;
    }
}
