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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GreaterSkyPvpPlugin plugin;
    private Map<String, Object> worldMap;
    private File worldFile;
    private final boolean sql;

    public WorldManager(GreaterSkyPvpPlugin plugin, boolean mySql) throws FileNotFoundException {
        sql = mySql;
        this.plugin = plugin;
        if(sql) {
            setupTable();
            loadWorlds();
        } else {
            worldFile = new File(plugin.getDataFolder(), "worlds.json");
            if(!worldFile.exists()) plugin.saveResource(worldFile.getName(), false);
            worldMap = gson.fromJson(new FileReader(worldFile), new TypeToken<Map<String, Object>>(){}.getType());
        }

        for(World world : plugin.getServer().getWorlds()) {
            addWorld(world);
        }

    }

    private void setupTable() {
        Connection conn = null;
        PreparedStatement ps = null;
        plugin.getLogger().info("Checking for/creating world table");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("create table if not exists worlds\n" +
                    "(\n" +
                    "    worldID  int auto_increment\n" +
                    "        primary key,\n" +
                    "    uuid     varchar(36)          not null,\n" +
                    "    enabled  tinyint(1) default 1 not null,\n" +
                    "    voidkill tinyint(1) default 1 not null,\n" +
                    "    constraint worlds_uuid_uindex\n" +
                    "        unique (uuid)\n" +
                    ");");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, null);
        }
    }

    private void loadWorlds() {
        worldMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        plugin.getLogger().info("Loading world data from database...");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("SELECT uuid, enabled, voidkill FROM worlds");
            rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> settings = new HashMap<>();
                settings.put("voidKill", rs.getBoolean("voidkill"));
                settings.put("enabled", rs.getBoolean("enabled"));
                worldMap.put(rs.getString("uuid"), settings);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, rs);
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
        if(!sql) {
            final String json = gson.toJson(worldMap);
            worldFile.delete();
            Files.write(worldFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = plugin.getConnectionManager().getConnection();
                ps = conn.prepareStatement("SELECT uuid FROM worlds");
                rs = ps.executeQuery();
                List<String> old = new ArrayList<>();
                while(rs.next()) {
                    old.add(rs.getString("uuid"));
                }
                ps.close();
                for(String world : old) {
                    if(!worldMap.containsKey(world)) {
                        ps = conn.prepareStatement("DELETE FROM worlds WHERE uuid=?");
                        ps.setString(1, world);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                for(String world : worldMap.keySet()) {
                    if(old.contains(world)) {
                        ps = conn.prepareStatement("UPDATE worlds " +
                                "SET enabled=?, voidkill=? " +
                                "WHERE uuid=?");
                        ps.setBoolean(1, (Boolean) ((Map<String, Object>)worldMap.get(world)).get("enabled"));
                        ps.setBoolean(2, (Boolean) ((Map<String, Object>)worldMap.get(world)).get("voidKill"));
                        ps.setString(3, world);
                    } else {
                        ps = conn.prepareStatement("INSERT INTO worlds (uuid, enabled, voidkill) " +
                                "VALUES (?, ?, ?)");
                        ps.setString(1, world);
                        ps.setBoolean(2, (Boolean) ((Map<String, Object>)worldMap.get(world)).get("enabled"));
                        ps.setBoolean(3, (Boolean) ((Map<String, Object>)worldMap.get(world)).get("voidKill"));
                    }
                    ps.executeUpdate();
                    ps.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                plugin.getConnectionManager().close(conn, ps, rs);
            }
        }

    }

    public void setVoidKill(World world, boolean enable) {
        if(worldMap.containsKey(world.getUID().toString()))
            ((Map<String, Object>)worldMap.get(world.getUID().toString())).put("voidKill", enable);
    }

}
