package xyzcraft.hungergame;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyzcraft.hungergame.Commands.test;
import xyzcraft.hungergame.Listener.CompassUsageListener;
import xyzcraft.hungergame.Listener.PlayerTalkEvent;
import xyzcraft.hungergame.Listener.PlayerUseSpecialFood;
import xyzcraft.hungergame.Managers.*;
import xyzcraft.hungergame.Utils.Chat;

import java.io.File;
import java.util.*;

public final class main extends JavaPlugin {
    public static main main;
    public static YamlConfiguration info_config;
    public static List<String> scoreboard_contents = new ArrayList<>();

    @Override
    public void onEnable() {
        main = this;
        File file = new File(main.getDataFolder(), "info.yml");
        if (!file.exists()) main.saveResource("info.yml", false);
        info_config = YamlConfiguration.loadConfiguration(file);
        Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
            WorldManager.resetWorld(1);
        }, 20L);
        scoreboard_contents = info_config.getStringList("scoreboard.contents");
        Collections.reverse(scoreboard_contents);
        getServer().getPluginManager().registerEvents(new GameManager(), this);
        getServer().getPluginManager().registerEvents(new ChestManager(), this);
        getServer().getPluginManager().registerEvents(new CompassUsageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerUseSpecialFood(), this);
        getServer().getPluginManager().registerEvents(new PlayerTalkEvent(), this);
        loadChestItems();
        Objects.requireNonNull(getCommand("to")).setExecutor(new test());
        System.out.println("Plugin Enabled!");
        new BukkitRunnable() {

            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.setPlayerListName(Chat.parseColor("&7[&a" + player.getPing() + "&7ms] &r" + player.getName()));
                });
            }
        }.runTaskTimerAsynchronously(this, 0, 20L);
    }
    private void loadChestItems() {
        Objects.requireNonNull(info_config.getConfigurationSection("normal_chest")).getKeys(false).forEach(
                k -> {
                    try {
                        ChestManager.normal_items.add(new ChestManager.ItemWithProbability(info_config.getString("normal_chest." + k + ".item_nbt"), info_config.getDouble("normal_chest." + k + ".percent")));
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        Objects.requireNonNull(info_config.getConfigurationSection("pro_chest")).getKeys(false).forEach(
                k -> {
                    try {
                        ChestManager.pro_items.add(new ChestManager.ItemWithProbability(info_config.getString("pro_chest." + k + ".item_nbt"), info_config.getDouble("pro_chest." + k + ".percent")));
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
