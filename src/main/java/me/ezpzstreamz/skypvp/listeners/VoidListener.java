package me.ezpzstreamz.skypvp.listeners;

import me.ezpzstreamz.skypvp.managers.WorldManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class VoidListener implements Listener {

    private final WorldManager worldManager;

    public VoidListener(WorldManager wm) {
        worldManager = wm;
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            if(e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if(worldManager.shouldVoidKill(e.getEntity().getWorld())) {
                    Player p = (Player) e.getEntity();
                    ((Player) e.getEntity()).damage(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + 1000);
                }
            }
        }
    }

}
