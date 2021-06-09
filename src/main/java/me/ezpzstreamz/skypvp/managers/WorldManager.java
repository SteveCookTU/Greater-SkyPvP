package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.World;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Object> worldMap;
    private final File worldFile;

    public WorldManager(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        worldFile = new File(plugin.getDataFolder(), "worlds.json");
        if(!worldFile.exists()) plugin.saveResource(worldFile.getName(), false);
        worldMap = gson.fromJson(new FileReader(worldFile), new TypeToken<Map<String, Object>>(){}.getType());

        for(World world : plugin.getServer().getWorlds()) {
            addWorld(world);
        }
    }

    public void addWorld(World world) {
        if(!worldMap.containsKey(world.getUID().toString())) {
            Map<String, Object> defaults = new HashMap<>();
            defaults.put("voidKill", true);
            defaults.put("enabled", true);
            worldMap.put(world.getUID().toString(), defaults);
        }
    }

    public boolean shouldVoidKill(World world) {
        if(worldMap.containsKey(world.getUID().toString()))
            return (boolean) ((Map<String, Object>)worldMap.get(world.getUID().toString())).get("voidKill");
        return true;
    }

    public void saveWorlds() throws IOException {
        final String json = gson.toJson(worldMap);
        worldFile.delete();
        Files.write(worldFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void setVoidKill(World world, boolean enable) {
        if(worldMap.containsKey(world.getUID().toString()))
            ((Map<String, Object>)worldMap.get(world.getUID().toString())).put("voidKill", enable);
    }

}
