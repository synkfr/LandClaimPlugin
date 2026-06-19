package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("PublicWarps GUI Configuration")
@Header("Shown to any player via /claim publicwarps — lists every warp that")
@Header("has been marked public by its owner, server-wide.")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class PublicWarpsConfig extends OkaeriConfig {

    public String title = "Public Warps";

    @Comment("Locked UI background")
    public ItemConfig frame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Visual separation")
    public ItemConfig navFill = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Back button — return to the main menu.")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to the main menu"));

    public ItemConfig previousPage = new ItemConfig(
            "ARROW",
            "<gray>Previous Page",
            List.of());

    public ItemConfig nextPage = new ItemConfig(
            "ARROW",
            "<gray>Next Page",
            List.of());

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
