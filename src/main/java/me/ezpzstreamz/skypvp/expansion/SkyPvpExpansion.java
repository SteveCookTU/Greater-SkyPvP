package me.ezpzstreamz.skypvp.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ezpzstreamz.skypvp.GreaterSkyPvpPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class SkyPvpExpansion extends PlaceholderExpansion {

    private final GreaterSkyPvpPlugin plugin;

    public SkyPvpExpansion(GreaterSkyPvpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "greaterskypvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) {
            return "";
        }

        if(params.equals("deaths"))
            return Integer.toString(plugin.getStatManager().getDeaths(player, null));

        if(params.equals("kills"))
            return Integer.toString(plugin.getStatManager().getKills(player, null));

        if(params.equals("kit")) {
            return plugin.getKitManager().getAssignedKitName(player.getUniqueId().toString());
        }

        if(params.split("_").length == 2) {
            String[] args = params.split("_");
            if(args[0].equals("kills")) {
                if(args[1].endsWith("d")) {
                    int days = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getKills(player, LocalDateTime.now().minusDays(days)));
                } else if(args[1].endsWith("h")) {
                    int hours = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getKills(player, LocalDateTime.now().minusHours(hours)));
                } else if(args[1].endsWith("m")) {
                    int minutes = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getKills(player, LocalDateTime.now().minusMinutes(minutes)));
                }
            } else if(args[0].equals("deaths")) {
                if(args[1].endsWith("d")) {
                    int days = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getDeaths(player, LocalDateTime.now().minusDays(days)));
                } else if(args[1].endsWith("h")) {
                    int hours = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getDeaths(player, LocalDateTime.now().minusHours(hours)));
                } else if(args[1].endsWith("m")) {
                    int minutes = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                    return Integer.toString(plugin.getStatManager().getDeaths(player, LocalDateTime.now().minusMinutes(minutes)));
                }
            }
        }

        return null;
    }
}
