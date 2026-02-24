package org.ayosynk.landClaimPlugin.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ChatUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String parse(String text) {
        if (text == null)
            return "";
        try {
            var comp = MiniMessage.miniMessage().deserialize(text);
            return LegacyComponentSerializer.legacySection().serialize(comp);
        } catch (Exception e) {
            return colorize(text);
        }
    }

    public static String colorize(String text) {
        if (text == null)
            return "";

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }
}