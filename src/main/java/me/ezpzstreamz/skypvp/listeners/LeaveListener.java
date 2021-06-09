package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    private final GreaterSkyPvpPlugin plugin;

    public LeaveListener(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        String arenaName = plugin.getArenaManager().isPlayerInQueue(e.getPlayer());
        if(arenaName != null) {
            plugin.getArenaManager().removePlayerFromQueue(arenaName, e.getPlayer());
            return;
        }
        arenaName = plugin.getArenaManager().isPlayerInDuel(e.getPlayer());
        if(arenaName != null) {
            plugin.getArenaManager().endDuel(plugin.getArenaManager().isPlayerInDuel(e.getPlayer()));
        }

    }

}
