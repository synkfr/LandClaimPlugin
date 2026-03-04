package org.ayosynk.landClaimPlugin.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Reusable chat-input capture utility.
 * <p>
 * Call {@link #awaitInput(Player, Consumer)} to register a one-shot callback.
 * The next chat message from that player is intercepted (event cancelled),
 * the raw text is passed to the callback, and the listener self-cleans.
 * Typing "cancel" aborts and invokes the callback with {@code null}.
 */
public class ChatInputListener implements Listener {

    private static ChatInputListener instance;
    private static Plugin plugin;

    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    private ChatInputListener() {
    }

    /**
     * Must be called once during plugin enable.
     */
    public static void init(Plugin pluginRef) {
        plugin = pluginRef;
        instance = new ChatInputListener();
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    /**
     * Register a one-shot chat input callback for the given player.
     *
     * @param player   the player to listen for
     * @param callback receives the typed text, or {@code null} if cancelled
     */
    public static void awaitInput(Player player, Consumer<String> callback) {
        if (instance == null)
            throw new IllegalStateException("ChatInputListener.init() was not called!");
        instance.pending.put(player.getUniqueId(), callback);
    }

    /**
     * Cancel a pending input request without invoking the callback.
     */
    public static void cancel(UUID playerId) {
        if (instance != null) {
            instance.pending.remove(playerId);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncChatEvent event) {
        Consumer<String> callback = pending.remove(event.getPlayer().getUniqueId());
        if (callback == null)
            return;

        event.setCancelled(true);
        String text = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();

        if (text.equalsIgnoreCase("cancel")) {
            callback.accept(null);
        } else {
            callback.accept(text);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
