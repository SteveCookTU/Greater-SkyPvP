package me.ezpzstreamz.skypvp.commands;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CmdSkyPvp implements TabExecutor {

    private final GreaterSkyPvpPlugin plugin;

    public CmdSkyPvp(GreaterSkyPvpPlugin p) {
plugin = p;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String l, String[] a) {
        if(s.hasPermission("greaterskypvp.admin")) {
            if (a.length < 3)
                return false;

            if (a[0].equalsIgnoreCase("kit")) {
                if (a[1].equalsIgnoreCase("create")) {
                    if (plugin.getKitManager().containsKit(a[2])) {
                        s.sendMessage("[SkyPvP] The kit specified already exists. Use \"/skypvp kit delete " + a[2] + "\" and re-create it.");
                    }
                    if (s instanceof Player) {
                        Player p = (Player) s;
                        String kitName = a[2];
                        boolean replace = true;
                        boolean equip = true;
                        if (a.length > 3)
                            replace = a[3].equalsIgnoreCase("true");
                        if (a.length > 4)
                            equip = a[4].equalsIgnoreCase("true");
                        plugin.getKitManager().addKit(kitName, p.getInventory(), replace, equip);
                        s.sendMessage("[SkyPvP] " + kitName + " kit has been created with your current inventory.");
                        return true;
                    }
                    s.sendMessage("[SkyPvP] You must be a player to run this command.");
                    return true;
                } else if (a[1].equalsIgnoreCase("delete")) {
                    String kitName = a[2];
                    plugin.getKitManager().deleteKit(kitName);
                    s.sendMessage("[SkyPvP] " + kitName + " kit has been deleted.");
                }
            } else if (a[0].equalsIgnoreCase("setvoidkill")) {
                String worldName = a[1];
                World world = plugin.getServer().getWorld(worldName);
                if (world != null) {
                    plugin.getWorldManager().setVoidKill(world, a[2].equalsIgnoreCase("true"));
                    if(a[2].equalsIgnoreCase("true")) {
                        s.sendMessage("[SkyPvP] Void will now instantly kill players in world " + worldName);
                    } else {
                        s.sendMessage("[SkyPvP] Void will not instantly kill players in world " + worldName);
                    }

                }
            } else if (a[0].equalsIgnoreCase("arena")) {
                if (a[1].equalsIgnoreCase("create")) {
                    String arenaName = a[2];
                    plugin.getArenaManager().addArena(arenaName);
                    s.sendMessage("[SkyPvP] " + arenaName + " arena has been created.");
                } else if (a[1].equalsIgnoreCase("delete")) {
                    String arenaName = a[2];
                    plugin.getArenaManager().removeArena(arenaName);
                    s.sendMessage("[SkyPvP] " + arenaName + " arena has been deleted.");
                } else if (a.length == 4) {
                    if (s instanceof Player) {
                        if (a[1].equalsIgnoreCase("set")) {
                            String arenaName = a[2];
                            int spawnPoint;
                            try {
                                spawnPoint = Integer.parseInt(a[3]);
                                switch (spawnPoint) {
                                    case 0:
                                        plugin.getArenaManager().setLocation(arenaName, spawnPoint, ((Player) s).getLocation());
                                        s.sendMessage("[SkyPvP] " + arenaName + " lobby spawn point set.");
                                        break;
                                    case 1:
                                        plugin.getArenaManager().setLocation(arenaName, spawnPoint, ((Player) s).getLocation());
                                        s.sendMessage("[SkyPvP] " + arenaName + " player 1 spawn point set.");
                                        break;
                                    case 2:
                                        plugin.getArenaManager().setLocation(arenaName, spawnPoint, ((Player) s).getLocation());
                                        s.sendMessage("[SkyPvP] " + arenaName + " player 2 spawn point set.");
                                        break;
                                    default:
                                        throw new Exception();
                                }
                            } catch (Exception e) {
                                s.sendMessage("[SkyPvP] Selected spawn point invalid. Valid range: 0-2");
                                return true;
                            }
                        }
                    }
                }
            }
        } else {
            s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        if(commandSender.hasPermission("greaterskypvp.admin")) {
            switch (strings.length) {
                case 1:
                    completions.add("kit");
                    completions.add("setvoidkill");
                    completions.add("arena");
                    break;
                case 2:
                    if (strings[0].equalsIgnoreCase("kit")) {
                        completions.add("create");
                        completions.add("delete");
                    } else if (strings[0].equalsIgnoreCase("setvoidkill")) {
                        for (World w : plugin.getServer().getWorlds()) {
                            completions.add(w.getName());
                        }
                    } else if (strings[0].equalsIgnoreCase("arena")) {
                        completions.add("set");
                        completions.add("create");
                        completions.add("delete");
                    }
                    break;
                case 3:
                    if (strings[0].equalsIgnoreCase("setvoidkill")) {
                        completions.add("true");
                        completions.add("false");
                    } else if (strings[0].equalsIgnoreCase("arena") && strings[1].equalsIgnoreCase("set")) {
                        completions = plugin.getArenaManager().getArenas();
                    }
                    break;
                case 4:
                    if (strings[0].equalsIgnoreCase("kit")) {
                        completions.add("true");
                        completions.add("false");
                    } else if (strings[0].equalsIgnoreCase("arena")) {
                        completions.add("0");
                        completions.add("1");
                        completions.add("2");
                    }
                    break;
                case 5:
                    if (strings[0].equalsIgnoreCase("kit")) {
                        completions.add("true");
                        completions.add("false");
                    }
                    break;
            }
        }
        return completions;
    }
}
