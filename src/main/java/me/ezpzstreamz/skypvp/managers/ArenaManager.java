package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.Arena;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ArenaManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GreaterSkyPvpPlugin plugin;
    private final Map<String, Arena> arenaMap;
    private final Map<String, Queue<String>> arenaQueues;
    private final Map<String, List<String>> arenaPlayers;
    private final Map<String, BukkitTask> arenaTasks;
    private final File arenaFile;

    public ArenaManager(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        this.plugin = plugin;

        arenaFile = new File(plugin.getDataFolder(), "arenas.json");
        if(!arenaFile.exists()) plugin.saveResource(arenaFile.getName(), false);
        arenaMap = gson.fromJson(new FileReader(arenaFile), new TypeToken<Map<String, Arena>>(){}.getType());

        arenaQueues = new HashMap<>();
        arenaPlayers = new HashMap<>();
        arenaTasks = new HashMap<>();

        for(String n : arenaMap.keySet()) {
            arenaQueues.put(n, new LinkedList<>());
            arenaPlayers.put(n,  new ArrayList<>());
        }

    }

    public void saveArenas() throws IOException {
        final String json = gson.toJson(arenaMap);
        arenaFile.delete();
        Files.write(arenaFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void addArena(String name) {
        if(!arenaMap.containsKey(name))
            arenaMap.put(name, new Arena());
    }

    public void removeArena(String name) {
        arenaMap.remove(name);
    }

    public Arena getArena(String name) {
        return arenaMap.get(name);
    }

    public void addPlayerToQueue(String name, Player p) {
        arenaQueues.get(name).add(p.getUniqueId().toString());
        if(arenaQueues.get(name).size() >= 2 && arenaPlayers.get(name).isEmpty()) {
            startDuel(name, false);
        }
    }

    public String isPlayerInQueue(Player p) {
        for(String arena : arenaQueues.keySet()) {
            if(arenaQueues.get(arena).contains(p.getUniqueId().toString())) {
                return arena;
            }
        }
        return null;
    }

    public void removePlayerFromQueue(String name, Player p) {
        arenaQueues.get(name).remove(p.getUniqueId().toString());
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaLeave").replaceAll("%arena%", name)));
    }

    public void removePlayerFromDuelStart(String name, Player p) {
        if(isDuelStarting(name)) {
            arenaTasks.get(name).cancel();
            arenaTasks.remove(name);
            arenaPlayers.get(name).remove(p.getUniqueId().toString());
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaLeave").replaceAll("%arena%", name)));
            if(arenaPlayers.size() < 2 && arenaQueues.get(name).size() > 0) {
                arenaPlayers.get(name).add(arenaQueues.get(name).poll());
                startDuel(name, true);
            }
        }
    }

    public boolean isDuelStartingLastTick(String name) {
        return arenaTasks.get(name).isCancelled();
    }

    public String isPlayerInDuel(Player p) {
        for(String arena : arenaPlayers.keySet()) {
            if(arenaPlayers.get(arena).contains(p.getUniqueId().toString())) {
                return arena;
            }
        }
        return null;
    }

    public void startDuel(String name, boolean isRestart) {
        if(!isRestart) {
            arenaPlayers.get(name).add(arenaQueues.get(name).poll());
            arenaPlayers.get(name).add(arenaQueues.get(name).poll());
        }
        int index = 1;
        for (String s : arenaQueues.get(name)) {
            Player p = plugin.getServer().getPlayer(UUID.fromString(s));
            int matchesBefore;
            if(index % 2 == 1) {
                matchesBefore = (index + 1) / 2;
            } else {
                matchesBefore = index / 2;
            }
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaQueue").replaceAll("%count%", "" + matchesBefore)));
            index++;
        }

        Player p1 = plugin.getServer().getPlayer(UUID.fromString(arenaPlayers.get(name).get(0)));
        Player p2 = plugin.getServer().getPlayer(UUID.fromString(arenaPlayers.get(name).get(1)));

        BukkitTask task = new BukkitRunnable() {
            int count = 10;
            @Override
            public void run() {
                if(count == 0) {
                    this.cancel();
                    p1.teleport(arenaMap.get(name).getPlayer1Location());
                    p2.teleport(arenaMap.get(name).getPlayer2Location());
                    p1.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarted").replaceAll("%opponent%", p2.getDisplayName())));
                    p2.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarted").replaceAll("%opponent%", p1.getDisplayName())));
                    arenaTasks.remove(name);
                }
                if(count % 10 == 0 || (count <= 5 && count >= 1)) {
                    p1.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarting").replaceAll("%time%", "" + count).replaceAll("%opponent%", p2.getDisplayName())));
                    p2.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarting").replaceAll("%time%", "" + count).replaceAll("%opponent%", p1.getDisplayName())));
                }
                count--;

            }
        }.runTaskTimer(plugin, 0, 20L);

        arenaTasks.put(name, task);

    }

    public boolean isDuelStarting(String name) {
        return arenaTasks.containsKey(name);
    }

    public void endDuel(String name) {
        List<String> temp = new ArrayList<>(arenaPlayers.get(name));
        arenaPlayers.get(name).clear();
        for(String uuid : temp) {
            Player p = plugin.getServer().getPlayer(UUID.fromString(uuid));
            if(p != null) {
                p.teleport(arenaMap.get(name).getSpawnLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                arenaQueues.get(name).add(p.getUniqueId().toString());
            }
        }
        if(arenaQueues.get(name).size() >= 2 && arenaPlayers.get(name).isEmpty()) {
            startDuel(name, false);
        }
    }

    public List<String> getArenas() {
        return new ArrayList<>(arenaMap.keySet());
    }

    public boolean isArenaSetup(String name) {
        if(arenaMap.containsKey(name))
            return arenaMap.get(name).isSetup();
        return false;
    }

}
