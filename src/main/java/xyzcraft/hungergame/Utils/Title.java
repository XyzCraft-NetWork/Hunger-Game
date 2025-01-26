package xyzcraft.hungergame.Utils;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyzcraft.hungergame.main;

public class Title {
    public static void sendTitle(Player player, EnumTitleType titleType, String text, int fadeIn, int stay, int fadeOut) {
        Packet<?> packet;
        packet = new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut);
        ((CraftPlayer) player).getHandle().b.a(packet);
        packet = switch (titleType) {
            case TITLE -> new ClientboundSetTitleTextPacket(IChatBaseComponent.a(Chat.parseColor(text)));
            case SUBTITLE -> new ClientboundSetSubtitleTextPacket(IChatBaseComponent.a(Chat.parseColor(text)));
            case ACTIONBAR -> new ClientboundSetActionBarTextPacket(IChatBaseComponent.a(Chat.parseColor(text)));
            default -> packet;
        };
        ((CraftPlayer) player).getHandle().b.a(packet);
    }

    public static void sendTitle(Player player, EnumTitleType titleType, String text) {
        sendTitle(player, titleType, text, 10, 70, 20);
    }

    public enum EnumTitleType {
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIME
    }
}
