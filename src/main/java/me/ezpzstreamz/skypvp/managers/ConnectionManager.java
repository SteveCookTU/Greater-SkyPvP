package me.ezpzstreamz.skypvp.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionManager {

    private final HikariDataSource dataSource;

    public ConnectionManager(GreaterSkyPvpPlugin plugin) {
        String mySqlUsername = plugin.getConfig().getString("mysql.username");
        String mySqlPassword = plugin.getConfig().getString("mysql.password");
        String mySqlHost = plugin.getConfig().getString("mysql.host");
        String mySqlPort = plugin.getConfig().getString("mysql.port");
        String mySqlDatabase = plugin.getConfig().getString("mysql.database");

        int maximumConnections = plugin.getConfig().getInt("mysql.maxConnections");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + mySqlHost + ":" + mySqlPort + "/" + mySqlDatabase);
        hikariConfig.setUsername(mySqlUsername);
        hikariConfig.setPassword(mySqlPassword);
        hikariConfig.setMaximumPoolSize(maximumConnections);

        dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        if(conn != null) try {conn.close();} catch (SQLException ignored){}
        if(ps != null) try {ps.close();} catch (SQLException ignored){}
        if(rs != null) try {rs.close();} catch (SQLException ignored){}
    }

    public void closePool() {
        if(dataSource != null && !dataSource.isClosed())
            dataSource.close();
    }

}
