package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Object> statMap;
    private final File statFile;

    public StatManager(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        statFile = new File(plugin.getDataFolder(), "stats.json");
        if(!statFile.exists()) plugin.saveResource(statFile.getName(), false);
        statMap = gson.fromJson(new FileReader(statFile), new TypeToken<Map<String, Object>>(){}.getType());
    }

    public void saveStats() throws IOException {
        final String json = gson.toJson(statMap);
        statFile.delete();
        Files.write(statFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void registerPlayer(Player player) {
        if(!statMap.containsKey(player.getUniqueId().toString())) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("kills", new ArrayList<String>());
            defaults.put("deaths", new ArrayList<String>());
            statMap.put(player.getUniqueId().toString(), defaults);
        }
    }

    public int getKills(Player player, LocalDateTime limit) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            if(limit == null) {
                return ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("kills")).size();
            } else {
                List<String> kills = (List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("kills");
                int count = 0;
                for(int i = kills.size() - 1; i >= 0; i--) {
                    if(LocalDateTime.parse(kills.get(i)).isAfter(limit))
                        count++;
                    else
                        break;
                }
                return count;
            }
        }
        return 0;
    }

    public int getDeaths(Player player, LocalDateTime limit) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            if(limit == null) {
                return ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("deaths")).size();
            } else {
                List<String> deaths = (List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("deaths");
                int count = 0;
                for(int i = deaths.size() - 1; i >= 0; i--) {
                    if(LocalDateTime.parse(deaths.get(i)).isAfter(limit))
                        count++;
                    else
                        break;
                }
                return count;
            }
        }

        return 0;
    }

    public void addKill(Player player, LocalDateTime time) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("kills")).add(time.toString());
        }
    }

    public void addDeath(Player player, LocalDateTime time) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("deaths")).add(time.toString());
        }
    }

}
