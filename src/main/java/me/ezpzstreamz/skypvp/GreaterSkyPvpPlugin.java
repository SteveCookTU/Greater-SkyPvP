package me.ezpzstreamz.skypvp;

import me.ezpzstreamz.skypvp.commands.CmdArena;
import me.ezpzstreamz.skypvp.commands.CmdKit;
import me.ezpzstreamz.skypvp.commands.CmdSkyPvp;
import me.ezpzstreamz.skypvp.commands.CmdStats;
import me.ezpzstreamz.skypvp.expansion.SkyPvpExpansion;
import me.ezpzstreamz.skypvp.listeners.*;
import me.ezpzstreamz.skypvp.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Objects;

public class GreaterSkyPvpPlugin extends JavaPlugin {

    private KitManager kitManager;
    private WorldManager worldManager;
    private StatManager statManager;
    private ArenaManager arenaManager;
    private MessageManager messageManager;

    private ConnectionManager connectionManager;

    @Override
    public void onEnable() {

        if(!this.getDataFolder().exists()) {
            try {
                boolean mkdir = this.getDataFolder().mkdir();
                if(mkdir)
                    getLogger().info("Generated data folder.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        saveDefaultConfig();

        if(getConfig().getBoolean("mysql.enabled")) {
            connectionManager = new ConnectionManager(this);
        }

        try {
            worldManager = new WorldManager(this, getConfig().getBoolean("mysql.enabled"));
            statManager = new StatManager(this, getConfig().getBoolean("mysql.enabled"));
            kitManager = new KitManager(this, getConfig().getBoolean("mysql.enabled"));
            arenaManager = new ArenaManager(this, getConfig().getBoolean("mysql.enabled"));
            messageManager = new MessageManager(this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SkyPvpExpansion(this).register();
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Placeholders are disabled.");
        }

        Objects.requireNonNull(this.getCommand("skypvp")).setExecutor(new CmdSkyPvp(this));
        Objects.requireNonNull(this.getCommand("skyarena")).setExecutor(new CmdArena(this));
        Objects.requireNonNull(this.getCommand("skykit")).setExecutor(new CmdKit(this));
        Objects.requireNonNull(this.getCommand("skystats")).setExecutor(new CmdStats(this));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldInitListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidListener(worldManager), this);
        getServer().getPluginManager().registerEvents(new PvpListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);

        getLogger().info("Greater SkyPvP enabled.");
    }

    @Override
    public void onDisable() {
        try {
            kitManager.saveKits();
            kitManager.saveAssignments();
            worldManager.saveWorlds();
            statManager.saveStats();
            arenaManager.saveArenas();
            messageManager.saveMessage();
            if(connectionManager != null)
                connectionManager.closePool();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getLogger().info("Greater SkyPvP disabled.");
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public WorldManager getWorldManager() { return worldManager; }

    public StatManager getStatManager() { return statManager; }

    public ArenaManager getArenaManager() { return arenaManager; }

    public MessageManager getMessageManager() { return messageManager; }

    public ConnectionManager getConnectionManager() { return connectionManager; }
}
