package me.ezpzstreamz.skypvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
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
        return new Location(Bukkit.getServer().getWorld(UUID.fromString((String) spawnLocation.get("world"))),
                (double)spawnLocation.get("x"), (double)spawnLocation.get("y"), (double)spawnLocation.get("z"),
                ((Double)spawnLocation.get("yaw")).floatValue(), ((Double)spawnLocation.get("pitch")).floatValue());
    }

    public Location getPlayer1Location() {
        return new Location(Bukkit.getServer().getWorld(UUID.fromString((String) player1Location.get("world"))),
                (double)player1Location.get("x"), (double)player1Location.get("y"), (double)player1Location.get("z"),
                ((Double)player1Location.get("yaw")).floatValue(), ((Double)player1Location.get("pitch")).floatValue());
    }

    public Location getPlayer2Location() {
        return new Location(Bukkit.getServer().getWorld(UUID.fromString((String) player2Location.get("world"))),
                (double)player2Location.get("x"), (double)player2Location.get("y"), (double)player2Location.get("z"),
                ((Double)player2Location.get("yaw")).floatValue(), ((Double)player2Location.get("pitch")).floatValue());
    }

    private void setLocation(Location l, Map<String, Object> location) {
        location.put("x", l.getX());
        location.put("y", l.getY());
        location.put("z", l.getZ());
        location.put("pitch", l.getPitch());
        location.put("yaw", l.getYaw());
        location.put("world", l.getWorld().getUID().toString());
    }

}
