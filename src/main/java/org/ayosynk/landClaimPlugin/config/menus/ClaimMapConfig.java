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
    public ItemConfig wilderness = new ItemConfig("WHITE_STAINED_GLASS_PANE", "<gray>Wilderness",
            List.of("<gray>Click to claim this chunk"));

    @Comment("Your Claim Tile")
    public ItemConfig yourClaim = new ItemConfig("LIME_STAINED_GLASS_PANE", "<green>Your Claim",
            List.of("<gray>Click to unclaim this chunk"));

    @Comment("Other Player's Claim Tile")
    public ItemConfig otherClaim = new ItemConfig("RED_STAINED_GLASS_PANE", "<red>Other Claim",
            List.of("<gray>Owned by <owner>"));

    @Comment("Ally's Claim Tile")
    public ItemConfig allyClaim = new ItemConfig("YELLOW_STAINED_GLASS_PANE", "<gold>Ally Claim",
            List.of("<gray>Owned by <owner>"));

    @Comment("Member/Trusted Claim Tile")
    public ItemConfig memberClaim = new ItemConfig("CYAN_STAINED_GLASS_PANE", "<aqua>Member Claim",
            List.of("<gray>Owned by <owner>"));

    @Comment("Map Display Background (Fallback)")
    public ItemConfig mapFill = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom Control Bar Background")
    public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Current Position Marker")
    public ItemConfig currentPos = new ItemConfig("MAGENTA_STAINED_GLASS_PANE", "<light_purple>You are here",
            List.of());

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
