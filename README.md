***Greater SkyPvP***

This plugin was created as a request over on the good old Bukkit forums. I decided to release it since there may be others who are looking for something similar.

**General Features**

-   Create and manage kits useable by players
    -   Option to replace all inventory items
    -   Option to equip offhand and armor on kits that do not replace all inventory items
    -   Kit timers to allow players to be given kits for a certain period of time
-   Create and manage arenas for 1v1 duels
-   Basic player statistics
    -   Kills
    -   Deaths
-   PlaceholderAPI
    -   Obtain statistics and current kit
    -   Pull data relevant to time (ex: kills within the past day)
-   Custom messages for responses to players
-   Void instant kill per world

**Commands**

-   /skypvp - Admin command base
    -   /skypvp kit create <kitName> - Creates a kit based on the sender's inventory
    -   /skypvp kit delete <kitName> - Deletes the specified kit
    -   /skypvp setvoidkill <worldName> <true/false> - Sets if the specified world should instantly kill players in the void
    -   /skypvp arena create <arenaName> - Creates an arena with the specified name
    -   /skypvp arena delete <arenaName> - Deletes the specified arena
    -   /skypvp arena set <arenaName> <0/1/2> - Sets the spawn points of the arena
        -   0 - Lobby spawn
        -   1 - Player 1 spawn
        -   2 - Player 2 spawn
-   /skykit - Base command for kits
    -   /skykit <kitName> [playerName] [timeInSeconds] - Give the specified kit to a player or to self with an optional timer
        -   Player and timer option are only available to those with the admin permission.
-   /skyarena - Base command for arenas
    -   /skyarena <arenaName> [playerName] - Join or send a player to an arena lobby
        -   Player option is only available to those with the admin permission.
-   /skystats - Base command for stats
    -   /skystats [playerName] - Display personal stats or those of the specified player.

**Permissions**

-   greaterskypvp.default
    -   Access to /skykit and /skyarena base usage
-   greaterskypvp.admin
    -   Access to all commands

**PlaceholderAPI**

-   %greaterskypvp_kills%
    -   Retrieve kills statistic
-   %greaterskypvp_deaths%
    -   Retrieve deaths statistic
-   %greaterskypvp_kit%
    -   Retrieve current kit
-   %greaterskypvp_kills_<time>%
    -   Retrieve kills from within the specified time frame (days, hours, minutes)
        -   Days: %greaterskypvp_kills_#d%
        -   Hours: %greaterskypvp_kills_#h%
        -   Minutes: %greaterskypvp_kills_#m%
-   %greaterskypvp_deaths_<time>%
    -   Retrieve deaths from within the specified time frame (days, hours, minutes)
        -   Days: %greaterskypvp_deaths_#d%
        -   Hours: %greaterskypvp_deaths_#h%
        -   Minutes: %greaterskypvp_deaths_#m%

**Notes**

My plugin work is made as a hobby and I am just getting back into the Spigot API with this development. If there are any issues I will gladly give support and fix them. I am also open to most, if not all, feature suggestions. I understand the feature set is basic at the moment and my code may not be the prettiest but I have many plans to add features and refactor what is currently there. Thank you and enjoy the plugin!
