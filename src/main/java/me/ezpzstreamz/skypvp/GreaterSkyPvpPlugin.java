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

import java.io.FileNotFoundException;
import java.io.IOException;

public class GreaterSkyPvpPlugin extends JavaPlugin {

    private KitManager kitManager;
    private WorldManager worldManager;
    private StatManager statManager;
    private ArenaManager arenaManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {

        if(!this.getDataFolder().exists()) {
            try {
                this.getDataFolder().mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            kitManager = new KitManager(this);
            worldManager = new WorldManager(this);
            statManager = new StatManager(this);
            arenaManager = new ArenaManager(this);
            messageManager = new MessageManager(this);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SkyPvpExpansion(this).register();
        } else {
            getLogger().warning("Could not find PlaceholderAPI! Placeholders are disabled.");
        }

        this.getCommand("skypvp").setExecutor(new CmdSkyPvp(this));
        this.getCommand("skyarena").setExecutor(new CmdArena(this));
        this.getCommand("skykit").setExecutor(new CmdKit(this));
        this.getCommand("skystats").setExecutor(new CmdStats(this));

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
}
