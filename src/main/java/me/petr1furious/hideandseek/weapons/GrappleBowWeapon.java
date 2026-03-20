package me.petr1furious.hideandseek.weapons;

import me.petr1furious.hideandseek.GameConfig;
import me.petr1furious.hideandseek.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class GrappleBowWeapon {
    private final GameConfig config;
    private final String TAG = "grapple_bow";

    public GrappleBowWeapon(GameConfig config) {
        this.config = config;
    }

    @SuppressWarnings("deprecation")
    public void onBowShoot(EntityShootBowEvent event) {
        var bow = event.getBow();
        if (bow == null || !Items.checkForItem(bow, TAG))
            return;
        if (!(event.getEntity() instanceof Player shooter))
            return;

        GrappleBowConfig grapple = config.getGrappleBow();
        if (grapple.isDisableWhileGliding() && shooter.isGliding()) {
            shooter.sendActionBar(Component.text("Grapple Bow disabled while gliding").color(NamedTextColor.RED));
            shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.2f, 0.5f);
            event.setCancelled(true);
            return;
        }
        int remaining = getRemainingCooldownSeconds(shooter);
        if (remaining > 0) {
            shooter.sendActionBar(Component.text("Grapple Bow: " + remaining + "s").color(NamedTextColor.RED));
            shooter.playSound(shooter.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.2f, 0.5f);
            event.setCancelled(true);
            return;
        }

        setCooldown(shooter, grapple);
        event.setConsumeItem(false);

        if (event.getProjectile() instanceof AbstractArrow arrow) {
            Items.setProjectileTag(arrow, TAG);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            arrow.setDamage(0.0);
            arrow.setCritical(false);
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 0.8f, 1.2f);
        }
    }

    public boolean handleProjectileImpact(Projectile projectile, org.bukkit.Location location, Entity shooterEntity,
        Entity target) {
        if (!Items.checkForProjectile(projectile, TAG))
            return false;
        if (!(shooterEntity instanceof Player shooter))
            return false;

        GrappleBowConfig grapple = config.getGrappleBow();
        Vector pull = location.toVector().subtract(shooter.getLocation().toVector());
        if (target != null) {
            pull = target.getLocation().toVector().subtract(shooter.getLocation().toVector());
        }

        if (pull.lengthSquared() > 1.0E-6) {
            Vector velocity = pull.normalize().multiply(grapple.getPullStrength());
            velocity.setY(velocity.getY() + grapple.getVerticalBoost());
            double maxSpeed = Math.max(0.1, grapple.getMaxSpeed());
            if (velocity.length() > maxSpeed) {
                velocity.normalize().multiply(maxSpeed);
            }
            shooter.setVelocity(velocity);
            shooter.setFallDistance(0);
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f, 1.1f);
        }

        projectile.remove();
        return true;
    }

    private int getRemainingCooldownSeconds(Player player) {
        return (int) Math.ceil(player.getCooldown(Material.BOW) / 20.0);
    }

    private void setCooldown(Player player, GrappleBowConfig grapple) {
        player.setCooldown(Material.BOW, Math.toIntExact(grapple.getCooldown() * 20L));
    }

    public ItemStack createItem(int count) {
        var bow = new ItemStack(Material.BOW);
        Items.addTag(bow, TAG);
        ItemMeta meta = bow.getItemMeta();
        meta.displayName(
            Component.text("Grapple Bow").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.INFINITY, 1, true);
        meta.setEnchantmentGlintOverride(true);
        bow.setItemMeta(meta);
        bow.setAmount(count);
        return bow;
    }
}
