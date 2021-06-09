package me.ezpzstreamz.skypvp.commands;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CmdStats implements TabExecutor {

    private final GreaterSkyPvpPlugin plugin;

    public CmdStats(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender.hasPermission("greaterskypvp.default")) {
            if(strings.length == 0) {
                if(commandSender instanceof Player) {
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("statsMessage").replaceAll("%kills%", "" + plugin.getStatManager().getKills(((Player) commandSender), null)).replaceAll("%deaths%", "" + plugin.getStatManager().getDeaths(((Player) commandSender), null))));
                }
                return true;
            } else if(strings.length == 1) {
                Player p = plugin.getServer().getPlayer(strings[0]);
                if(p != null)
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("statsMessage").replaceAll("%kills%", "" + plugin.getStatManager().getKills(p, null)).replaceAll("%deaths%", "" + plugin.getStatManager().getDeaths(p, null))));
                return true;
            }
        } else {
            commandSender.sendMessage(plugin.getMessageManager().getMessage("noPermission"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        if(strings.length == 0) {
            if(commandSender.hasPermission("greaterskypvp.default")) {
                for(Player p : commandSender.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
