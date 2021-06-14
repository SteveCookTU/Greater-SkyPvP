package me.ezpzstreamz.skypvp.commands;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CmdArena implements TabExecutor {

    public GreaterSkyPvpPlugin plugin;

    public CmdArena(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String l, String[] a) {
        if(a.length == 1) {
            if(s instanceof Player) {
                if(s.hasPermission("greaterskypvp.default")) {
                    Player p = (Player) s;
                    String arenaName = a[0];
                    if(plugin.getArenaManager().isArenaSetup(arenaName)) {
                        p.teleport(plugin.getArenaManager().getArena(arenaName).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        plugin.getArenaManager().addPlayerToQueue(arenaName, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaJoin").replaceAll("%arena%", arenaName)));
                    } else {
                        s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaNotSetup")));
                    }
                } else {
                    s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
                }
                return true;
            }
            s.sendMessage("[SkyPvP] You must be a player to run this command.");
            return true;
        } else if(a.length == 2) {
            if(s.hasPermission("greaterskypvp.admin")) {
                String arenaName = a[0];
                String playerName = a[1];
                Player p = plugin.getServer().getPlayer(playerName);
                if(p != null) {
                    if(plugin.getArenaManager().isArenaSetup(arenaName)) {
                        p.teleport(plugin.getArenaManager().getArena(arenaName).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                        plugin.getArenaManager().addPlayerToQueue(arenaName, p);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaJoin").replaceAll("%arena%", arenaName)));
                        s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaSend").replaceAll("%arena%", arenaName).replaceAll("%player%", p.getDisplayName())));
                    } else {
                        s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaNotSetup")));
                    }
                }
            } else {
                s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        if(strings.length == 1) {
            if(commandSender.hasPermission("greaterskypvp.default"))
                completions = plugin.getArenaManager().getArenas();
        } else if(strings.length == 2) {
            if(commandSender.hasPermission("greaterskypvp.admin")) {
                for(Player p : commandSender.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
