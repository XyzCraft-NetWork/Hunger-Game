package xyzcraft.hungergame.Listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerUseSpecialFood implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {

        if (event.getAction().toString().contains("RIGHT_CLICK")) {
            Player player = event.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();


            if (itemInHand.getType() == Material.MUSHROOM_STEW) {

                if (itemInHand.getAmount() > 0) {

                    itemInHand.setAmount(itemInHand.getAmount() - 1);

                    player.setHealth(Math.min(player.getHealth() + 8, 20));
                    player.sendMessage("你吃了一碗蘑菇汤，恢复了8点生命！");
                }
            }


            if (itemInHand.getType() == Material.SUSPICIOUS_STEW) {

                if (itemInHand.getAmount() > 0) {

                    itemInHand.setAmount(itemInHand.getAmount() - 1);


                    applyRandomEffect(player);
                }
            }
        }
    }


    private void applyRandomEffect(Player player) {
        int effectType = random.nextInt(6);
        switch (effectType) {
            case 0 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));
                player.sendMessage("你获得了瞬间恢复 II!");
            }
            case 1 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                player.sendMessage("你获得了速度 II，持续30秒！");
            }
            case 2 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 0));
                player.sendMessage("你获得了生命回复 I，持续15秒！");
            }
            case 3 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0));
                player.sendMessage("你获得了抗火 I，持续30秒！");
            }
            case 4 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 0));
                player.sendMessage("你获得了瞬间恢复 I!");
            }
            case 5 -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 300, 0));
                player.sendMessage("你获得了抗性提升 I，持续15秒！");
            }
            default -> {
            }
        }
    }
}
