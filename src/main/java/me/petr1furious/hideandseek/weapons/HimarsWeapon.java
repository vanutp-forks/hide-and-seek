package me.petr1furious.hideandseek.weapons;

import me.petr1furious.hideandseek.GameConfig;
import me.petr1furious.hideandseek.Items;
import me.petr1furious.hideandseek.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import java.util.HashMap;
import java.util.Map;

public class HimarsWeapon {
    private final GameConfig config;
    private final Map<Projectile, Location> projectileStart = new HashMap<>();

    private final String TAG = "himars";

    public HimarsWeapon(GameConfig config) {
        this.config = config;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Items.checkForItem(event.getItem(), TAG))
            return;
        var item = event.getItem();
        if (!(item.getItemMeta() instanceof CrossbowMeta meta))
            return;
        if (meta.getChargedProjectiles().isEmpty()) {
            meta.addChargedProjectile(new ItemStack(Material.FIREWORK_ROCKET));
            item.setItemMeta(meta);
        }
    }

    public void onBowShoot(EntityShootBowEvent event) {
        var bow = event.getBow();
        if (bow == null)
            return;
        if (event.getProjectile() instanceof Firework firework && Items.checkForItem(bow, TAG)) {
            Player shooter = (Player) event.getEntity();
            int remaining = shooter.getGameMode() == GameMode.SURVIVAL ? getRemainingCooldownSeconds(shooter) : 0;
            if (remaining > 0) {
                shooter.sendActionBar(Component.text("HIMARS: " + remaining + "s").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
            setCooldown(shooter);
            applyLaunchDirection(firework, shooter);
            Items.setProjectileTag(firework, TAG);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(20);
            firework.setFireworkMeta(meta);
            projectileStart.put(firework, shooter.getLocation());
        }
    }

    public boolean handleProjectileImpact(Projectile projectile, Location location, Entity shooter, Entity target) {
        if (!Items.checkForProjectile(projectile, TAG))
            return false;
        Location start = projectileStart.remove(projectile);
        double power = 1.0;
        if (start != null) {
            power = Math.min(start.distance(location) * config.getHimars().getExplosionPowerPerBlock(),
                config.getHimars().getMaxExplosionPower());
        }
        Utils.spawnExplosion(location, power, shooter);
        projectile.remove();
        return true;
    }

    private int getRemainingCooldownSeconds(Player player) {
        return (int) Math.ceil(player.getCooldown(Material.CROSSBOW) / 20.0);
    }

    private void setCooldown(Player player) {
        player.setCooldown(Material.CROSSBOW, Math.toIntExact(config.getHimars().getCooldown() * 20L));
    }

    private void applyLaunchDirection(Firework firework, Player shooter) {
        Vector vanillaVelocity = firework.getVelocity();
        Vector vanillaDirection = vanillaVelocity.clone().normalize();
        Vector aimDirection = shooter.getEyeLocation().getDirection().normalize();
        double blend = Math.max(0.0, Math.min(1.0, config.getHimars().getAimDirectionBlend()));
        Vector finalDirection = aimDirection.multiply(blend).add(vanillaDirection.multiply(1.0 - blend));

        if (finalDirection.lengthSquared() <= 1.0E-6) {
            finalDirection = aimDirection;
        } else {
            finalDirection.normalize();
        }

        firework.setVelocity(finalDirection.multiply(config.getHimars().getFireworkSpeedBlocksPerTick()));
    }

    public ItemStack createItem(int count) {
        var crossbow = new ItemStack(Material.CROSSBOW);
        Items.addTag(crossbow, TAG);
        var meta = crossbow.getItemMeta();
        meta.displayName(Component.text("HIMARS").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);
        crossbow.setItemMeta(meta);
        crossbow.setAmount(count);
        return crossbow;
    }
}
