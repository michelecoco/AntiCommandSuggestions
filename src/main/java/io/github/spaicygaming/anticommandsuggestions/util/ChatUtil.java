package io.github.spaicygaming.anticommandsuggestions.util;

import io.github.spaicygaming.anticommandsuggestions.AntiCommandSuggestionsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ChatUtil {

    /**
     * Prevent utility class initialization
     */
    private ChatUtil() {
    }

    /**
     * Main class instance
     */
    private static AntiCommandSuggestionsPlugin main = AntiCommandSuggestionsPlugin.getInstance();

    private static ConsoleCommandSender console = main.getServer().getConsoleSender();

    /**
     * The chat prefix
     */
    private static String chatPrefix;

    public static void loadChatPrefix() {
        chatPrefix = colorString(main.getConfig().getString("Messages.chat-prefix"));
    }


    /**
     * Send a message to the ConsoleCommandSender using the "startup" format.
     *
     * @param message The message to send
     */
    public static void sendStartupMessage(String message) {
        console.sendMessage(ChatColor.AQUA + "[AntiCommandSuggestions] " + ChatColor.GREEN + message);
    }

    /**
     * Send an alert message to the ConsoleSender
     *
     * @param message The message to display
     */
    public static void alertConsole(String message) {
        console.sendMessage("[AntiCommandSuggestions] " + ChatColor.RED + message);
    }

    /**
     * Color the String translating it to ChatColor using the character '&'
     *
     * @param string the String to color
     * @return the colored string
     */
    private static String colorString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Color the list of strings by translating it to ChatColor using the character '&'
     *
     * @param strings the list to color
     * @return the colored list
     */
    public static List<String> colorList(List<String> strings) {
        return new ArrayList<>(strings).stream().map(ChatUtil::colorString).collect(Collectors.toList());
    }

    /**
     * Get the String from the "Messages" section in config.yml and affix the chat prefix
     *
     * @param messageName the name of the message
     * @return the colored message preceded by the chat prefix
     */
    public static String getMessage(String messageName) {
        return chatPrefix + getMessageNoPrefix(messageName);
    }

    /**
     * Get the String from the messages section in the config.yml.
     * Doesn't affix the prefix. Instead use {@link #getMessage(String)}
     *
     * @param messageName the message key
     * @return the colored message
     */
    public static String getMessageNoPrefix(String messageName) {
        return colorString(main.getConfig().getString("Messages." + messageName));
    }

    public static void sendMessage(CommandSender user, String messageKey) {
        user.sendMessage(getMessage(messageKey));
    }
}
