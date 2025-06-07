package org.ayosynk.landClaimPlugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClaimTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        String cmd = command.getName().toLowerCase();
        List<String> completions = new ArrayList<>();

        if (cmd.equals("claim")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList(
                        "auto", "trust", "untrust", "unstuck",
                        "visible", "help", "reload"
                ));
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("trust") ||
                        args[0].equalsIgnoreCase("untrust")) {
                    // Return null to let Bukkit handle online player suggestions
                    return null;
                }
            }
        } else if (cmd.equals("unclaim")) {
            if (args.length == 1) {
                completions.add("auto");
            }
        }

        // Filter based on current input
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}