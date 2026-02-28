package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("WarpControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class WarpControlPanelConfig extends OkaeriConfig {

    public String title = "Warp Control Panel";

    @Comment("Locked UI background")
    public ItemConfig frame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Visual separation")
    public ItemConfig spacer = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Change Location Button")
    public ItemConfig changeLocation = new ItemConfig(
            "LIME_BED",
            "<green>Change Location",
            List.of("<gray>Update the Location with", "<gray>your current location"));

    @Comment("Change Icon Button")
    public ItemConfig changeIcon = new ItemConfig(
            "ITEM_FRAME",
            "<yellow>Change Icon",
            List.of("<gray>Select a new icon for the warp"));

    @Comment("Delete Warp Button")
    public ItemConfig deleteWarp = new ItemConfig(
            "BARRIER",
            "<red><bold>Delete Warp",
            List.of("<gray>Permanently delete this warp."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Warp Management"));

    public static class ItemConfig extends OkaeriConfig {
        public String material;
        public String name;
        public List<String> lore;

        public ItemConfig() {
        } // For Okaeri to instantiate

        public ItemConfig(String material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }
    }
}
