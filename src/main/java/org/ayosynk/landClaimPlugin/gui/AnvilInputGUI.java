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
        closeAndCallback(null);
    }

    private void closeAndCallback(String result) {
        if (isClosed) return;
        isClosed = true;
        HandlerList.unregisterAll(this);
        // Delay callback slightly so inventory close processes finish
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.getOpenInventory().getTopInventory().equals(view.getTopInventory())) {
                player.closeInventory();
            }
            callback.accept(result);
        });
    }

    public static void open(LandClaimPlugin plugin, Player player, String title, String initialText, Consumer<String> callback) {
        new AnvilInputGUI(plugin, player, title, initialText, callback);
    }
}
