package me.ezpzstreamz.skypvp.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.ezpzstreamz.skypvp.Arena;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ArenaManager {

    private Gson gson;
    private final GreaterSkyPvpPlugin plugin;
    private Map<String, Arena> arenaMap;
    private final Map<String, Queue<String>> arenaQueues;
    private final Map<String, List<String>> arenaPlayers;
    private final Map<String, BukkitTask> arenaTasks;
    private File arenaFile;
    private final boolean sql;

    public ArenaManager(GreaterSkyPvpPlugin plugin, boolean mySql) throws FileNotFoundException {
        this.plugin = plugin;
        sql = mySql;
        if(sql) {
            setupTables();
            loadArenas();
        } else {
            gson = new GsonBuilder().setPrettyPrinting().create();

            arenaFile = new File(plugin.getDataFolder(), "arenas.json");
            if(!arenaFile.exists()) plugin.saveResource(arenaFile.getName(), false);
            arenaMap = gson.fromJson(new FileReader(arenaFile), new TypeToken<Map<String, Arena>>(){}.getType());
        }

        arenaQueues = new HashMap<>();
        arenaPlayers = new HashMap<>();
        arenaTasks = new HashMap<>();

        for(String n : arenaMap.keySet()) {
            arenaQueues.put(n, new LinkedList<>());
            arenaPlayers.put(n,  new ArrayList<>());
        }
    }

    public void saveArenas() throws IOException {
        if(!sql) {
            final String json = gson.toJson(arenaMap);
            arenaFile.delete();
            Files.write(arenaFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = plugin.getConnectionManager().getConnection();
                ps = conn.prepareStatement("SELECT arenaName FROM arenas");
                rs = ps.executeQuery();
                List<String> old = new ArrayList<>();
                while(rs.next()) {
                    old.add(rs.getString("arenaName"));
                }
                ps.close();
                for(String arena : old) {
                    if(!arenaMap.containsKey(arena)) {
                        ps = conn.prepareStatement("DELETE FROM arenas WHERE arenaName=?");
                        ps.setString(1, arena);
                        ps.executeUpdate();
                        ps.close();
                    }
                }
                for(String arena : arenaMap.keySet()) {
                    Location spawnLocation = arenaMap.get(arena).getSpawnLocation();
                    Location player1Location = arenaMap.get(arena).getPlayer1Location();
                    Location player2Location = arenaMap.get(arena).getPlayer2Location();
                    if(old.contains(arena)) {
                        if(spawnLocation != null) {
                            ps = conn.prepareStatement("UPDATE arena_spawns " +
                                    "SET xCoordinate=?, yCoordinate=?, zCoordinate=?, pitch=?, yaw=?, worldID=(SELECT worldID FROM worlds WHERE uuid=?) " +
                                    "WHERE arenaID=(SELECT arenaID FROM arenas WHERE arenaName=?) AND spawnID=0");
                            execUpdateStatementParams(ps, arena, spawnLocation);
                        }
                        if(player1Location != null) {
                            ps = conn.prepareStatement("UPDATE arena_spawns " +
                                    "SET xCoordinate=?, yCoordinate=?, zCoordinate=?, pitch=?, yaw=?, worldID=(SELECT worldID FROM worlds WHERE uuid=?) " +
                                    "WHERE arenaID=(SELECT arenaID FROM arenas WHERE arenaName=?) AND spawnID=1");
                            execUpdateStatementParams(ps, arena, player1Location);
                        }
                        if(player2Location != null) {
                            ps = conn.prepareStatement("UPDATE arena_spawns " +
                                    "SET xCoordinate=?, yCoordinate=?, zCoordinate=?, pitch=?, yaw=?, worldID=(SELECT worldID FROM worlds WHERE uuid=?) " +
                                    "WHERE arenaID=(SELECT arenaID FROM arenas WHERE arenaName=?) AND spawnID=2");
                            execUpdateStatementParams(ps, arena, player2Location);
                        }
                    } else {
                        ps = conn.prepareStatement("INSERT INTO arenas (arenaName) " +
                                "VALUES (?)");
                        ps.setString(1, arena);
                        ps.executeUpdate();
                        ps.close();
                        if(spawnLocation != null) {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID, xCoordinate, yCoordinate, zCoordinate, pitch, yaw, worldID) " +
                                    "SELECT a.arenaID, 0, ?, ?, ?, ?, ?, w.worldID " +
                                    "FROM arenas a, worlds w " +
                                    "WHERE a.arenaName=? AND w.uuid=?");
                            setInsertStatement(ps, arena, spawnLocation);
                        } else {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID) " +
                                    "SELECT a.arenaID, 0 " +
                                    "FROM arenas a " +
                                    "WHERE a.arenaName=? ");
                            ps.setString(1, arena);
                        }
                        ps.executeUpdate();
                        ps.close();

                        if(player1Location != null) {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID, xCoordinate, yCoordinate, zCoordinate, pitch, yaw, worldID) " +
                                    "SELECT a.arenaID, 1, ?, ?, ?, ?, ?, w.worldID " +
                                    "FROM arenas a, worlds w " +
                                    "WHERE a.arenaName=? AND w.uuid=?");
                            setInsertStatement(ps, arena, player1Location);
                        } else {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID) " +
                                    "SELECT a.arenaID, 1 " +
                                    "FROM arenas a " +
                                    "WHERE a.arenaName=? ");
                            ps.setString(1, arena);
                        }
                        ps.executeUpdate();
                        ps.close();

                        if(player2Location != null) {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID, xCoordinate, yCoordinate, zCoordinate, pitch, yaw, worldID) " +
                                    "SELECT a.arenaID, 2, ?, ?, ?, ?, ?, w.worldID " +
                                    "FROM arenas a, worlds w " +
                                    "WHERE a.arenaName=? AND w.uuid=?");
                            setInsertStatement(ps, arena, player2Location);
                        } else {
                            ps = conn.prepareStatement("INSERT INTO arena_spawns (arenaID, spawnID) " +
                                    "SELECT a.arenaID, 2 " +
                                    "FROM arenas a " +
                                    "WHERE a.arenaName=? ");
                            ps.setString(1, arena);
                        }
                        ps.executeUpdate();
                        ps.close();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                plugin.getConnectionManager().close(conn, ps, rs);
            }
        }
    }

    private void setInsertStatement(PreparedStatement ps, String arena, Location player1Location) throws SQLException {
        ps.setDouble(1, player1Location.getX());
        ps.setDouble(2, player1Location.getY());
        ps.setDouble(3, player1Location.getZ());
        ps.setDouble(4, player1Location.getPitch());
        ps.setDouble(5, player1Location.getYaw());
        ps.setString(6, arena);
        ps.setString(7, Objects.requireNonNull(player1Location.getWorld()).getUID().toString());
    }

    private void execUpdateStatementParams(PreparedStatement ps, String arena, Location loc) throws SQLException {
        ps.setDouble(1, loc.getX());
        ps.setDouble(2, loc.getY());
        ps.setDouble(3, loc.getZ());
        ps.setDouble(4, loc.getPitch());
        ps.setDouble(5, loc.getYaw());
        ps.setString(6, Objects.requireNonNull(loc.getWorld()).getUID().toString());
        ps.setString(7, arena);
        ps.executeUpdate();
        ps.close();
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
            assert p != null;
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
                    assert p1 != null;
                    p1.teleport(arenaMap.get(name).getPlayer1Location());
                    assert p2 != null;
                    p2.teleport(arenaMap.get(name).getPlayer2Location());
                    p1.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarted").replaceAll("%opponent%", p2.getDisplayName())));
                    p2.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getMessageManager().getMessage("arenaStarted").replaceAll("%opponent%", p1.getDisplayName())));
                    arenaTasks.remove(name);
                }
                if(count % 10 == 0 || (count <= 5 && count >= 1)) {
                    assert p1 != null;
                    assert p2 != null;
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

    public void setLocation(String name, int index, Location l) {
        switch(index) {
            case 0:
                arenaMap.get(name).setSpawnLocation(l);
                break;
            case 1:
                arenaMap.get(name).setPlayer1Location(l);
                break;
            case 2:
                arenaMap.get(name).setPlayer2Location(l);
                break;
        }
    }

    private void setupTables() {
        Connection conn = null;
        PreparedStatement ps = null;
        plugin.getLogger().info("Checking for/creating Arena tables");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS arenas (\n" +
                    "arenaID   int auto_increment\n" +
                    "    primary key,\n" +
                    "arenaName varchar(255) not null,\n" +
                    "constraint arenas_arenaName_uindex\n" +
                    "    unique (arenaName)" +
                    ");");
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("CREATE TABLE IF NOT EXISTS arena_spawns (\n" +
                    "arenaID     int    not null,\n" +
                    "spawnID     int    not null,\n" +
                    "xCoordinate double null,\n" +
                    "yCoordinate double null,\n" +
                    "zCoordinate double null,\n" +
                    "pitch       double null,\n" +
                    "yaw         double null,\n" +
                    "worldID     int    null,\n" +
                    "primary key (arenaID, spawnID),\n" +
                    "constraint arena_spawns_arenas_arenaID_fk\n" +
                    "    foreign key (arenaID) references arenas (arenaID)\n" +
                    "        on update cascade on delete cascade,\n" +
                    "constraint arena_spawns_worlds_worldID_fk\n" +
                    "    foreign key (worldID) references worlds (worldID)\n" +
                    "        on update cascade on delete cascade" +
                    ");");
            ps.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, null);
        }
    }

    private void loadArenas() {
        arenaMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        plugin.getLogger().info("Loading arena data from database...");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("SELECT a.arenaName, asp.spawnID, asp.xCoordinate, asp.yCoordinate, asp.zCoordinate, asp.pitch, asp.yaw, w.uuid\n" +
                    "FROM arenas a\n" +
                    "INNER JOIN arena_spawns asp ON asp.arenaID = a.arenaID\n" +
                    "LEFT OUTER JOIN worlds w ON asp.worldID = w.worldID;");
            rs = ps.executeQuery();
            while(rs.next()) {
                String arenaName = rs.getString("arenaName");
                if(!arenaMap.containsKey(arenaName)) {
                    arenaMap.put(arenaName, new Arena());
                }
                rs.getString("xCoordinate");
                if(!rs.wasNull()) {
                    setLocation(arenaName, rs.getInt("spawnID"),
                            new Location(plugin.getServer().getWorld(UUID.fromString(rs.getString("uuid"))),
                                    rs.getDouble("xCoordinate"),
                                    rs.getDouble("yCoordinate"),
                                    rs.getDouble("zCoordinate"),
                                    new Double(rs.getDouble("yaw")).floatValue(),
                                    new Double(rs.getDouble("pitch")).floatValue()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, rs);
        }
    }
}
