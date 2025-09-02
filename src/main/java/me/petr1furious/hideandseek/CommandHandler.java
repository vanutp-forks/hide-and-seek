package me.petr1furious.hideandseek;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.petr1furious.hideandseek.weapons.WeaponConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHandler {
    private final HideAndSeek plugin;

    public CommandHandler(HideAndSeek plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        registerCommand("hideandseek");
        registerCommand("hs");
    }

    private void saveGameSettings() {
        plugin.getGameConfig().save();
        plugin.saveConfig();
    }

    void startGameCommand(CommandSender sender) {
        if (plugin.getGameStatus() == GameStatus.RUNNING) {
            sender.sendMessage(Component.text("Game is already running").color(NamedTextColor.RED));
            return;
        }
        plugin.startGame(null);
    }

    void joinGameCommand(CommandSender sender, Player target) {
        if (plugin.getGameStatus() == GameStatus.NOT_STARTED) {
            sender.sendMessage(Component.text("Game is not running").color(NamedTextColor.RED));
            return;
        }
        plugin.addPlayerToGame(target, true);
    }

    void giveItems(List<Player> players, ItemStack item) {
        for (var player : players) {
            player.getInventory().addItem(item);
        }
    }

    private ItemStack createItemStack(String item, GameConfig config, int count) {
        String key = item.toLowerCase();
        if (key.equals("infinite_crossbow")) {
            return plugin.getInfiniteCrossbowWeapon().createItem(count);
        } else if (key.equals("oreshnik")) {
            return plugin.getOreshnikWeapon().createItem(count);
        } else if (key.equals("himars")) {
            return plugin.getHimarsWeapon().createItem(count);
        } else if (key.equals("locator")) {
            return plugin.getLocatorWeapon().createItem(count);
        }
        return null;
    }

    void registerCommand(String name) {
        var manager = plugin.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var commands = event.registrar();
            var rootBuilder = Commands.literal(name)
                .requires(source -> source.getSender().hasPermission("hideandseek.command"))
                .then(Commands.literal("start").executes(ctx -> {
                    startGameCommand(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("targets", ArgumentTypes.players()).executes(ctx -> {
                    if (plugin.getGameStatus() == GameStatus.RUNNING) {
                        ctx.getSource().getSender()
                            .sendMessage(Component.text("Game is already running").color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    List<Player> targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class)
                        .resolve(ctx.getSource());
                    plugin.startGame(targets);
                    return Command.SINGLE_SUCCESS;
                }))).then(Commands.literal("stop").executes(ctx -> {
                    if (plugin.getGameStatus() == GameStatus.NOT_STARTED) {
                        ctx.getSource().getSender()
                            .sendMessage(Component.text("Game is not running").color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.stopGame();
                    return Command.SINGLE_SUCCESS;
                })).then(Commands.literal("restart").executes(ctx -> {
                    if (plugin.getGameStatus() == GameStatus.NOT_STARTED) {
                        ctx.getSource().getSender()
                            .sendMessage(Component.text("Game is not running").color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    plugin.stopGame();
                    startGameCommand(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                })).then(Commands.literal("setcenter").requires(source -> source.getExecutor() instanceof Player)
                    .executes(ctx -> {
                        var player = (Player) ctx.getSource().getExecutor();
                        plugin.getGameConfig().setGameCenter(player.getLocation().toVector());
                        plugin.getGameConfig().setGameWorld(player.getWorld().getName());
                        saveGameSettings();
                        player.sendMessage(
                            Component.text("Game center set to your location").color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    }))
                .then(Commands.literal("setradius")
                    .then(Commands.argument("radius", IntegerArgumentType.integer(1)).executes(ctx -> {
                        int gameRadius = IntegerArgumentType.getInteger(ctx, "radius");
                        plugin.getGameConfig().setGameRadius(gameRadius);
                        saveGameSettings();
                        ctx.getSource().getSender().sendMessage(
                            Component.text("Game radius set to " + gameRadius).color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("setdistanceupdateinterval")
                    .then(Commands.argument("interval", IntegerArgumentType.integer(1)).executes(ctx -> {
                        int interval = IntegerArgumentType.getInteger(ctx, "interval");
                        plugin.getGameConfig().setDistanceUpdateIntervalTicks(interval * 20);
                        saveGameSettings();
                        ctx.getSource().getSender().sendMessage(Component
                            .text("Distance update interval set to " + interval + "s").color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("setdistancegranularity")
                    .then(Commands.argument("granularity", IntegerArgumentType.integer(1)).executes(ctx -> {
                        int granularity = IntegerArgumentType.getInteger(ctx, "granularity");
                        plugin.getGameConfig().setDistanceGranularity(granularity);
                        saveGameSettings();
                        ctx.getSource().getSender().sendMessage(
                            Component.text("Distance granularity set to " + granularity).color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("set")
                    .then(Commands.argument("weapon", StringArgumentType.string()).suggests((ctx, builder) -> {
                        builder.suggest("infinite_crossbow");
                        builder.suggest("ic");
                        builder.suggest("oreshnik");
                        builder.suggest("o");
                        builder.suggest("himars");
                        builder.suggest("h");
                        builder.suggest("locator");
                        builder.suggest("l");
                        return builder.buildFuture();
                    }).then(Commands.argument("property", StringArgumentType.string()).suggests((ctx, builder) -> {
                        String weapon = StringArgumentType.getString(ctx, "weapon");
                        WeaponConfig wc = plugin.getGameConfig().getWeaponConfig(weapon);
                        if (wc != null) {
                            for (String p : wc.getPropertyNames())
                                builder.suggest(p);
                        }
                        return builder.buildFuture();
                    }).then(Commands.argument("value", StringArgumentType.greedyString()).executes(ctx -> {
                        String weapon = StringArgumentType.getString(ctx, "weapon");
                        String property = StringArgumentType.getString(ctx, "property");
                        String value = StringArgumentType.getString(ctx, "value");
                        WeaponConfig wc = plugin.getGameConfig().getWeaponConfig(weapon);
                        if (wc == null) {
                            ctx.getSource().getSender()
                                .sendMessage(Component.text("Unknown weapon").color(NamedTextColor.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        WeaponConfig.WeaponSetResult result = wc.setProperty(property, value);
                        if (result == WeaponConfig.WeaponSetResult.SUCCESS) {
                            plugin.getGameConfig().save();
                            plugin.saveConfig();
                            Object newVal = wc.getPropertyValue(property);
                            ctx.getSource().getSender().sendMessage(Component
                                .text("Set " + weapon + "." + property + " = " + newVal).color(NamedTextColor.GREEN));
                        } else if (result == WeaponConfig.WeaponSetResult.UNKNOWN_PROPERTY) {
                            ctx.getSource().getSender()
                                .sendMessage(Component.text("Unknown property").color(NamedTextColor.RED));
                        } else if (result == WeaponConfig.WeaponSetResult.PARSE_ERROR) {
                            ctx.getSource().getSender()
                                .sendMessage(Component.text("Invalid value").color(NamedTextColor.RED));
                        }
                        return Command.SINGLE_SUCCESS;
                    })))))
                .then(Commands.literal("setlifts")
                    .then(Commands.argument("enable", BoolArgumentType.bool()).executes(ctx -> {
                        boolean enableLifts = BoolArgumentType.getBool(ctx, "enable");
                        plugin.getGameConfig().setEnableLifts(enableLifts);
                        saveGameSettings();
                        ctx.getSource().getSender().sendMessage(Component
                            .text("Lifts " + (enableLifts ? "enabled" : "disabled")).color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("join").requires(source -> source.getExecutor() instanceof Player)
                    .executes(ctx -> {
                        var player = (Player) ctx.getSource().getExecutor();
                        joinGameCommand(player, player);
                        return Command.SINGLE_SUCCESS;
                    }))
                .then(
                    Commands.literal("join").then(Commands.argument("player", ArgumentTypes.player()).executes(ctx -> {
                        var player = (Player) ctx.getSource().getExecutor();
                        Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource()).getFirst();
                        joinGameCommand(player, target);
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("give").then(Commands.argument("players", ArgumentTypes.players())
                    .then(Commands.argument("item", StringArgumentType.string()).suggests((ctx, builder) -> {
                        builder.suggest("infinite_crossbow");
                        builder.suggest("oreshnik");
                        builder.suggest("himars");
                        builder.suggest("locator");
                        return builder.buildFuture();
                    }).then(Commands.argument("count", IntegerArgumentType.integer(1)).executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource());
                        int count = IntegerArgumentType.getInteger(ctx, "count");
                        ItemStack items = createItemStack(item, plugin.getGameConfig(), count);
                        giveItems(players, items);

                        ctx.getSource().getSender()
                            .sendMessage(Component.text("Given the items to players").color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })).executes(ctx -> {
                        String item = StringArgumentType.getString(ctx, "item");
                        List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class)
                            .resolve(ctx.getSource());
                        ItemStack items = createItemStack(item, plugin.getGameConfig(), 1);
                        giveItems(players, items);

                        ctx.getSource().getSender()
                            .sendMessage(Component.text("Given the item to players").color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    }))))
                .then(Commands.literal("reload").executes(ctx -> {
                    plugin.reloadConfig();
                    plugin.getGameConfig().load();
                    ctx.getSource().getSender()
                        .sendMessage(Component.text("Config reloaded").color(NamedTextColor.GREEN));
                    return Command.SINGLE_SUCCESS;
                })).then(Commands.literal("setinventory")
                    .then(Commands.argument("enable", BoolArgumentType.bool()).executes(ctx -> {
                        boolean enableGameInventory = BoolArgumentType.getBool(ctx, "enable");
                        plugin.getGameConfig().setEnableGameInventory(enableGameInventory);
                        saveGameSettings();
                        ctx.getSource().getSender().sendMessage(
                            Component.text("Game inventory " + (enableGameInventory ? "enabled" : "disabled"))
                                .color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })))
                .then(Commands.literal("inventory")
                    .then(Commands.literal("copy").then(Commands.argument("source", ArgumentTypes.player())
                        .then(Commands.argument("targets", ArgumentTypes.players()).executes(ctx -> {
                            var source = ctx.getArgument("source", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource()).getFirst();
                            var targets = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class)
                                .resolve(ctx.getSource());
                            for (var target : targets) {
                                target.getInventory().setContents(source.getInventory().getContents());
                            }
                            ctx.getSource().getSender()
                                .sendMessage(Component.text("Copied inventory").color(NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }))))
                    .then(Commands.literal("save").requires(source -> source.getExecutor() instanceof Player)
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getExecutor();
                            plugin.saveGameInventory(player);
                            plugin.saveConfig();
                            player.sendMessage(Component.text("Game inventory saved").color(NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }))
                    .then(Commands.literal("load").requires(source -> source.getExecutor() instanceof Player)
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getExecutor();
                            plugin.loadGameInventory(player);
                            player.sendMessage(Component.text("Game inventory loaded").color(NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        })))
                .build();
            commands.register(rootBuilder);
        });
    }
}
