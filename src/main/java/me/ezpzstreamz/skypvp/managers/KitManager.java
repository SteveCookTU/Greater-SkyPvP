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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GreaterSkyPvpPlugin plugin;
    private Map<String, Kit> kitMap;
    private Map<String, String> kitAssignments;
    private final Map<String, BukkitTask> tasks;
    private File kitFile;
    private File assignmentsFile;
    private final boolean sql;

    public KitManager(GreaterSkyPvpPlugin plugin, boolean mySql) throws FileNotFoundException {
        this.plugin = plugin;
        sql = mySql;
        if(sql) {
            createTables();
            loadKits();
        } else {
            kitFile = new File(plugin.getDataFolder(), "kits.json");
            if(!kitFile.exists()) plugin.saveResource(kitFile.getName(), false);
            kitMap = gson.fromJson(new FileReader(kitFile), new TypeToken<Map<String, Kit>>(){}.getType());

            assignmentsFile = new File(plugin.getDataFolder(), "kitassignments.json");
            if(!assignmentsFile.exists()) plugin.saveResource(assignmentsFile.getName(), false);
            kitAssignments = gson.fromJson(new FileReader(assignmentsFile), new TypeToken<Map<String, String>>(){}.getType());
        }

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
        if(!sql) {
            final String json = gson.toJson(kitMap);
            kitFile.delete();
            Files.write(kitFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = plugin.getConnectionManager().getConnection();
                ps = conn.prepareStatement("SELECT kitName FROM kits");
                rs = ps.executeQuery();
                List<String> old = new ArrayList<>();
                while(rs.next()) {
                    old.add(rs.getString("kitName"));
                }
                ps.close();
                for(String kit : old) {
                    if(!kitMap.containsKey(kit)) {
                        ps = conn.prepareStatement("DELETE FROM kits WHERE kitName=?");
                        ps.setString(1, kit);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                for(String kit : kitMap.keySet()) {
                    if(old.contains(kit)) {
                        ps = conn.prepareStatement("UPDATE kits " +
                                "SET `replace`=?, equip=? " +
                                "WHERE kitName=?");
                        ps.setBoolean(1, kitMap.get(kit).shouldReplace());
                        ps.setBoolean(2, kitMap.get(kit).shouldEquip());
                        ps.setString(3, kit);
                        ps.executeUpdate();
                        ps.close();
                        String[] items = kitMap.get(kit).getItems();
                        for(int i = 0; i < items.length; i++) {
                            ps = conn.prepareStatement("UPDATE kit_items " +
                                    "SET base64String=? " +
                                    "WHERE kitID=(SELECT kitID FROM kits WHERE kitName=?) AND slotID=?");
                            ps.setString(1, items[i]);
                            ps.setString(2, kit);
                            ps.setInt(3, i);
                            ps.executeUpdate();
                            ps.close();
                        }
                    } else {
                        ps = conn.prepareStatement("INSERT INTO kits (kitName, `replace`, equip) " +
                                "VALUES (?, ?, ?)");
                        ps.setString(1, kit);
                        ps.setBoolean(2, kitMap.get(kit).shouldReplace());
                        ps.setBoolean(3, kitMap.get(kit).shouldEquip());
                        ps.executeUpdate();
                        ps.close();
                        String[] items = kitMap.get(kit).getItems();
                        for(int i = 0; i < items.length; i++) {
                            ps = conn.prepareStatement("INSERT INTO kit_items (kitID, slotID, base64string) " +
                                    "SELECT k.kitID, ?, ? " +
                                    "FROM kits k " +
                                    "WHERE k.kitName=?");
                            ps.setInt(1, i);
                            ps.setString(2, items[i]);
                            ps.setString(3, kit);
                            ps.executeUpdate();
                            ps.close();
                        }
                    }
                }

            }catch (SQLException e) {
                e.printStackTrace();
            } finally {
                plugin.getConnectionManager().close(conn, ps, rs);
            }
        }
    }

    public void saveAssignments() throws IOException {
        if(!sql) {
            final String json = gson.toJson(kitAssignments);
            assignmentsFile.delete();
            Files.write(assignmentsFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = plugin.getConnectionManager().getConnection();
                ps = conn.prepareStatement("SELECT p.uuid FROM kit_assignments ka INNER JOIN players p ON ka.playerID=p.playerID");
                rs = ps.executeQuery();
                List<String> old = new ArrayList<>();
                while(rs.next()) {
                    old.add(rs.getString("uuid"));
                }
                ps.close();
                for(String player : old) {
                    if(!kitAssignments.containsKey(player)) {
                        ps = conn.prepareStatement("DELETE FROM kit_assignments " +
                                "WHERE playerID=(SELECT playerID FROM players WHERE uuid=?)");
                        ps.setString(1, player);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                for(String player : kitAssignments.keySet()) {
                    if(old.contains(player)) {
                        ps = conn.prepareStatement("UPDATE kit_assignments " +
                                "SET kitID=(SELECT kitID FROM kits WHERE kitName=?) " +
                                "WHERE playerID=(SELECT playerID FROM players WHERE uuid=?)");
                        ps.setString(1, kitAssignments.get(player));
                        ps.setString(2, player);
                    } else {
                        ps = conn.prepareStatement("INSERT INTO kit_assignments (playerID, kitID) " +
                                "SELECT p.playerID, k.kitID " +
                                "FROM players p, kits k " +
                                "WHERE p.uuid=? AND k.kitName=?");
                        ps.setString(1, player);
                        ps.setString(2, kitAssignments.get(player));
                    }
                    ps.executeUpdate();
                    ps.close();
                }

            }catch (SQLException e) {
                e.printStackTrace();
            } finally {
                plugin.getConnectionManager().close(conn, ps, rs);
            }
        }
    }

    public void assignKit(String uuid, String kitName) {
        if(kitName == null)
            kitAssignments.remove(uuid);
        else
            kitAssignments.put(uuid, kitName);
    }

    public String getAssignedKitName(String uuid) {
        return kitAssignments.getOrDefault(uuid, "");
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

    private String itemTo64(ItemStack stack) throws IllegalStateException {
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

    private ItemStack itemFrom64(String data) throws IOException {
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

    private void createTables() {
        Connection conn = null;
        PreparedStatement ps = null;
        plugin.getLogger().info("Checking for/creating kit tables");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("create table if not exists kits\n" +
                    "(\n" +
                    "    kitID     int auto_increment\n" +
                    "        primary key,\n" +
                    "    kitName   varchar(255)         not null,\n" +
                    "    `replace` tinyint(1) default 1 not null,\n" +
                    "    equip     tinyint(1) default 1 not null,\n" +
                    "    constraint kits_kitName_uindex\n" +
                    "        unique (kitName)\n" +
                    ");");
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("create table if not exists kit_items\n" +
                    "(\n" +
                    "    kitID        int          not null,\n" +
                    "    slotID       int          not null,\n" +
                    "    base64string varchar(600) null,\n" +
                    "    primary key (kitID, slotID),\n" +
                    "    constraint kit_items_kits_kitID_fk\n" +
                    "        foreign key (kitID) references kits (kitID)\n" +
                    "            on update cascade on delete cascade\n" +
                    ");");
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("create table if not exists kit_assignments\n" +
                    "(\n" +
                    "    playerID int not null\n" +
                    "        primary key,\n" +
                    "    kitID    int not null,\n" +
                    "    constraint kit_assignments_kits_kitID_fk\n" +
                    "        foreign key (kitID) references kits (kitID)\n" +
                    "            on update cascade on delete cascade,\n" +
                    "    constraint kit_assignments_players_playerID_fk\n" +
                    "        foreign key (playerID) references players (playerID)\n" +
                    "            on update cascade on delete cascade\n" +
                    ");");
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, null);
        }
    }

    private void loadKits() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        kitMap = new HashMap<>();
        kitAssignments = new HashMap<>();
        plugin.getLogger().info("Loading kit data from database...");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("SELECT kitID, kitName, `replace`, equip\n " +
                    "FROM kits");
            rs = ps.executeQuery();
            while(rs.next()) {
                plugin.getLogger().info("Loading kit " + rs.getString("kitName") + "...");
                PreparedStatement ps2 = conn.prepareStatement("SELECT slotID, base64string\n" +
                        "FROM kit_items\n" +
                        "WHERE kitID=?");
                ps2.setInt(1, rs.getInt("kitID"));
                ResultSet rs2 = ps2.executeQuery();
                String[] items = new String[41];
                while(rs2.next()) {
                    items[rs2.getInt("slotID")] = rs2.getString("base64string");
                }
                ps2.close();
                rs2.close();
                kitMap.put(rs.getString("kitName"), new Kit(items, rs.getBoolean("replace"), rs.getBoolean("equip")));
            }

            ps.close();
            rs.close();

            ps = conn.prepareStatement("SELECT p.uuid, k.kitName\n" +
                    "FROM kit_assignments ka\n" +
                    "INNER JOIN players p on ka.playerID = p.playerID\n" +
                    "INNER JOIN kits k on ka.kitID = k.kitID");
            rs = ps.executeQuery();
            while(rs.next()) {
                kitAssignments.put(rs.getString("uuid"), rs.getString("kitName"));
            }

        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, rs);
        }
    }
}
