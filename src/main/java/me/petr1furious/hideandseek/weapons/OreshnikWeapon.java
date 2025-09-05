package me.petr1furious.hideandseek.weapons;

import me.petr1furious.hideandseek.GameConfig;
import me.petr1furious.hideandseek.Items;
import me.petr1furious.hideandseek.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

public class OreshnikWeapon {
    private final GameConfig config;
    private final Random random = new Random();
    private final Map<Player, Long> cooldown = new HashMap<>();

    private final String TAG = "oreshnik";

    public OreshnikWeapon(GameConfig config) {
        this.config = config;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Items.isRightClick(event))
            return;
        if (!Items.checkForItem(event.getItem(), TAG))
            return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR)
            return;
        event.setCancelled(true);
        if (isOnCooldown(player))
            return;
        setCooldown(player);
        var targetBlock = player.getTargetBlockExact(256);
        if (targetBlock == null) {
            player.sendActionBar(Component.text("Too far").color(NamedTextColor.RED));
            return;
        }
        spawnArrowWaves(targetBlock.getLocation(), config.getOreshnik().getWavesCount(),
            config.getOreshnik().getArrowsCount(), player);
        if (player.getGameMode() != GameMode.CREATIVE && event.getHand() != null) {
            ItemStack item = player.getInventory().getItem(event.getHand());
            if (item != null)
                item.setAmount(item.getAmount() - 1);
        }
    }

    public boolean handleProjectileImpact(Projectile projectile, Location location, Entity shooter, Entity target) {
        if (!Items.checkForProjectile(projectile, TAG))
            return false;
        Utils.spawnExplosion(location, config.getOreshnik().getExplosionPower(), shooter);
        projectile.remove();
        return true;
    }

    private boolean isOnCooldown(Player player) {
        Long last = cooldown.get(player);
        return last != null && System.currentTimeMillis() - last < 1000; // 1s
    }

    private void setCooldown(Player player) {
        cooldown.put(player, System.currentTimeMillis());
        Bukkit.getScheduler().runTaskLater(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
            () -> cooldown.remove(player), 20);
    }

    private void spawnArrowWaves(Location center, int wavesCount, int arrowsCount, ProjectileSource shooter) {
        double spawnHeight = 500;
        for (int wave = 0; wave < wavesCount; wave++) {
            int delay = wave * config.getOreshnik().getWavesDelay();
            Bukkit.getScheduler().runTaskLater(org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()), () -> {
                for (int i = 0; i < arrowsCount; i++) {
                    Location arrowLoc = center.clone().add(0, spawnHeight, 0);
                    arrowLoc.getWorld().spawn(arrowLoc, Arrow.class, spawnedArrow -> {
                        Items.setProjectileTag(spawnedArrow, TAG);
                        double range = config.getOreshnik().getRange();
                        double randomX;
                        double randomZ;
                        do {
                            randomX = (random.nextDouble() * 2 - 1) * range;
                            randomZ = (random.nextDouble() * 2 - 1) * range;
                        } while (randomX * randomX + randomZ * randomZ > range * range);
                        spawnedArrow.setVelocity(spawnedArrow.getVelocity().add(new Vector(randomX, -10.0, randomZ)));
                        spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        spawnedArrow.setFireTicks(Integer.MAX_VALUE);
                        spawnedArrow.setShooter(shooter);
                    });
                }
            }, delay);
        }
    }

    public ItemStack createItem(int count) {
        var item = new ItemStack(Material.REPEATER);
        Items.addTag(item, TAG);
        var meta = item.getItemMeta();
        meta.displayName(
            Component.text("ОРЕШНИК").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        item.setAmount(count);
        return item;
    }
}
