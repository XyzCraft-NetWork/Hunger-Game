package xyzcraft.hungergame.Utils;

import org.bukkit.ChatColor;

public class Chat {
    public static String parseColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
