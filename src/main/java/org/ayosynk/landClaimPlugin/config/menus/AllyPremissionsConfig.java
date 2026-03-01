package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("AllyPremissions GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class AllyPremissionsConfig extends OkaeriConfig {

    public String title = "Ally Premissions";

    @Comment("UI border framing")
    public ItemConfig frame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation background")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Information Button")
    public ItemConfig info = new ItemConfig(
            "NETHER_STAR",
            "<yellow><bold>Permission Info",
            List.of("<gray>Toggle permissions for this allied claim."));

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

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Ally Control Panel"));

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
