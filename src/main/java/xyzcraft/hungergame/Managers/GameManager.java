package xyzcraft.hungergame.Managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyzcraft.hungergame.Utils.Chat;
import xyzcraft.hungergame.Utils.Title;
import xyzcraft.hungergame.main;

import java.util.*;

import static xyzcraft.hungergame.Managers.GameManager.GameEventTriggered.*;
import static xyzcraft.hungergame.Utils.Random.getRandomMessage;


public class GameManager implements Listener {
    public final static Set<UUID> playersInGame = new HashSet<>();
    public static int eventTime = 0;
    public static GameEventState nowGameState = GameEventState.PVP;
    private static boolean gameRunning = true;
    private static boolean canPvP = false;
    private final Map<UUID, Location> playerSpawns = new HashMap<>();
    private final Map<Player, Location> playerRespawnLocations = new HashMap<>();
    private GameState currentGameState = GameState.PREPARED;
    private BukkitRunnable countdownTask;
    private int countdownTime = 16;

    private static void restartServer() {
        Bukkit.broadcastMessage("服务器将在15秒后重置");
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(main.main, 300L);
    }

    private static void checkEvents() {
        if (eventTime >= GameEventState.PVP.last_time && nowGameState == GameEventState.PVP) {
            startPVP();
        } else if (eventTime >= GameEventState.CHEST_REFILLING.last_time && nowGameState == GameEventState.CHEST_REFILLING) {
            refillChests();
        } else if (eventTime >= GameEventState.LAST_MAN_STANDING_MODE.last_time && nowGameState == GameEventState.LAST_MAN_STANDING_MODE) {
            startLastManStanding();
        } else if (eventTime >= GameEventState.GAME_DRAWING.last_time) {
            gameDraw();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setHealth(20);
        event.getPlayer().setSaturation(20);
        event.getPlayer().getInventory().clear();
        event.getPlayer().getActivePotionEffects().forEach(e -> event.getPlayer().removePotionEffect(e.getType()));
        ScoreboardManager.setScoreboard(event.getPlayer());
        ScoreboardManager.playerKillCount.put(event.getPlayer(), 0);
        event.getPlayer().setPlayerListHeaderFooter(Chat.parseColor(main.info_config.getString("player_list_header")), Chat.parseColor(main.info_config.getString("player_list_footer")));
        Player player = event.getPlayer();
        if (currentGameState == GameState.PREPARED) {

            teleportPlayerToRandomSpawn(player);
            directTo(player);
            player.setGameMode(GameMode.ADVENTURE);
        } else if (currentGameState == GameState.IN_GAME) {

            player.setGameMode(GameMode.SPECTATOR);
        }

        playersInGame.add(player.getUniqueId());
        checkGameStart();
    }

    private void directTo(Player player) {

        Location targetLocation = new Location(player.getWorld(), main.info_config.getDouble("original_pos.x"), main.info_config.getDouble("original_pos.y"), main.info_config.getDouble("original_pos.z")).add(0.5, 0, 0.5);

        Location playerLocation = player.getLocation();


        Vector direction = targetLocation.clone().subtract(playerLocation).toVector().normalize();


        playerLocation.setDirection(direction);


        player.teleport(playerLocation);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (currentGameState == GameState.PREPARED) {

            if (event.getFrom().getX() != Objects.requireNonNull(event.getTo()).getX() || event.getFrom().getZ() != event.getTo().getZ() || event.getFrom().getY() != event.getTo().getY()) {
                event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ(), event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch()));
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playersInGame.remove(event.getPlayer().getUniqueId());
        checkVictory();
        checkGameStart();
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            if (event.getEntity() instanceof Player) {
                playerRespawnLocations.put(((Player) event.getEntity()), event.getEntity().getLocation());
                if (currentGameState == GameState.IN_GAME) {
                    Player player = (Player) event.getEntity();
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage(Chat.parseColor("&7你现在是旁观者模式"));
                    playersInGame.remove(player.getUniqueId());
                    String randomMessage;
                    if (event.getEntity().getKiller() != null) {
                        randomMessage = getRandomMessage(main.info_config.getStringList("player_death.player_killed")).replace("%player_death%", event.getEntity().getName()).replace("%player_killer%", event.getEntity().getKiller().getName());
                        Bukkit.broadcastMessage(Chat.parseColor(randomMessage));
                        ScoreboardManager.playerKillCount.put(event.getEntity().getKiller(), ScoreboardManager.playerKillCount.get(event.getEntity().getKiller()));
                    } else {
                        randomMessage = getRandomMessage(main.info_config.getStringList("player_death.player_natural")).replace("%player_death%", event.getEntity().getName());
                        Bukkit.broadcastMessage(Chat.parseColor(randomMessage));
                    }
                }
            }
            checkVictory();
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        e.setRespawnLocation(playerRespawnLocations.get(e.getPlayer()));
    }

    private void teleportPlayerToRandomSpawn(Player player) {

        List<Location> availableSpawns = new ArrayList<>();
        Objects.requireNonNull(main.info_config.getConfigurationSection("player_spawn_position")).getKeys(false).forEach(
                k -> availableSpawns.add(new Location(Bukkit.getWorld("1"), main.info_config.getDouble("player_spawn_position." + k + ".x"), main.info_config.getDouble("player_spawn_position." + k + ".y"), main.info_config.getDouble("player_spawn_position." + k + ".z")).add(0.5, 0, 0.5))
        );
        for (UUID uuid : playersInGame) {
            Player existingPlayer = Bukkit.getPlayer(uuid);
            if (existingPlayer != null && playerSpawns.containsKey(uuid)) {
                availableSpawns.remove(playerSpawns.get(uuid));
            }
        }

        if (!availableSpawns.isEmpty()) {
            Location randomSpawn = availableSpawns.get(new Random().nextInt(availableSpawns.size()));
            player.teleport(randomSpawn);
            playerSpawns.put(player.getUniqueId(), randomSpawn);
        }
    }

    private void checkGameStart() {
        int player_count_max_triggered_game = main.info_config.getInt("min_player_count");
        final boolean[] tip_cancel_game = {false};
        if (playersInGame.size() >= player_count_max_triggered_game && currentGameState == GameState.PREPARED) {
            if (countdownTask != null && !countdownTask.isCancelled()) {
                countdownTask.cancel();
            }
            countdownTask = new BukkitRunnable() {
                @Override
                public void run() {
                    countdownTime--;
                    if (countdownTime <= 0) {
                        startGame();
                        cancel();
                    } else {
                        if (playersInGame.size() >= player_count_max_triggered_game) {
                            Bukkit.broadcastMessage(Chat.parseColor("&7游戏将在 " + countdownTime + " 秒后开始"));
                            Bukkit.getOnlinePlayers().forEach(p -> {
                                Title.sendTitle(p, Title.EnumTitleType.TITLE, countdownTime + "");
                                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                            });
                            tip_cancel_game[0] = true;
                        } else {
                            countdownTime = 16;
                            if (tip_cancel_game[0]) {
                                Bukkit.broadcastMessage(Chat.parseColor("&c游戏取消!人数不足"));
                                tip_cancel_game[0] = false;
                            }
                        }
                    }
                }
            };
            countdownTask.runTaskTimer(main.main, 0L, 20L);
        }
    }

    private void startGame() {
        startGameTimer();
        currentGameState = GameState.IN_GAME;
        Bukkit.broadcastMessage(Chat.parseColor("&c游戏开始!击败你的对手!"));
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 1));
        for (UUID uuid : playersInGame) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.setGameMode(GameMode.ADVENTURE);
            }
        }
        countdownTask.cancel();
    }

    private void checkVictory() {
        if (currentGameState == GameState.IN_GAME) {
            if (playersInGame.size() <= 1) {
                Player winner = Bukkit.getPlayer(playersInGame.iterator().next());
                if (winner != null) {
                    gameRunning = false;
                    Bukkit.broadcastMessage(Chat.parseColor("&e" + winner.getName() + " 获胜!"));
                    Title.sendTitle(winner, Title.EnumTitleType.TITLE, "&e&lVICTORY");
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
                    });
                    restartServer();
                }
            }
        }
    }

    public void startGameTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameRunning) {
                    this.cancel();
                    return;
                }

                eventTime++;


                checkEvents();
            }
        }.runTaskTimer(main.main, 0L, 20L);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
            if (!canPvP) {
                e.setCancelled(true);
                e.getDamager().sendMessage(Chat.parseColor("&cPVP未开启!"));
            }
        }
    }

    public enum GameEventState {
        LAST_MAN_STANDING_MODE("Last Man Standing Mode", 150),
        CHEST_REFILLING("Chest Refilling", 150),
        PVP("PvP Enabling", 10),
        GAME_DRAWING("Game Drawing", 150),
        SERVER_RESTARTING("Game Over", 0);
        private final String text;
        private final int last_time;

        GameEventState(String text, int last_time) {
            this.text = text;
            this.last_time = last_time;
        }

        public String getText() {
            return text;
        }

        public int getLast_time() {
            return last_time;
        }
    }

    public enum GameState {
        PREPARED,
        IN_GAME
    }

    public static class GameEventTriggered {
        public static void gameDraw() {
            eventTime = 0;
            gameRunning = false;
            nowGameState = GameEventState.SERVER_RESTARTING;
            Bukkit.broadcastMessage(Chat.parseColor("&6游戏平局!"));
            Bukkit.getOnlinePlayers().forEach(k -> {
                k.setGameMode(GameMode.SPECTATOR);
                Title.sendTitle(k, Title.EnumTitleType.TITLE, "&e&lVICTORY");
            });
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            });
            restartServer();
        }

        public static void startLastManStanding() {
            eventTime = 0;
            int initial_radius = main.info_config.getInt("last_man_standing_mode.initial_radius");
            int target_radius = main.info_config.getInt("last_man_standing_mode.target_radius");
            int duration_seconds = main.info_config.getInt("last_man_standing_mode.duration_seconds");
            Bukkit.broadcastMessage(Chat.parseColor("&c绝杀模式开启!&r世界边界将会在" + duration_seconds + "秒内从" + initial_radius + "缩小到" + target_radius + "!"));
            World world = Bukkit.getWorld("1");
            WorldBorderManager borderManager = new WorldBorderManager(world, initial_radius, target_radius, duration_seconds);
            borderManager.startShrinking();
            nowGameState = GameEventState.GAME_DRAWING;
        }

        public static void refillChests() {
            eventTime = 0;
            Bukkit.broadcastMessage(Chat.parseColor("&c箱子已重新填充!"));
            ChestManager.chestFilled.clear();
            ChestManager.chestContents.clear();
            nowGameState = GameEventState.LAST_MAN_STANDING_MODE;
        }

        public static void startPVP() {
            eventTime = 0;
            Bukkit.broadcastMessage(Chat.parseColor("&cPVP开启!"));
            GameManager.canPvP = true;
            nowGameState = GameEventState.CHEST_REFILLING;
        }
    }
}
