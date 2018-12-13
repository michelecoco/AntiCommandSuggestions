package io.github.spaicygaming.anticommandsuggestions.listeners;

import io.github.spaicygaming.anticommandsuggestions.AntiCommandSuggestionsPlugin;
import io.github.spaicygaming.anticommandsuggestions.util.ChatUtil;
import io.github.spaicygaming.anticommandsuggestions.util.RegionsUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerCommandPreprocessListener implements Listener {

    // Main class instance
    private AntiCommandSuggestionsPlugin main;

    // Whether soft-dependencies are running on the server
    private boolean worldGuardInstalled, residenceInstalled;

    // Configuration values
    private boolean blacklist;
    private List<String> worldGuardRegions, residenceRegions, commands, messages;
    private List<Sound> sounds;

    /**
     * Initialize the class and load configuration values by calling {@link #reload()}
     *
     * @param main main class instance
     */
    public PlayerCommandPreprocessListener(AntiCommandSuggestionsPlugin main) {
        this.main = main;

        reload();
    }

    public void reload() {
        // Dynamic plugins loading support
        worldGuardInstalled = isRunning("WorldGuard");
        residenceInstalled = isRunning("Residence");

        // Load configuration values
        ConfigurationSection configSection = main.getConfig().getConfigurationSection("disable-commands-execution");

        blacklist = configSection.getBoolean("blacklist");
        worldGuardRegions = loadStringList(configSection, "worldguard-regions", worldGuardInstalled);
        residenceRegions = loadStringList(configSection, "residence-regions", residenceInstalled);
        commands = configSection.getStringList("commands").stream().map(String::toLowerCase).collect(Collectors.toList());
        messages = ChatUtil.colorList(configSection.getStringList("messages"));
        sounds = configSection.getStringList("sounds").stream().map(Sound::valueOf).collect(Collectors.toList());
    }

    /**
     * Check whether a plugin is installed and running
     *
     * @param pluginName The name of the plugin
     * @return true if it is
     */
    private boolean isRunning(String pluginName) {
        Plugin plugin = main.getServer().getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    /**
     * Get a list of strings from the configuration file.
     *
     * @param configSection the configuration section containing the list
     * @param listKey       the key the list is associated with
     * @param load          whether to return the specified list or {@link Collections#emptyList()}
     * @return an empty list if load is false
     */
    private List<String> loadStringList(ConfigurationSection configSection, String listKey, boolean load) {
        return load ? configSection.getStringList(listKey) : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        // Return if
        if (!worldGuardInstalled && !residenceInstalled)
            return;

        // Return if the player has bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("acs.bypass.cmd"))
            return;

        String fullCommand = event.getMessage().substring(1).toLowerCase();

        // blacklist true: prevent players from executing restricted commands while INSIDE the regions listed in the config
        // blacklist false: prevent players from executing restricted commands while OUTSIDE listed regions
        if (restrictedCommand(fullCommand)) {
            boolean playerInsideRegion = isInsideRegion(player.getLocation());

            if ((blacklist && playerInsideRegion) || (!blacklist && !playerInsideRegion)) {
                event.setCancelled(true);
                // Send message(s)
                messages.forEach(player::sendMessage);

                // Play sounds
                Location playerLoc = player.getLocation();
                sounds.forEach(sound -> playerLoc.getWorld().playSound(playerLoc, sound, 0.5F, 1F));
            }
        }
    }

    /**
     * Check whether the use of this command is restricted
     *
     * @param command the command to check
     * @return true if it is
     */
    private boolean restrictedCommand(String command) {
        String cmdToCheck = command;
        Stream<String> commandsStream = commands.stream();

        if (command.contains(" ")) {
            cmdToCheck = command.split(" ")[0];
        }
        return commandsStream.anyMatch(cmdToCheck::equals);
    }

    /**
     * Return true if the give location is inside a WorldGuard or Residence region listed in the configuration file.
     * Return false if neither WorldGuard nor Residence are running on the server.
     *
     * @param location the location to check
     * @return false if outside
     */
    private boolean isInsideRegion(Location location) {
        return (worldGuardInstalled && !Collections.disjoint(worldGuardRegions, RegionsUtil.getRegionsNames(location)))
                || (residenceInstalled && residenceRegions.contains(RegionsUtil.getResidenceRegionName(location)));
    }

}

