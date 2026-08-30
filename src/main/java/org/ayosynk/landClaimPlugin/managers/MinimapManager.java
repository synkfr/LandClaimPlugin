package org.ayosynk.landClaimPlugin.managers;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.PluginConfig;
import org.ayosynk.landClaimPlugin.map.TerritoryMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MinimapManager {

    private final LandClaimPlugin plugin;
    private final NamespacedKey mapKey;
    private final TerritoryMapRenderer renderer;
    private MapView mapView;

    public MinimapManager(LandClaimPlugin plugin) {
        this.plugin = plugin;
        this.mapKey = new NamespacedKey(plugin, "territory_map");
        this.renderer = new TerritoryMapRenderer(plugin);
        initMapView();
    }

    public void initMapView() {
        PluginConfig.MinimapConfig config = plugin.getConfigManager().getPluginConfig().minimap;
        if (!config.enabled) return;

        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (world == null) return;

        if (config.mapId >= 0) {
            mapView = Bukkit.getMap(config.mapId);
        }

        if (mapView == null) {
            mapView = Bukkit.createMap(world);
            config.mapId = mapView.getId();
            plugin.getConfigManager().getPluginConfig().save();
        }

        mapView.setTrackingPosition(false);
        mapView.setUnlimitedTracking(false);
        mapView.setScale(MapView.Scale.CLOSE);
        mapView.getRenderers().clear();
        mapView.addRenderer(renderer);
    }

    public ItemStack createMinimapItem(Player player) {
        if (mapView == null) {
            initMapView();
        }

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        if (meta != null) {
            if (mapView != null) {
                meta.setMapView(mapView);
            }

            PluginConfig.MinimapConfig config = plugin.getConfigManager().getPluginConfig().minimap;
            meta.displayName(MiniMessage.miniMessage().deserialize(config.itemName));

            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : config.itemLore) {
                lore.add(MiniMessage.miniMessage().deserialize(line));
            }
            meta.lore(lore);

            meta.getPersistentDataContainer().set(mapKey, PersistentDataType.BYTE, (byte) 1);
            mapItem.setItemMeta(meta);
        }
        return mapItem;
    }

    public boolean isMinimapItem(ItemStack item) {
        if (item == null || item.getType() != Material.FILLED_MAP || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(mapKey, PersistentDataType.BYTE);
    }

    public void giveOrOpenMinimap(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMinimapItem(item)) {
                player.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<gold>[LandClaim]</gold> <yellow>You already have a Territory Map in your inventory!</yellow>"));
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                return;
            }
        }

        ItemStack mapItem = createMinimapItem(player);
        if (player.getInventory().getItemInOffHand().getType() == Material.AIR) {
            player.getInventory().setItemInOffHand(mapItem);
        } else {
            player.getInventory().addItem(mapItem);
        }

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<gold>[LandClaim]</gold> <green>Received the <yellow>Territory Map</yellow>! Hold it in your hand or off-hand to view nearby claims in real time.</green>"));
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
    }

    public void toggleZoom(Player player) {
        TerritoryMapRenderer.ZoomLevel newZoom = renderer.toggleZoom(player.getUniqueId());
        String modeName = newZoom == TerritoryMapRenderer.ZoomLevel.OVERVIEW ? "16x16 Chunks (Overview)" : "8x8 Chunks (Close Detail)";
        player.sendActionBar(MiniMessage.miniMessage().deserialize("<yellow>Map Zoom: <gold>" + modeName + "</gold></yellow>"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, newZoom == TerritoryMapRenderer.ZoomLevel.OVERVIEW ? 1.0f : 1.5f);
    }

    public TerritoryMapRenderer getRenderer() {
        return renderer;
    }
}
