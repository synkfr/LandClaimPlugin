package org.ayosynk.landClaimPlugin.util;

import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.util.UUID;

public final class GeyserHelper {

    private static Method geyserIsBedrockMethod = null;
    private static Object geyserApiInstance = null;
    
    private static Method floodgateIsBedrockMethod = null;
    private static Object floodgateApiInstance = null;

    private static boolean initialized = false;

    private GeyserHelper() {}

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;

        // Try to load Geyser API
        try {
            Class<?> geyserApiClass = Class.forName("org.geysermc.geyser.api.GeyserApi");
            Method apiMethod = geyserApiClass.getMethod("api");
            geyserApiInstance = apiMethod.invoke(null);
            geyserIsBedrockMethod = geyserApiInstance.getClass().getMethod("isBedrockPlayer", UUID.class);
        } catch (Throwable ignored) {
            // Geyser API not available
        }

        // Try to load Floodgate API
        try {
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstanceMethod = floodgateApiClass.getMethod("getInstance");
            floodgateApiInstance = getInstanceMethod.invoke(null);
            floodgateIsBedrockMethod = floodgateApiInstance.getClass().getMethod("isBedrockPlayer", UUID.class);
        } catch (Throwable ignored) {
            // Floodgate API not available
        }
    }

    public static boolean isBedrockPlayer(Player player) {
        if (player == null) return false;
        return isBedrockPlayer(player.getUniqueId());
    }

    public static boolean isBedrockPlayer(UUID uuid) {
        if (uuid == null) return false;
        init();

        if (geyserIsBedrockMethod != null && geyserApiInstance != null) {
            try {
                Object result = geyserIsBedrockMethod.invoke(geyserApiInstance, uuid);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (Throwable ignored) {}
        }

        if (floodgateIsBedrockMethod != null && floodgateApiInstance != null) {
            try {
                Object result = floodgateIsBedrockMethod.invoke(floodgateApiInstance, uuid);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (Throwable ignored) {}
        }

        return false;
    }
}
