package me.ezpzstreamz.skypvp;

import jdk.internal.math.FloatingDecimal;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Arena {

    private final Map<String, Object> spawnLocation;
    private final Map<String, Object> player1Location;
    private final Map<String, Object> player2Location;

    public Arena() {
        spawnLocation = new HashMap<>();
        player1Location = new HashMap<>();
        player2Location = new HashMap<>();
    }

    public boolean isSetup() {
        return !spawnLocation.isEmpty() && !player1Location.isEmpty() && !player2Location.isEmpty();
    }

    public void setSpawnLocation(Location l) {
        setLocation(l, spawnLocation);
    }

    public void setPlayer1Location(Location l) {
        setLocation(l, player1Location);
    }

    public void setPlayer2Location(Location l) {
        setLocation(l, player2Location);
    }

    public Location getSpawnLocation() {
        return getLocation(spawnLocation);
    }

    public Location getPlayer1Location() {
        return getLocation(player1Location);
    }

    public Location getPlayer2Location() {
        return getLocation(player2Location);
    }

    @Nullable
    private Location getLocation(Map<String, Object> locationMap) {
        if(locationMap.isEmpty()) {
            return null;
        }
        return new Location(Bukkit.getServer().getWorld(UUID.fromString((String) locationMap.get("world"))),
                (double) locationMap.get("x"), (double) locationMap.get("y"), (double) locationMap.get("z"),
                ((Double) locationMap.get("yaw")).floatValue(), ((Double) locationMap.get("pitch")).floatValue());
    }

    private void setLocation(Location l, Map<String, Object> location) {
        location.put("x", l.getX());
        location.put("y", l.getY());
        location.put("z", l.getZ());
        location.put("pitch", (double)l.getPitch());
        location.put("yaw", (double)l.getYaw());
        location.put("world", Objects.requireNonNull(l.getWorld()).getUID().toString());
    }

}
