package xyzcraft.hungergame.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyzcraft.hungergame.Managers.GameManager;

public class test implements CommandExecutor {

    @Override
    @SuppressWarnings("all")
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 1) {
            return false;
        }
        try {
            GameManager.eventTime = Integer.parseInt(strings[0]);

        } catch (NumberFormatException ignore) {
            commandSender.sendMessage("&cUnexpected argument: " + strings[0]);
        }
        return false;
    }
}
