package io.github.spaicygaming.anticommandsuggestions;

import io.github.spaicygaming.anticommandsuggestions.commands.ACSCommandExecutor;
import io.github.spaicygaming.anticommandsuggestions.listeners.PlayerCommandPreprocessListener;
import io.github.spaicygaming.anticommandsuggestions.listeners.PlayerCommandSendListener;
import io.github.spaicygaming.anticommandsuggestions.util.ChatUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiCommandSuggestionsPlugin extends JavaPlugin {

    private static AntiCommandSuggestionsPlugin INSTANCE;

    private PlayerCommandSendListener playerCommandSendListener;
    private PlayerCommandPreprocessListener playerCommandPreprocessListener;

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Disable if server version prior to 1.13
        String serverVersion = getServer().getBukkitVersion().split("-")[0];
        if (Double.parseDouble(serverVersion.split("\\.")[1]) < 13) {
            ChatUtil.alertConsole(String.format("The plugin isn't compatible with this server version! (%s)", serverVersion));
            ChatUtil.alertConsole("Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        ChatUtil.loadChatPrefix();

        // Register listeners
        playerCommandSendListener = new PlayerCommandSendListener(this);
        getServer().getPluginManager().registerEvents(playerCommandSendListener, this);
        playerCommandPreprocessListener = new PlayerCommandPreprocessListener(this);
        getServer().getPluginManager().registerEvents(playerCommandPreprocessListener, this);

        // Register command
        getCommand("anticommandsuggestions").setExecutor(new ACSCommandExecutor(this));

        ChatUtil.sendStartupMessage("AntiCommandSuggestions has been enabled!");
    }

    public static AntiCommandSuggestionsPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiCommandSuggestions has been disabled :(");
    }

    public void reload() {
        super.reloadConfig();
        ChatUtil.loadChatPrefix();
        playerCommandSendListener.reload();
        playerCommandPreprocessListener.reload();
    }

}
