package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("ClaimMap GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class ClaimMapConfig extends OkaeriConfig {

    public String title = "Claim Map";

    @Comment("Wilderness Legend Tile")
    public ItemConfig wilderness = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Map Display Background")
    public ItemConfig mapFill = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom Control Bar Background")
    public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Refresh Map View Button")
    public ItemConfig refresh = new ItemConfig("FLOW_POTTERY_SHERD", "Refresh", List.of());

    @Comment("Back to Main Menu Button")
    public ItemConfig back = new ItemConfig("SPECTRAL_ARROW", "Back", List.of());

    @Comment("Map Information Button")
    public ItemConfig info = new ItemConfig(
            "WRITABLE_BOOK",
            "Info",
            List.of("See what the color means what"));

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
