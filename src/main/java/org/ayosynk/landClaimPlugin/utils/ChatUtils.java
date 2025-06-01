package org.ayosynk.landClaimPlugin.utils;

import net.md_5.bungee.api.ChatColor;

public class ChatUtils {
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}