package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldInitListener implements Listener {

    private final GreaterSkyPvpPlugin plugin;

    public WorldInitListener(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent e) {
        plugin.getWorldManager().addWorld(e.getWorld());
    }

}
