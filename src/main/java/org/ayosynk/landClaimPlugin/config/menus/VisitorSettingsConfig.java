package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("VisitorSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class VisitorSettingsConfig extends OkaeriConfig {

    public String title = "Visitor Settings";

    @Comment("Decorative border")
    public ItemConfig frame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation bar background")
    public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Explains visitor permissions")
    public ItemConfig info = new ItemConfig(
            "NETHER_STAR",
            "<yellow><bold>Visitor Info",
            List.of("<gray>Configure what visitors can do", "<gray>inside this claim.", "",
                    "<gray>These are default permissions."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Main Menu"));

    @Comment("Previous Page Button")
    public ItemConfig previousPage = new ItemConfig(
            "LIGHT_BLUE_CANDLE",
            "<aqua>Previous Page",
            List.of("<gray>Go to the previous page."));

    @Comment("Next Page Button")
    public ItemConfig nextPage = new ItemConfig(
            "LIME_CANDLE",
            "<green>Next Page",
            List.of("<gray>Go to the next page."));

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
