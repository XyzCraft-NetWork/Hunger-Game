package xyzcraft.hungergame.Listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import xyzcraft.hungergame.Utils.Chat;
import xyzcraft.hungergame.main;

import java.util.Objects;

public class PlayerTalkEvent implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        e.setCancelled(true);
        if (e.getPlayer().isOp()) {
            Bukkit.broadcastMessage(Chat.parseColor(Objects.requireNonNull(main.info_config.getString("player_opped_send_message")).replace("%player%", e.getPlayer().getName()).replace("%message%", e.getMessage())));
        } else {
            Bukkit.broadcastMessage(Chat.parseColor(Objects.requireNonNull(main.info_config.getString("player_send_message")).replace("%player%", e.getPlayer().getName()).replace("%message%", e.getMessage())));

        }

    }
}
