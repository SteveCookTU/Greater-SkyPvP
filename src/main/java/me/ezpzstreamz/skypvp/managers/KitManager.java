package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import me.ezpzstreamz.skypvp.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, Kit> kitMap;
    private final Map<String, String> kitAssignments;
    private final Map<String, BukkitTask> tasks;
    private final File kitFile;
    private final File assignmentsFile;

    public KitManager(GreaterSkyPvpPlugin plugin) throws FileNotFoundException {
        kitFile = new File(plugin.getDataFolder(), "kits.json");
        if(!kitFile.exists()) plugin.saveResource(kitFile.getName(), false);
        kitMap = gson.fromJson(new FileReader(kitFile), new TypeToken<Map<String, Kit>>(){}.getType());

        assignmentsFile = new File(plugin.getDataFolder(), "kitassignments.json");
        if(!assignmentsFile.exists()) plugin.saveResource(assignmentsFile.getName(), false);
        kitAssignments = gson.fromJson(new FileReader(assignmentsFile), new TypeToken<Map<String, String>>(){}.getType());

        tasks = new HashMap<>();
    }

    public void addTask(Player p, BukkitTask task) {
        tasks.put(p.getUniqueId().toString(), task);
    }

    public void removeTask(Player p) {
        tasks.remove(p.getUniqueId().toString());
    }

    public boolean playerHasTask(Player p) {
        return tasks.containsKey(p.getUniqueId().toString());
    }

    public BukkitTask getTask(Player p) {
        return tasks.get(p.getUniqueId().toString());
    }

    public void saveKits() throws IOException {
        final String json = gson.toJson(kitMap);
        kitFile.delete();
        Files.write(kitFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void saveAssignments() throws IOException {
        final String json = gson.toJson(kitAssignments);
        assignmentsFile.delete();
        Files.write(assignmentsFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    }

    public void assignKit(String uuid, String kitName) {
        kitAssignments.put(uuid, kitName);
    }

    public String getAssignedKitName(String uuid) {
        return kitAssignments.get(uuid);
    }

    public void addKit(String name, PlayerInventory inventory, boolean replace, boolean equip) {
        int size = inventory.getContents().length;
        String[] items = new String[size];
        for(int i = 0; i < size; i++) {
            items[i] = itemTo64(inventory.getContents()[i]);
        }
        if(!kitMap.containsKey(name)) {
            kitMap.put(name, new Kit(items, replace, equip));
        }
    }

    public void deleteKit(String name) {
        kitMap.remove(name);
    }

    public boolean containsKit(String name) {
        return kitMap.containsKey(name);
    }

    public List<String> getKits() {
        return new ArrayList<>(kitMap.keySet());
    }

    public ItemStack[] getKitItems(String name) throws IOException {
        ItemStack[] items = new ItemStack[kitMap.get(name).getItems().length];
        for(int i = 0; i < items.length; i++) {
            items[i] = itemFrom64(kitMap.get(name).getItems()[i]);
        }
        return items;
    }

    public Kit getKit(String name) {
        return kitMap.get(name);
    }

    private static String itemTo64(ItemStack stack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    private static ItemStack itemFrom64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public void giveKit(Player p, String kitName) {
        try {
            Kit kit = getKit(kitName);
            ItemStack[] items = getKitItems(kitName);
            if(kit.shouldReplace()) {
                for(int i = 0; i < items.length; i++) {
                    p.getInventory().setItem(i, items[i]);
                }
            } else {
                if(kit.shouldEquip()) {
                    ItemStack[] armor = p.getInventory().getArmorContents();
                    ItemStack offhand = p.getInventory().getItemInOffHand();
                    for(int i = 0; i < items.length - 5; i++) {
                        if(items[i] == null)
                            continue;
                        p.getInventory().addItem(items[i]);
                    }

                    for(int i = 36; i < items.length; i++) {
                        p.getInventory().setItem(i, items[i]);
                    }
                    for(ItemStack item : armor) {
                        if(item != null) {
                            p.getInventory().addItem(item);
                        }
                    }
                    if(offhand.getType() != Material.AIR)
                        p.getInventory().addItem(offhand);


                } else {
                    for(ItemStack i : items) {
                        if(i == null)
                            continue;
                        p.getInventory().addItem(i);
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
