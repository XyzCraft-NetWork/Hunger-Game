package xyzcraft.hungergame.Managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import xyzcraft.hungergame.main;

public class WorldBorderManager {
    private final World world;
    private final int initialRadius;
    private final int targetRadius;
    private final int duration;
    private double currentRadius;
    private int tickCount = 0;

    public WorldBorderManager(World world, int initialRadius, int targetRadius, int duration) {
        this.world = world;
        this.initialRadius = initialRadius;
        this.targetRadius = targetRadius;
        this.duration = duration;
        this.currentRadius = this.initialRadius;
    }


    public void startShrinking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (tickCount >= duration) {
                    this.cancel();
                    return;
                }


                double radiusStep = (initialRadius - targetRadius) / (double) duration;


                currentRadius -= radiusStep;


                world.getWorldBorder().setCenter(main.info_config.getDouble("original_pos.x") + 0.5, main.info_config.getDouble("original_pos.z") + 0.5);
                world.getWorldBorder().setSize(currentRadius);


                tickCount++;


            }
        }.runTaskTimer(main.main, 0L, 20L);
    }
}
