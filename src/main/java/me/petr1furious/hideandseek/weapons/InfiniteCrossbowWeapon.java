package me.petr1furious.hideandseek.weapons;

import me.petr1furious.hideandseek.GameConfig;
import me.petr1furious.hideandseek.Items;
import me.petr1furious.hideandseek.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.ArrayList;
import java.util.List;

public class InfiniteCrossbowWeapon {
    private final GameConfig config;

    private final String TAG = "infinite_crossbow";

    public InfiniteCrossbowWeapon(GameConfig config) {
        this.config = config;
    }

    public void onPlayerInteract(PlayerInteractEvent event) {
        var item = event.getItem();
        if (!Items.checkForItem(item, TAG))
            return;
        if (!(item.getItemMeta() instanceof CrossbowMeta meta))
            return;
        int count = meta.getChargedProjectiles().size();
        int max = config.getInfiniteCrossbow().getMaxLoadedProjectiles();
        if (count < max) {
            meta.addChargedProjectile(new ItemStack(Material.ARROW));
            setLore(meta, count + 1, max);
            item.setItemMeta(meta);
            if (Items.isLeftClick(event)) {
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2f);
            }
        } else if (Items.isLeftClick(event)) {
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 0.5f);
        }
    }

    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow))
            return;
        var bow = event.getBow();
        if (bow == null)
            return;
        if (Items.checkForItem(bow, TAG)) {
            Items.setProjectileTag(arrow, TAG);
        }
    }

    public boolean handleProjectileImpact(Projectile projectile, org.bukkit.Location location, Entity shooter,
        Entity target) {
        if (!Items.checkForProjectile(projectile, TAG))
            return false;
        if (!config.getInfiniteCrossbow().isEnableExplosions())
            return false;
        Utils.spawnExplosion(location, config.getInfiniteCrossbow().getExplosionPower(), shooter);
        if (target != null)
            Utils.killWithExplosion(target, shooter);
        projectile.remove();
        return true;
    }

    public ItemStack createItem(int count) {
        var crossbow = new ItemStack(Material.CROSSBOW);
        Items.addTag(crossbow, TAG);
        var meta = crossbow.getItemMeta();
        meta.displayName(
            Component.text("Infinite Crossbow").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);
        crossbow.setItemMeta(meta);
        var cmeta = (CrossbowMeta) crossbow.getItemMeta();
        cmeta.addChargedProjectile(new ItemStack(Material.ARROW));
        setLore(cmeta, 1, config.getInfiniteCrossbow().getMaxLoadedProjectiles());
        crossbow.setItemMeta(cmeta);
        crossbow.setAmount(count);
        return crossbow;
    }

    private void setLore(CrossbowMeta meta, int projectiles, int max) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Crossbow with infinite ammo").color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        if (max > 1) {
            lore.add(Component.text("Loaded: " + projectiles + "/" + max).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
    }
}
