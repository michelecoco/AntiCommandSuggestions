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

    private AntiCommandSuggestionsPlugin main;

    private boolean worldGuardInstalled, residenceInstalled;

    // Configuration values
    private boolean blacklist;
    private List<String> worldGuardRegions, residenceRegions, commands;
    private List<Sound> sounds;

    public PlayerCommandPreprocessListener(AntiCommandSuggestionsPlugin main) {
        this.main = main;

        reload();
    }

    public void reload() {
        // Dynamic plugins loading support
        worldGuardInstalled = isRunning("WorldGuard");
        residenceInstalled = isRunning("Residence");

        ConfigurationSection configSection = main.getConfig().getConfigurationSection("disable-commands-execution");

        blacklist = configSection.getBoolean("blacklist");
        worldGuardRegions = loadStringList(configSection, "worldguard-regions", worldGuardInstalled);
        residenceRegions = loadStringList(configSection, "residence-regions", residenceInstalled);
        commands = configSection.getStringList("commands");
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

    private List<String> loadStringList(ConfigurationSection configSection, String listKey, boolean load) {
        return load ? configSection.getStringList(listKey) : Collections.emptyList();
    }

    @SuppressWarnings("unused")
    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!worldGuardInstalled && !residenceInstalled)
            return;

        String fullCommand = event.getMessage().substring(1).toLowerCase();

        if (restrictedCommand(fullCommand)) {
            Player player = event.getPlayer();
            boolean playerInsideRegion = isInsideRegion(player.getLocation());

            if ((blacklist && playerInsideRegion) || (!blacklist && !playerInsideRegion)) {
                event.setCancelled(true);
                ChatUtil.sendMessage(player, "command-blocked");

                Location playerLoc = player.getLocation();
                sounds.forEach(sound -> playerLoc.getWorld().playSound(playerLoc, sound, 0.5F, 1F));
            }
        }
    }

    private boolean restrictedCommand(String command) {
        Stream<String> commandsStream = commands.stream();
        if (command.contains(" ")) {
            return commandsStream.anyMatch(command::startsWith);
        } else {
            return commandsStream.anyMatch(command::equals);
        }
    }

    private boolean isInsideRegion(Location location) {
        return (worldGuardInstalled && !Collections.disjoint(worldGuardRegions, RegionsUtil.getRegionsNames(location)))
                || (residenceInstalled && residenceRegions.contains(RegionsUtil.getResidenceRegionName(location)));
    }

}

