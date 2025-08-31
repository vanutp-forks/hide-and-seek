package me.petr1furious.hideandseek;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

import me.petr1furious.hideandseek.weapons.HimarsWeapon;
import me.petr1furious.hideandseek.weapons.InfiniteCrossbowWeapon;
import me.petr1furious.hideandseek.weapons.OreshnikWeapon;

import java.util.Random;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.ArrayList;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HideAndSeek extends JavaPlugin implements Listener {

    private CommandHandler commandHandler;

    private Random random = new Random();

    private GameStatus gameStatus = GameStatus.NOT_STARTED;

    private boolean checkingGameEnd = false;

    private GameConfig gameConfig;

    private LiftHandler liftHandler;

    private int updateDistancesTaskID;

    private InfiniteCrossbowWeapon infiniteCrossbowWeapon;
    private OreshnikWeapon oreshnikWeapon;
    private HimarsWeapon himarsWeapon;

    private final Set<UUID> gamePlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameConfig = new GameConfig(getConfig());
        commandHandler = new CommandHandler(this);
        commandHandler.registerCommands();
        liftHandler = new LiftHandler();
        infiniteCrossbowWeapon = new InfiniteCrossbowWeapon(gameConfig);
        oreshnikWeapon = new OreshnikWeapon(gameConfig);
        himarsWeapon = new HimarsWeapon(gameConfig);
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
        return gameStatus == GameStatus.RUNNING && gamePlayers.contains(player.getUniqueId())
            && player.getGameMode() != GameMode.SPECTATOR && player.getGameMode() != GameMode.CREATIVE
            && player.getWorld().getName().equals(gameConfig.getGameWorld());
    }

    void addPlayerToGame(Player player) {
        gamePlayers.add(player.getUniqueId());
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

    void startGame() {
        startGame(null);
    }

    void startGame(List<Player> participants) {
        gameStatus = GameStatus.RUNNING;
        checkingGameEnd = false;
        getServer().sendMessage(Component.text("Starting game").color(NamedTextColor.GREEN));

        gamePlayers.clear();

        List<Player> chosen;
        if (participants == null || participants.isEmpty()) {
            chosen = new ArrayList<>();
            for (var p : getServer().getOnlinePlayers()) {
                chosen.add(p);
            }
        } else {
            chosen = participants;
        }

        for (Player player : chosen) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                addPlayerToGame(player);
                gamePlayers.add(player.getUniqueId());
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

        for (UUID uuid : gamePlayers) {
            Player player = getServer().getPlayer(uuid);
            if (player != null) {
                resetPlayer(player);
            }
        }
        gamePlayers.clear();
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

                Location location;
                if (event.getHitBlock() != null && event.getHitBlockFace() != null) {
                    Vector projectileStart = projectile.getLocation().toVector();
                    Vector projectileDirection = projectile.getVelocity().normalize();

                    Block hitBlock = event.getHitBlock();
                    BlockFace hitFace = event.getHitBlockFace();
                    Vector planeNormal = hitFace.getDirection();
                    Vector planePoint = hitBlock.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5))
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

                if (handleProjectileHitLocation(projectile, location, shooter, event.getHitEntity())) {
                    event.setCancelled(true);
                }
            }

            @EventHandler
            public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
                infiniteCrossbowWeapon.onPlayerInteract(event);
                himarsWeapon.onPlayerInteract(event);
                oreshnikWeapon.onPlayerInteract(event);
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
                if (bow.getItemMeta().hasCustomModelDataComponent() && bow.getType() == Material.CROSSBOW) {
                    infiniteCrossbowWeapon.onBowShoot(event);
                    himarsWeapon.onBowShoot(event);
                }
            }
        }, this);
    }

    boolean handleProjectileHitLocation(Projectile projectile, Location location, Entity shooter, Entity target) {
        if (infiniteCrossbowWeapon.handleProjectileImpact(projectile, location, shooter, target))
            return true;
        if (oreshnikWeapon.handleProjectileImpact(projectile, location, shooter, target))
            return true;
        if (himarsWeapon.handleProjectileImpact(projectile, location, shooter, target))
            return true;
        return false;
    }

    void updateDistances() {
        if (gameStatus != GameStatus.RUNNING) {
            return;
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();

        for (Player player1 : getServer().getOnlinePlayers()) {
            if (!gamePlayers.contains(player1.getUniqueId()))
                continue;
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
                playerObjective
                    .getScore(Component.text().append(Component.text(distanceRange, NamedTextColor.GOLD))
                        .append(Component.text(" " + player2.getName(), NamedTextColor.AQUA)).build().content())
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

    public InfiniteCrossbowWeapon getInfiniteCrossbowWeapon() {
        return infiniteCrossbowWeapon;
    }

    public OreshnikWeapon getOreshnikWeapon() {
        return oreshnikWeapon;
    }

    public HimarsWeapon getHimarsWeapon() {
        return himarsWeapon;
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
