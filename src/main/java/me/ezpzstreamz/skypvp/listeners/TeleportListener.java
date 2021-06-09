package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportListener implements Listener {

    private final GreaterSkyPvpPlugin plugin;

    public TeleportListener(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if(e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            String arenaName = plugin.getArenaManager().isPlayerInQueue(e.getPlayer());
            if(arenaName != null) {
                plugin.getArenaManager().removePlayerFromQueue(arenaName, e.getPlayer());
                return;
            }
            arenaName = plugin.getArenaManager().isPlayerInDuel(e.getPlayer());
            if(arenaName != null) {
                if(plugin.getArenaManager().isDuelStarting(arenaName)) {
                    if(!plugin.getArenaManager().isDuelStartingLastTick(arenaName))
                        plugin.getArenaManager().removePlayerFromDuelStart(arenaName, e.getPlayer());
                } else {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaCantLeave")));
                    e.setCancelled(true);
                }
            }
        }

    }

}
