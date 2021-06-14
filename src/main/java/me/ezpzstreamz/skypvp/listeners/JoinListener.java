package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import me.ezpzstreamz.skypvp.managers.KitManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final GreaterSkyPvpPlugin plugin;

    public JoinListener(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().getInventory().clear();
        String kitName = plugin.getKitManager().getAssignedKitName(e.getPlayer().getUniqueId().toString());
        if(!kitName.equals("")) {
            plugin.getKitManager().giveKit(e.getPlayer(), kitName);
        }

        plugin.getStatManager().registerPlayer(e.getPlayer());

        e.getPlayer().teleport(e.getPlayer().getWorld().getSpawnLocation());

    }

}
