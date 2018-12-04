package io.github.spaicygaming.anticommandsuggestions.commands;

import io.github.spaicygaming.anticommandsuggestions.AntiCommandSuggestionsPlugin;
import io.github.spaicygaming.anticommandsuggestions.util.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ACSCommandExecutor implements CommandExecutor {

    // Main class instance
    private AntiCommandSuggestionsPlugin main;

    public ACSCommandExecutor(AntiCommandSuggestionsPlugin main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {

        // Configuration reload command
        if (args.length == 1 && args[0].equals("reload")) {
            String messageKey = "reload-no-perms";

            if (sender.hasPermission("acs.reload")) {
                main.reload();
                messageKey = "reload-success";
            }
            ChatUtil.sendMessage(sender, messageKey);
            return true;
        }
        // Invalid command
        else {
            ChatUtil.sendMessage(sender, "commands-usage");
        }

        return false;
    }
}
