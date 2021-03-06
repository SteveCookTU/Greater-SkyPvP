package me.ezpzstreamz.skypvp.commands;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import me.ezpzstreamz.skypvp.managers.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CmdKit implements TabExecutor {

    private final GreaterSkyPvpPlugin plugin;

    public CmdKit(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command cmd, @NotNull String l, String[] a) {
        if(a.length == 1) {
            if(s instanceof Player) {
                if(s.hasPermission("greaterskypvp.default")) {
                    Player p = (Player) s;
                    if(!plugin.getKitManager().getKits().contains(a[0])) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitDoesNotExist")));
                        return true;
                    }
                    removeTask(p);
                    plugin.getKitManager().giveKit(p, a[0]);
                    plugin.getKitManager().assignKit(p.getUniqueId().toString(), a[0]);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitReceived").replaceAll("%kit%", a[0])));
                } else {
                    s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
                }
                return true;
            }
            s.sendMessage("[SkyPvP] You must be a player to run this command.");
            return true;
        } else if(a.length == 2) {
            if(s.hasPermission("greaterskypvp.admin")) {
                Player p = s.getServer().getPlayer(a[1]);
                if(p != null && p.isOnline()) {
                    if(!plugin.getKitManager().getKits().contains(a[0])) {
                        s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitDoesNotExist")));
                        return true;
                    }
                    removeTask(p);
                    plugin.getKitManager().giveKit(p, a[0]);
                    plugin.getKitManager().assignKit(p.getUniqueId().toString(), a[0]);
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitReceived").replaceAll("%kit%", a[0])));
                    s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitGiven").replaceAll("%kit%", a[0]).replaceAll("%player%", p.getDisplayName())));
                }
            } else {
                s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
            }
            return true;

        } else if(a.length == 3) {
            if(s.hasPermission("greaterskypvp.admin")) {
                int kitTimer;
                try {
                    kitTimer = Integer.parseInt(a[2]);
                    Player p = s.getServer().getPlayer(a[1]);
                    if(p != null && p.isOnline()) {
                        if(!plugin.getKitManager().getKits().contains(a[0])) {
                            s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitDoesNotExist")));
                            return true;
                        }
                        removeTask(p);
                        String prevKit = plugin.getKitManager().getAssignedKitName(p.getUniqueId().toString());
                        ItemStack[] prevItems = p.getInventory().getContents().clone();
                        plugin.getKitManager().giveKit(p, a[0]);
                        plugin.getKitManager().assignKit(p.getUniqueId().toString(), a[0]);
                        plugin.getKitManager().addTask(p, new KitBukkitRunnable(p, prevKit, plugin.getKitManager(), prevItems).runTaskLater(plugin, 20L * kitTimer));
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitReceivedTimed").replaceAll("%kit%", a[0]).replaceAll("%time%", "" + kitTimer)));
                        s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("kitGivenTimed").replaceAll("%kit%", a[0]).replaceAll("%time%", "" + kitTimer).replace("%player%", p.getDisplayName())));
                    }
                } catch (Exception e) {
                    s.sendMessage("[SkyPvP] Please enter a valid time in seconds.");
                }
            } else {
                s.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("noPermission")));
            }
        }
        return true;
    }

    private void removeTask(Player p) {
        if(plugin.getKitManager().playerHasTask(p)) {
            if(Bukkit.getScheduler().isCurrentlyRunning(plugin.getKitManager().getTask(p).getTaskId())) {
                if(!plugin.getKitManager().getTask(p).isCancelled()) {
                    plugin.getKitManager().getTask(p).cancel();
                }
            }
            plugin.getKitManager().removeTask(p);
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        List<String> completions = new ArrayList<>();
        if(strings.length == 1) {
            completions = plugin.getKitManager().getKits();
        } else if(strings.length == 2) {
            if(commandSender.hasPermission("greaterskypvp.admin")) {
                for(Player p : commandSender.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }

    static class KitBukkitRunnable extends BukkitRunnable {

        private final String previousKit;
        private final Player player;
        private final KitManager kitManager;
        private final ItemStack[] prevItems;

        KitBukkitRunnable(Player pl, String prev, KitManager km, ItemStack[] prevItems) {
            super();
            previousKit = prev;
            player = pl;
            kitManager = km;
            this.prevItems = prevItems;
        }

        @Override
        public void run() {
            resetInv();
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            super.cancel();
            resetInv();
        }

        private void resetInv() {
            if(player != null && player.isOnline()) {
                player.getInventory().clear();
                for(int i = 0; i < prevItems.length; i++) {
                    if(prevItems[i] != null)
                        player.getInventory().setItem(i, prevItems[i]);
                }
                kitManager.assignKit(player.getUniqueId().toString(), previousKit);
            }
        }
    }

}


