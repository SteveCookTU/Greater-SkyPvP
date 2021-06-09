package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class MessageManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, String> messageMap;
    private File messageFile;

    public MessageManager(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        reloadMessages(plugin);
    }

    public void saveMessage() throws IOException {
        final String json = gson.toJson(messageMap);
        messageFile.delete();
        Files.write(messageFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void reloadMessages(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        messageFile = new File(plugin.getDataFolder(), "messages.json");
        if(!messageFile.exists()) plugin.saveResource(messageFile.getName(), false);
        messageMap = gson.fromJson(new FileReader(messageFile), new TypeToken<Map<String, String>>(){}.getType());
    }

    public String getMessage(String name) {
        return messageMap.get(name);
    }

}
