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
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final GreaterSkyPvpPlugin plugin;
    private Map<String, Object> statMap;
    private File statFile;
    private final boolean sql;

    public StatManager(GreaterSkyPvpPlugin plugin, boolean mySql) throws FileNotFoundException {
        this.plugin = plugin;
        sql = mySql;
        if(sql) {
            createTables();
            loadStats();
        } else {
            statFile = new File(plugin.getDataFolder(), "stats.json");
            if(!statFile.exists()) plugin.saveResource(statFile.getName(), false);
            statMap = gson.fromJson(new FileReader(statFile), new TypeToken<Map<String, Object>>(){}.getType());
        }

    }

    public void saveStats() throws IOException {
        if(!sql) {
            final String json = gson.toJson(statMap);
            boolean delete = statFile.delete();
            if(delete)
                Files.write(statFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } else {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = plugin.getConnectionManager().getConnection();
                ps = conn.prepareStatement("SELECT uuid FROM players");
                rs = ps.executeQuery();
                List<String> old = new ArrayList<>();
                while(rs.next()) {
                    old.add(rs.getString("uuid"));
                }
                ps.close();
                for(String player : old) {
                    if(!statMap.containsKey(player)) {
                        ps = conn.prepareStatement("DELETE FROM players WHERE uuid=?");
                        ps.setString(1, player);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                for(String player : statMap.keySet()) {
                    if(!old.contains(player)) {
                        ps = conn.prepareStatement("INSERT INTO players (uuid) " +
                                "VALUES (?)");
                        ps.setString(1, player);
                        ps.executeUpdate();
                        ps.close();
                    }
                }

                ps = conn.prepareStatement("DELETE FROM kill_record");
                ps.executeUpdate();
                ps.close();

                ps = conn.prepareStatement("DELETE FROM death_record");
                ps.executeUpdate();
                ps.close();

                for(String player : statMap.keySet()) {
                    Object o = statMap.get(player);
                    Object killList = ((Map<?, ?>) o).get("kills");
                    for(Object datetime : ((List<?>)killList)) {
                        ps = conn.prepareStatement("""
                                INSERT INTO kill_record (playerID, datetime)
                                SELECT p.playerID, ?
                                FROM players p
                                WHERE p.uuid=?""");
                        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.parse((String)datetime)));
                        ps.setString(2, player);
                        ps.executeUpdate();
                        ps.close();
                    }

                    Object deathList = ((Map<?, ?>) o).get("deaths");

                    for(Object datetime : ((List<?>)deathList)) {
                        ps = conn.prepareStatement("""
                                INSERT INTO death_record (playerID, datetime)
                                SELECT p.playerID, ?
                                FROM players p
                                WHERE p.uuid=?""");
                        ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.parse((String)datetime)));
                        ps.setString(2, player);
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
            Object o = statMap.get(player.getUniqueId().toString());
            Object killList = ((Map<?, ?>) o).get("kills");
            return GetStatListCount(limit, killList);
        }
        return 0;
    }

    public int getDeaths(Player player, LocalDateTime limit) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            Object o = statMap.get(player.getUniqueId().toString());
            Object deathList = ((Map<?, ?>) o).get("deaths");
            return GetStatListCount(limit, deathList);
        }

        return 0;
    }

    private int GetStatListCount(LocalDateTime limit, Object deathList) {
        if(limit == null) {
            return ((List<?>)deathList).size();
        } else {
            int count = 0;
            for(int i = ((List<?>) deathList).size() - 1; i >= 0; i--) {
                if(LocalDateTime.parse((String) ((List<?>) deathList).get(i)).isAfter(limit))
                    count++;
                else
                    break;
            }
            return count;
        }
    }

    @SuppressWarnings("unchecked")
    public void addKill(Player player, LocalDateTime time) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("kills")).add(time.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public void addDeath(Player player, LocalDateTime time) {
        if(statMap.containsKey(player.getUniqueId().toString())) {
            ((List<String>)((Map<String, Object>)statMap.get(player.getUniqueId().toString())).get("deaths")).add(time.toString());
        }
    }

    private void createTables() {
        Connection conn = null;
        PreparedStatement ps = null;
        plugin.getLogger().info("Checking for/creating statistic tables");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("""
                    create table if not exists players
                    (
                        playerID int auto_increment
                            primary key,
                        uuid     varchar(36) not null,
                        constraint players_uuid_uindex
                            unique (uuid)
                    );""");
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("""
                    create table if not exists kill_record
                    (
                        playerID int                                  not null,
                        datetime datetime default current_timestamp() not null,
                        constraint kill_record_players_playerID_fk
                            foreign key (playerID) references players (playerID)
                                on update cascade on delete cascade
                    );""");
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement("""
                    create table if not exists death_record
                    (
                        playerID int                                  not null,
                        datetime datetime default current_timestamp() not null,
                        constraint death_record_players_playerID_fk
                            foreign key (playerID) references players (playerID)
                                on update cascade on delete cascade
                    );""");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, null);
        }
    }

    private void loadStats() {
        statMap = new HashMap<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        plugin.getLogger().info("Loading statistic data from database...");
        try {
            conn = plugin.getConnectionManager().getConnection();
            ps = conn.prepareStatement("SELECT uuid FROM players");
            rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> stats = new HashMap<>();
                PreparedStatement ps2 = conn.prepareStatement("""
                        SELECT datetime
                        FROM kill_record
                        WHERE playerID=(SELECT playerID FROM players WHERE uuid=?)""");
                ps2.setString(1, rs.getString("uuid"));
                ResultSet rs2 = ps2.executeQuery();
                List<String> kills = new ArrayList<>();
                while(rs2.next()) {
                    kills.add(rs2.getTimestamp("datetime").toLocalDateTime().toString());
                }
                stats.put("kills", kills);
                ps2.close();
                rs2.close();

                ps2 = conn.prepareStatement("""
                        SELECT datetime
                        FROM death_record
                        WHERE playerID=(SELECT playerID FROM players WHERE uuid=?)""");
                ps2.setString(1, rs.getString("uuid"));
                rs2 = ps2.executeQuery();
                List<String> deaths = new ArrayList<>();
                while(rs2.next()) {
                    deaths.add(rs2.getTimestamp("datetime").toLocalDateTime().toString());
                }
                stats.put("deaths", deaths);
                ps2.close();
                rs2.close();
                statMap.put(rs.getString("uuid"), stats);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        } finally {
            plugin.getConnectionManager().close(conn, ps, rs);
        }
    }

}
