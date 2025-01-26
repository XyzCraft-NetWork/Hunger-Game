package xyzcraft.hungergame.Managers;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import xyzcraft.hungergame.Utils.Chat;
import xyzcraft.hungergame.Utils.PlaceHolderParser;
import xyzcraft.hungergame.main;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

public class ScoreboardManager {

    public final static Map<Player, Integer> playerKillCount = new HashMap<>();
    private final static Map<String, Function<Player, String>> placeholders = new HashMap<>() {{
        put("date", (Player player) -> {
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy");
            return formatter.format(now);
        });
        put("current_event", (player) -> GameManager.nowGameState.getText());
        put("event_change_time", (player) -> formatCountdown(GameManager.nowGameState.getLast_time() - GameManager.eventTime));
        put("alive_player_count", (player) -> GameManager.playersInGame.size() + "");
        put("player_kill_count", player -> playerKillCount.get(player) + "");
        put("position_x", player -> ((int) player.getLocation().getX()) + "");
        put("position_y", player -> ((int) player.getLocation().getY()) + "");
        put("position_z", player -> ((int) player.getLocation().getZ()) + "");
        put("server_ip", player -> main.info_config.getString("server-ip"));
    }};
    public static void setScoreboard(Player player) {
        new PlayerScoreboard(main.main, player, "§6§lHungerGame").startShowing();
    }
    public static String formatCountdown(int seconds) {

        int minutes = seconds / 60;

        int remainingSeconds = seconds % 60;


        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private static class PlayerScoreboard {
        private final Scoreboard scoreboard;
        private final Objective objective;
        private final String title;
        private final Player player;
        private final Plugin plugin;
        /**
         * 用于保存所有的Team
         */
        private final List<Team> contents;
        private boolean isRun;
        private BukkitTask task;

        public PlayerScoreboard(Plugin plugin, Player player, String title) {
            this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
            this.title = title;
            this.objective = scoreboard.registerNewObjective(player.getName(), "dummy", this.title.replace("&", "§"));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            this.player = player;
            this.isRun = false;
            this.plugin = plugin;
            contents = Lists.newArrayList();
        }

        public void startShowing() {

            if (isRun) {
                return;
            }
            if (player == null || !player.isOnline()) {
                return;
            }
            isRun = true;
            player.setScoreboard(scoreboard);
            int max_size = main.scoreboard_contents.size();
            List<String> tempList = Lists.newArrayList();
            for (int i = 0; i <= max_size; i++) {
                tempList.add("§" + ChatColor.values()[i].getChar());
            }

            for (int i = 0; i < max_size; i++) {

                Team regTeam = scoreboard.registerNewTeam("" + i);

                regTeam.addEntry(tempList.get(i));

                objective.getScore(tempList.get(i)).setScore(i);

                contents.add(regTeam);
            }

            task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (!isRun) {
                    return;
                }

                for (int i = 0; i < max_size; i++) {
                    Team content = contents.get(i);
                    content.setPrefix(Chat.parseColor(PlaceHolderParser.parse(tempList.get(i) + "&r" + main.scoreboard_contents.get(i), placeholders, player)));
                }

            }, 0L, 20L);
        }

        public void turnOff() {
            isRun = false;
            task.cancel();
        }
    }
}