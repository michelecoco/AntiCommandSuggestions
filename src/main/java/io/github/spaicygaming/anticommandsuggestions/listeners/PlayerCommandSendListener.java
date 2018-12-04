package io.github.spaicygaming.anticommandsuggestions.listeners;

import com.google.common.collect.Sets;
import io.github.spaicygaming.anticommandsuggestions.AntiCommandSuggestionsPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerCommandSendListener implements Listener {

    /**
     * Main class instance
     */
    private AntiCommandSuggestionsPlugin main;

    /**
     * Whether to restrict spigot 1.13+ commands suggestions system
     */
    private boolean enabled;

    /**
     * Suggestions not associated with any permission. Every player can see them
     */
    private Set<String> defaultWhitelist;
    // Permission  - whitelisted commands
    /**
     * {@link Map} collection that contains {@link String} permissions as key and {@link Set<String>} as value.
     * Keys are the permissions players must have to be able to see the associated suggestions.
     */
    private Map<String, Set<String>> whitelists;

    /**
     * Initialize the class and load configuration values by calling {@link #reload()}
     *
     * @param main main class instance
     */
    public PlayerCommandSendListener(AntiCommandSuggestionsPlugin main) {
        this.main = main;

        reload();
    }

    /**
     * Load configuration values
     */
    public void reload() {
        enabled = main.getConfig().getBoolean("disable-commands-suggestions.enabled");
        defaultWhitelist = Sets.newHashSet(main.getConfig().getStringList("disable-commands-suggestions.whitelists.default"));

        ConfigurationSection suggestionSections = main.getConfig().getConfigurationSection("disable-commands-suggestions.whitelists.extra");
        whitelists = suggestionSections.getKeys(false).stream().collect(Collectors
                .toMap(key -> String.format("acs.whitelist.%s", key), key -> Sets.newHashSet(suggestionSections.getStringList(key))));

        whitelists.values().forEach(whitelist -> whitelist.addAll(defaultWhitelist));
    }

    @EventHandler
    void onPlayerCommandSend(PlayerCommandSendEvent event) {
        if (!enabled) return;

        // Return if the involved player has the bypass permission
        Player player = event.getPlayer();
        if (player.hasPermission("acs.bypass.tab"))
            return;

        // Collect the suggestions to show to the player based on his permissions
        List<String> extraSuggestions = whitelists.keySet().stream()
                .filter(player::hasPermission)
                .map(whitelists::get)
                .flatMap(Set::stream)
                .collect(Collectors.toList());

        // Don't show not-whitelisted suggestions
        if (extraSuggestions.isEmpty()) {
            if (defaultWhitelist.isEmpty())
                event.getCommands().clear();
            else
                event.getCommands().removeIf(line -> !defaultWhitelist.contains(line));
        } else {
            event.getCommands().removeIf(line -> !extraSuggestions.contains(line));
        }
    }

}
