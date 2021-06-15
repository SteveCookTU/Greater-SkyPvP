package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.time.LocalDateTime;

public class PvpListener implements Listener {

    private final GreaterSkyPvpPlugin plugin;

    public PvpListener(GreaterSkyPvpPlugin p) {
        plugin = p;
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            plugin.getStatManager().addDeath((Player) event.getEntity(), LocalDateTime.now());
            if(event.getEntity().getKiller() != null) {
                plugin.getStatManager().addKill(event.getEntity().getKiller(), LocalDateTime.now());
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(event.getEntity() instanceof Player) {
            if(((Player) event.getEntity()).getHealth() - event.getFinalDamage() <= 0) {
                String arenaName = plugin.getArenaManager().isPlayerInDuel((Player) event.getEntity());
                if(arenaName != null) {
                    event.setCancelled(true);
                    plugin.getArenaManager().endDuel(plugin.getArenaManager().isPlayerInDuel((Player) event.getEntity()));
                    plugin.getStatManager().addDeath((Player) event.getEntity(), LocalDateTime.now());
                    if(event.getDamager() instanceof Player) {
                        plugin.getStatManager().addKill((Player) event.getDamager(), LocalDateTime.now());
                    }
                }
            }
        }
    }
}
