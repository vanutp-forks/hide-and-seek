package me.petr1furious.hideandseek;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class Items {
    public static NamespacedKey ARROW_TYPE_KEY = new NamespacedKey("hide_and_seek_items", "arrow_type");

    public static String INFINITE_TAG = "infinite";
    public static String ORESHNIK_TAG = "oreshnik";
    public static String HIMARS_TAG = "himars";

    static void setInfiniteCrossbowMetaLore(ItemMeta meta, int projectiles, int maxProjectiles) {
        List<Component> metaLore = new java.util.ArrayList<>();
        metaLore.add(Component.text("Crossbow with infinite ammo").color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        if (maxProjectiles > 1) {
            metaLore.add(Component.text("Loaded: " + projectiles + "/" + maxProjectiles).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(metaLore);
    }

    static boolean checkForInfiniteCrossbow(org.bukkit.inventory.ItemStack item) {
        if (item == null) {
            return false;
        }
        if (item.getType() == org.bukkit.Material.CROSSBOW && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 1) {
            return true;
        }
        return false;
    }

    static boolean checkForOreshnikItem(ItemStack item) {
        if (item == null)
            return false;
        if (item.getType() == Material.REPEATER && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 2) {
            return true;
        }
        return false;
    }

    public static boolean checkForHimarsCrossbow(ItemStack item) {
        if (item == null)
            return false;
        if (item.getType() == Material.CROSSBOW && item.getItemMeta().hasCustomModelData()
            && item.getItemMeta().getCustomModelData() == 3) {
            return true;
        }
        return false;
    }

    static boolean isLeftClick(PlayerInteractEvent event) {
        return event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_AIR
            || event.getAction() == org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
    }

    static boolean isRightClick(PlayerInteractEvent event) {
        return event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
            || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
    }

    public static void interactWithInfiniteCrossbow(PlayerInteractEvent event, int maxProjectiles) {
        var player = event.getPlayer();

        if (Items.checkForInfiniteCrossbow(event.getItem())) {
            var crossbow = event.getItem();
            var meta = (CrossbowMeta) crossbow.getItemMeta();
            int count = meta.getChargedProjectiles().size();
            if (count < maxProjectiles) {
                meta.addChargedProjectile(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW));
                Items.setInfiniteCrossbowMetaLore(meta, count + 1, maxProjectiles);
                crossbow.setItemMeta(meta);

                if (isLeftClick(event)) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 2f);
                }
            } else {
                if (isLeftClick(event)) {
                    player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BIT, 0.1f, 0.5f);
                }
            }
        }
    }

    public static void interactWithHimars(PlayerInteractEvent event) {
        if (Items.checkForHimarsCrossbow(event.getItem())) {
            var crossbow = event.getItem();
            var meta = (CrossbowMeta) crossbow.getItemMeta();
            int count = meta.getChargedProjectiles().size();
            if (count == 0) {
                meta.addChargedProjectile(new org.bukkit.inventory.ItemStack(org.bukkit.Material.FIREWORK_ROCKET));
                crossbow.setItemMeta(meta);
            }
        }
    }

    public static ItemStack getInfiniteCrossbow(int maxProjectiles) {
        var crossbow = new org.bukkit.inventory.ItemStack(org.bukkit.Material.CROSSBOW);
        var meta = crossbow.getItemMeta();
        meta.displayName(
            Component.text("Infinite Crossbow").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(1);
        meta.setUnbreakable(true);
        setInfiniteCrossbowMetaLore(meta, 1, maxProjectiles);
        meta.setEnchantmentGlintOverride(true);
        var crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ARROW));
        crossbow.setItemMeta(meta);

        return crossbow;
    }

    public static ItemStack getHimars(double fireworkSpeed) {
        var crossbow = new ItemStack(Material.CROSSBOW);
        var meta = crossbow.getItemMeta();
        meta.displayName(Component.text("HIMARS").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(3);
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);
        crossbow.setItemMeta(meta);

        return crossbow;
    }

    public static ItemStack getOreshnik(int wavesCount, int arrowsCount) {
        var repeater = new ItemStack(Material.REPEATER);
        var meta = repeater.getItemMeta();
        meta.displayName(
            Component.text("ОРЕШНИК").color(NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(2);
        meta.setEnchantmentGlintOverride(true);
        repeater.setItemMeta(meta);
        return repeater;
    }
}
