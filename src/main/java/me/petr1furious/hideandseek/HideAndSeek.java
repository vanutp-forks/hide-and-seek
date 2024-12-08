package me.petr1furious.hideandseek;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;

public class HideAndSeek extends JavaPlugin implements Listener {

    private CommandHandler commandHandler;

    private Random random = new Random();

    private GameStatus gameStatus = GameStatus.NOT_STARTED;
    private boolean gameTeleport = true;

    private boolean checkingGameEnd = false;

    private GameConfig gameConfig;

    private LiftHandler liftHandler;

    private int updateDistancesTaskID;

    private final Map<Player, Long> himarsCooldown = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameConfig = new GameConfig(getConfig());
        commandHandler = new CommandHandler(this);
        commandHandler.registerCommands();
        liftHandler = new LiftHandler();
        registerEvents();
    }

    @Override
    public void onDisable() {
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    boolean isPlayerInGame(Player player) {
        return gameStatus == GameStatus.RUNNING && player.getGameMode() != GameMode.SPECTATOR
            && player.getGameMode() != GameMode.CREATIVE;
    }

    boolean getGameTeleport() {
        return gameTeleport;
    }

    void addPlayerToGame(Player player, boolean teleport) {
        if (teleport) {
            boolean success = false;
            for (int i = 0; i < 100; i++) {
                Location location = Utils.getFirstSolidBlock(getRandomLocationInSphere());
                if (location != null) {
                    player.teleport(location.add(0.5, 0, 0.5));
                    success = true;
                    break;
                }
            }
            if (!success) {
                Utils.teleportPlayerOnBlock(player);
            }
        } else {
            Utils.teleportPlayerOnBlock(player);
        }
        player.setGameMode(GameMode.SURVIVAL);
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY,
            Integer.MAX_VALUE, 1, false, false));
        player.setHealth(20);
        player.setFoodLevel(20);
        if (this.gameStatus == GameStatus.ENDED) {
            this.gameStatus = GameStatus.RUNNING;
        }
        if (gameConfig.isEnableGameInventory()) {
            loadGameInventory(player);
        }
    }

    void startGame(boolean teleport) {
        gameStatus = GameStatus.RUNNING;
        gameTeleport = teleport;
        checkingGameEnd = false;
        getServer().sendMessage(Component.text("Starting game").color(NamedTextColor.GREEN));

        for (var player : getServer().getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                addPlayerToGame(player, teleport);
            }
        }

        getServer().getScheduler().cancelTask(updateDistancesTaskID);
        updateDistancesTaskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            updateDistances();
        }, 0, 80);
    }

    void stopGame() {
        getServer().sendMessage(Component.text("Stopping game").color(NamedTextColor.RED));
        resetGame();
    }

    void resetPlayer(Player player) {
        var manager = Bukkit.getScoreboardManager();
        player.setScoreboard(manager.getMainScoreboard());
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            Utils.teleportPlayerOnBlock(player);
        }
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY);
    }

    void resetGame() {
        gameStatus = GameStatus.NOT_STARTED;

        getServer().getScheduler().cancelTask(updateDistancesTaskID);

        for (var player : getServer().getOnlinePlayers()) {
            resetPlayer(player);
        }
    }

    void endGame() {
        gameStatus = GameStatus.ENDED;
    }

    void checkGameEnd() {
        checkingGameEnd = false;

        var playersInGame = 0;
        Player lastPlayer = null;
        for (var player : getServer().getOnlinePlayers()) {
            if (isPlayerInGame(player)) {
                playersInGame++;
                lastPlayer = player;
            }
        }

        if (playersInGame <= 1) {
            if (lastPlayer == null) {
                getServer().broadcast(Component.text("Game over! No players left!").color(NamedTextColor.RED));
            } else {
                getServer().broadcast(Component.text("Game over! ").color(NamedTextColor.RED)
                    .append(Component.text(lastPlayer.getName()).color(NamedTextColor.BLUE))
                    .append(Component.text(" wins!").color(NamedTextColor.RED)));
            }
            endGame();
        }
    }

    void registerEvents() {
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerDeath(PlayerDeathEvent event) {
                if (gameStatus != GameStatus.RUNNING) {
                    return;
                }

                Player player = event.getPlayer();
                event.setCancelled(isPlayerInGame(player));
                getServer().broadcast(event.deathMessage().color(NamedTextColor.GRAY));
                player.setGameMode(GameMode.SPECTATOR);

                if (!checkingGameEnd) {
                    checkingGameEnd = true;
                    getServer().getScheduler().scheduleSyncDelayedTask(HideAndSeek.this, () -> {
                        checkGameEnd();
                    }, 60);
                }
            }

            @EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                if (gameStatus != GameStatus.RUNNING) {
                    return;
                }

                getServer().getScheduler().scheduleSyncDelayedTask(HideAndSeek.this, () -> {
                    updateDistances();
                }, 1);
            }

            @EventHandler
            public void onProjectileHit(ProjectileHitEvent event) {
                Projectile projectile = event.getEntity();
                var shooter = Utils.getEntityShooter(projectile);

                event.setCancelled(true);

                Location location;
                if (event.getHitBlock() != null && event.getHitBlockFace() != null) {
                    Vector projectileStart = projectile.getLocation().toVector();
                    Vector projectileDirection = projectile.getVelocity().normalize();

                    Block hitBlock = event.getHitBlock();
                    BlockFace hitFace = event.getHitBlockFace();
                    Vector planeNormal = hitFace.getDirection();
                    Vector planePoint = hitBlock.getLocation().toVector()
                        .add(new Vector(0.5, 0.5, 0.5))
                        .add(planeNormal.multiply(0.501));

                    double denominator = planeNormal.dot(projectileDirection);
                    if (Math.abs(denominator) > 1e-6) {
                        double t = planeNormal.dot(planePoint.subtract(projectileStart)) / denominator;
                        Vector intersection = projectileStart.add(projectileDirection.multiply(t));
                        location = intersection.toLocation(projectile.getWorld());
                    } else {
                        location = projectile.getLocation();
                    }
                } else if (event.getHitEntity() != null) {
                    location = event.getHitEntity().getLocation();
                } else {
                    location = projectile.getLocation();
                }

                if (projectile instanceof Arrow arrow) {
                    handleProjectileHitLocation(arrow, location, shooter, event.getHitEntity());
                } else if (projectile instanceof Firework firework) {
                    if (firework.hasMetadata("himars_firework")) {
                        Utils.spawnExplosion(location, gameConfig.getHimarsExplosionPower(), shooter);
                        firework.remove();
                    }
                }
            }

            @EventHandler
            public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
                Items.interactWithInfiniteCrossbow(event, gameConfig.getMaxLoadedCrossbowProjectiles());
                Items.interactWithHimars(event);
            }

            @EventHandler
            public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
                liftHandler.handleLift(event.getPlayer(), gameConfig.getLiftMaterial(), gameConfig.isEnableLifts());
            }

            @EventHandler
            public void onArrowShoot(EntityShootBowEvent event) {
                if (event.getBow() == null)
                    return;
                ItemStack bow = event.getBow();
                if (bow.getItemMeta().hasCustomModelData() && bow.getType() == Material.CROSSBOW) {
                    int cmd = bow.getItemMeta().getCustomModelData();
                    if (event.getProjectile() instanceof Arrow arrow) {
                        if (cmd == 1) {
                            arrow.getPersistentDataContainer().set(Items.ARROW_TYPE_KEY, PersistentDataType.STRING,
                                Items.INFINITE_TAG);
                        }
                        if (cmd == 2) {
                            arrow.getPersistentDataContainer().set(Items.ARROW_TYPE_KEY, PersistentDataType.STRING,
                                Items.ORESHNIK_INITIAL_TAG);
                        }
                    }
                    if (event.getProjectile() instanceof Firework firework) {
                        if (cmd == 3) {
                            Player shooter = (Player) event.getEntity();
                            if (isOnCooldown(shooter)) {
                                shooter
                                    .sendActionBar(Component.text("HIMARS is on cooldown!").color(NamedTextColor.RED));
                                event.setCancelled(true);
                                return;
                            }
                            setCooldown(shooter);
                            firework.setVelocity(firework.getVelocity().multiply(gameConfig.getHimarsFireworkSpeed()));
                            firework.setMetadata("himars_firework", new FixedMetadataValue(HideAndSeek.this, true));
                            
                            FireworkMeta meta = firework.getFireworkMeta();
                            int desiredPower = 20;
                            meta.setPower(desiredPower);
                            firework.setFireworkMeta(meta);
                        }
                    }
                }
            }
        }, this);
    }

    void handleProjectileHitLocation(Arrow arrow, Location location, Entity shooter, Entity target) {
        String arrowType = arrow.getPersistentDataContainer().get(Items.ARROW_TYPE_KEY, PersistentDataType.STRING);

        if (Items.INFINITE_TAG.equals(arrowType) || Items.ORESHNIK_TAG.equals(arrowType)
            || Items.HIMARS_TAG.equals(arrowType)) {
            if (gameConfig.isEnableExplosions()) {
                if (Items.ORESHNIK_TAG.equals(arrowType)) {
                    Utils.spawnExplosion(location, gameConfig.getOreshnikExplosionPower(), shooter);
                } else if (Items.HIMARS_TAG.equals(arrowType)) {
                    Utils.spawnExplosion(location, gameConfig.getHimarsExplosionPower(), shooter);
                } else {
                    Utils.spawnExplosion(location, gameConfig.getExplosionPower(), shooter);
                }
                if (target != null) {
                    Utils.killWithExplosion(target, shooter);
                }

                arrow.remove();
            }
        }

        if (Items.ORESHNIK_INITIAL_TAG.equals(arrowType)) {
            spawnArrowWaves(location, gameConfig.getOreshnikWavesCount(), gameConfig.getOreshnikArrowsCount());

            arrow.remove();
        }
    }

    private boolean isOnCooldown(Player player) {
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return false;
        }
        if (!himarsCooldown.containsKey(player)) {
            return false;
        }
        long lastUsed = himarsCooldown.get(player);
        return (System.currentTimeMillis() - lastUsed) < gameConfig.getHimarsCooldown() * 1000;
    }

    private void setCooldown(Player player) {
        himarsCooldown.put(player, System.currentTimeMillis());
        new BukkitRunnable() {
            @Override
            public void run() {
                himarsCooldown.remove(player);
            }
        }.runTaskLater(this, gameConfig.getHimarsCooldown() * 20);
    }

    void updateDistances() {
        if (gameStatus != GameStatus.RUNNING) {
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();

        for (Player player1 : getServer().getOnlinePlayers()) {
            Scoreboard scoreboard = manager.getNewScoreboard();
            player1.setScoreboard(scoreboard);
            for (String entry : scoreboard.getEntries()) {
                scoreboard.resetScores(entry);
            }

            Objective playerObjective = scoreboard.getObjective(player1.getName());
            if (playerObjective == null) {
                playerObjective = scoreboard.registerNewObjective(player1.getName(), Criteria.DUMMY,
                    Component.text("Distances").color(NamedTextColor.GOLD));
                playerObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            for (Player player2 : getServer().getOnlinePlayers()) {
                if (player1 == player2 || !isPlayerInGame(player2))
                    continue;

                double distance = player1.getLocation().distance(player2.getLocation());
                int roundedDistance = (int) (Math.round(distance / 50.0) * 50);
                String distanceRange = getDistanceRange(roundedDistance);
                playerObjective.getScore(ChatColor.GOLD + distanceRange + ChatColor.AQUA + " " + player2.getName())
                    .setScore(roundedDistance);
            }
        }
    }

    String getDistanceRange(int distance) {
        int lowerBound = (distance / 50) * 50;
        int upperBound = lowerBound + 50;
        return lowerBound + "-" + upperBound;
    }

    Location getRandomLocationInCube() {
        Vector gameCenter = gameConfig.getGameCenter();
        double gameRadius = gameConfig.getGameRadius();
        double x = gameCenter.getX() + (random.nextDouble() * 2 - 1) * gameRadius;
        double y = gameCenter.getY() + (random.nextDouble() * 2 - 1) * gameRadius;
        double z = gameCenter.getZ() + (random.nextDouble() * 2 - 1) * gameRadius;

        List<World> worlds = Bukkit.getWorlds();
        World world = worlds.get(0);
        for (World w : worlds) {
            if (w.getName().equals(gameConfig.getGameWorld())) {
                world = w;
                break;
            }
        }

        return new Location(world, x, y, z);
    }

    Location getRandomLocationInSphere() {
        Vector gameCenter = gameConfig.getGameCenter();
        double gameRadius = gameConfig.getGameRadius();
        while (true) {
            Location location = getRandomLocationInCube();
            if (location.distance(gameCenter.toLocation(location.getWorld())) <= gameRadius
                && location.getY() >= location.getWorld().getMinHeight()
                && location.getY() <= location.getWorld().getMaxHeight()) {
                return location;
            }
        }
    }

    public void spawnArrowWaves(Location center, int wavesCount, int arrowsCount) {
        double spawnHeight = 500;

        for (int wave = 0; wave < wavesCount; wave++) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                for (int i = 0; i < arrowsCount; i++) {
                    Location arrowLoc = center.clone().add(0, spawnHeight, 0);
                    arrowLoc.getWorld().spawn(arrowLoc, Arrow.class, spawnedArrow -> {
                        spawnedArrow.getPersistentDataContainer().set(Items.ARROW_TYPE_KEY, PersistentDataType.STRING,
                            "oreshnik");

                        double oreshnikRange = gameConfig.getOreshnikRange();
                        double randomX;
                        double randomZ;
                        do {
                            randomX = (random.nextDouble() * 2 - 1) * oreshnikRange;
                            randomZ = (random.nextDouble() * 2 - 1) * oreshnikRange;
                        } while (randomX * randomX + randomZ * randomZ > oreshnikRange * oreshnikRange);
                        spawnedArrow.setVelocity(
                            spawnedArrow.getVelocity().add(new org.bukkit.util.Vector(randomX, -10.0, randomZ)));
                        spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                        spawnedArrow.setFireTicks(Integer.MAX_VALUE);
                    });
                }
            }, wave * gameConfig.getOreshnikWavesDelay());
        }
    }

    public void saveGameInventory(Player player) {
        getGameConfig().setGameInventory(player.getInventory().getContents());
    }

    public void loadGameInventory(Player player) {
        if (getGameConfig().getGameInventory() != null) {
            player.getInventory().clear();
            player.getInventory().setContents(getGameConfig().getGameInventory());
        }
    }
}
