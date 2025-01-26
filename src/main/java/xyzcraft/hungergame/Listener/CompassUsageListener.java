package xyzcraft.hungergame.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyzcraft.hungergame.Utils.Chat;
import xyzcraft.hungergame.main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CompassUsageListener implements Listener {
    private static final Map<UUID, Integer> compassUses = new HashMap<>();

    @EventHandler
    public void onPlayerUseCompass(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().toString().contains("RIGHT_CLICK") && event.getItem() != null && event.getItem().getType() == Material.COMPASS && event.getHand() == EquipmentSlot.HAND)  {
            ItemStack compass = player.getInventory().getItemInMainHand();

            ItemMeta meta = compass.getItemMeta();
            if (meta == null) {
                return;
            }

            String compassUUIDString = meta.getPersistentDataContainer().get(new NamespacedKey(main.main, "compassUUID"), PersistentDataType.STRING);
            if (compassUUIDString == null) {
                return;
            }

            UUID compassID = UUID.fromString(compassUUIDString);

            int usesLeft = compassUses.getOrDefault(compassID, 5);
            if (usesLeft <= 0) {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                player.sendMessage(Chat.parseColor("&7你的指南针已用完，已被销毁"));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            } else {
                Player nearestPlayer = findNearestPlayer(player);
                if (nearestPlayer != null) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);

                    double distance = player.getLocation().distance(nearestPlayer.getLocation());
                    player.sendMessage("离你最近的玩家是: " + nearestPlayer.getName() + "，距离: " + String.format("%.1f", distance) + " 米");

                    player.setCompassTarget(nearestPlayer.getLocation());
                    player.sendMessage("(已将指南针指向锁定至最近的玩家的位置)");

                    compassUses.put(compassID, usesLeft - 1);
                    player.sendMessage("你还可以使用该指南针 " + (usesLeft - 1) + " 次");

                    meta.getPersistentDataContainer().set(new NamespacedKey(main.main, "compassUses"), PersistentDataType.INTEGER, usesLeft - 1);
                    compass.setItemMeta(meta);
                } else {
                    player.sendMessage("附近没有其他玩家");
                }
            }
        }
    }



    private Player findNearestPlayer(Player player) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;


        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (otherPlayer != player) {
                double distance = player.getLocation().distance(otherPlayer.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = otherPlayer;
                }
            }
        }
        return nearestPlayer;
    }


    public static ItemStack createCompass() {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {

            UUID compassUUID = UUID.randomUUID();
            meta.getPersistentDataContainer().set(new NamespacedKey(main.main, "compassUUID"), PersistentDataType.STRING, compassUUID.toString());
            meta.getPersistentDataContainer().set(new NamespacedKey(main.main, "compassUses"), PersistentDataType.INTEGER, 5);
            meta.setDisplayName(Chat.parseColor("&cPlayer Finder =)"));
            compass.setItemMeta(meta);


            compassUses.put(compassUUID, 5);
        }

        return compass;
    }
}
