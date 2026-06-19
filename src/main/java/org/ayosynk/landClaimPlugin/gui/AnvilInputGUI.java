package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.function.Consumer;

public class AnvilInputGUI implements Listener {

    private final LandClaimPlugin plugin;
    private final Player player;
    private final Consumer<String> callback;
    private final AnvilView view;
    private boolean isClosed = false;

    public AnvilInputGUI(LandClaimPlugin plugin, Player player, String title, String initialText, Consumer<String> callback) {
        this.plugin = plugin;
        this.player = player;
        this.callback = callback;

        this.view = MenuType.ANVIL.builder()
                .title(Component.text(title))
                .build(player);

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(initialText));
            item.setItemMeta(meta);
        }
        this.view.getTopInventory().setItem(0, item);

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(this.view);
    }

    @EventHandler
    public void onPrepareAnvil(org.bukkit.event.inventory.PrepareAnvilEvent event) {
        if (!event.getView().equals(this.view)) return;
        
        ItemStack resultItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = resultItem.getItemMeta();
        if (meta != null) {
            meta.displayName(GuiHelper.MM.deserialize("<i:false><color:#30FF60>✔ Save</color>"));
            resultItem.setItemMeta(meta);
        }
        event.setResult(resultItem);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().equals(this.view)) return;

        event.setCancelled(true);

        if (event.getRawSlot() == 2) { // Output slot
            String text = this.view.getRenameText();

            if (text != null && !text.isEmpty()) {
                closeAndCallback(text);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getView().equals(this.view)) return;
        event.getInventory().clear();
        closeAndCallback(null);
    }

    private void closeAndCallback(String result) {
        if (isClosed) return;
        isClosed = true;
        HandlerList.unregisterAll(this);
        
        // Clear items to prevent dropping/returning to player
        view.getTopInventory().clear();

        // Delay callback slightly so inventory close processes finish
        // Route through the player's region thread on Folia, main thread on Paper.
        org.ayosynk.landClaimPlugin.util.FoliaScheduler.runForPlayer(plugin, player, () -> {
            if (player.getOpenInventory().getTopInventory().equals(view.getTopInventory())) {
                player.closeInventory();
            }
            callback.accept(result);
        });
    }

    public static void open(LandClaimPlugin plugin, Player player, String title, String initialText, Consumer<String> callback) {
        // Bedrock player path: prefer a native Geyser CustomForm text input if available.
        // Falls back to the chat-prompt listener if Geyser forms are disabled or unavailable.
        if (org.ayosynk.landClaimPlugin.util.GeyserHelper.isBedrockPlayer(player)) {
            if (plugin.getConfigManager().getPluginConfig().geyserForms
                    && org.ayosynk.landClaimPlugin.util.GeyserFormHelper.isAvailable()) {
                player.closeInventory();
                player.sendMessage(plugin.getConfigManager().getMessage("geyser-form-sent", "<title>", title));

                org.ayosynk.landClaimPlugin.util.GeyserFormHelper.sendCustomFormTextInput(
                        player, title, "", title, initialText, result -> {
                            // Route the callback back through the player's region thread on Folia.
                            org.ayosynk.landClaimPlugin.util.FoliaScheduler.runForPlayer(plugin, player, () -> {
                                if (result == null) {
                                    player.sendMessage(plugin.getConfigManager().getMessage("geyser-form-cancelled"));
                                    callback.accept(null);
                                } else {
                                    callback.accept(result);
                                }
                            });
                        });
                return;
            }

            // Chat-prompt fallback (no Geyser forms).
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("geyser-chat-prompt", "<title>", title));

            plugin.getServer().getPluginManager().registerEvents(new Listener() {
                private boolean handled = false;

                private void handleInput(Player p, String message, org.bukkit.event.Cancellable event) {
                    if (handled) return;
                    handled = true;
                    event.setCancelled(true);
                    HandlerList.unregisterAll(this);

                    if (message.equalsIgnoreCase("cancel")) {
                        p.sendMessage(plugin.getConfigManager().getMessage("geyser-chat-cancelled"));
                        callback.accept(null);
                    } else {
                        callback.accept(message);
                    }
                }

                @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
                public void onLegacyChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
                    if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                        handleInput(event.getPlayer(), event.getMessage(), event);
                    }
                }

                @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
                public void onModernChat(io.papermc.paper.event.player.AsyncChatEvent event) {
                    if (event.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                        String plainText = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
                        handleInput(event.getPlayer(), plainText, event);
                    }
                }
            }, plugin);
        } else {
            new AnvilInputGUI(plugin, player, title, initialText, callback);
        }
    }
}
