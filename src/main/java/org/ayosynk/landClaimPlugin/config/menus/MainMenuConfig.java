package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("MainMenu GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class MainMenuConfig extends OkaeriConfig {

    public String title = "Claim: {claim_name}";

    @Comment("The filler item surrounding the menu")
    public ItemConfig filler = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Claim Information Button")
    public ItemConfig claimInfo = new ItemConfig(
            "WRITTEN_BOOK",
            "<yellow>Claim Information",
            List.of(
                    "<gray>Owner: <white>{owner}",
                    "<gray>Size: <white>{size} chunks",
                    "<gray>Power usage: <white>{power}",
                    "<gray>Members count: <white>{members}",
                    "",
                    "<yellow>Click to view details"));

    @Comment("Claim Map Button")
    public ItemConfig claimMap = new ItemConfig(
            "FILLED_MAP",
            "<green>Claim Map",
            List.of(
                    "<gray>Opens visual chunk overview",
                    "<gray>Shows nearby claims & highlights borders",
                    "",
                    "<yellow>Click to open map (Soon)"));

    @Comment("Members Button")
    public ItemConfig members = new ItemConfig(
            "PLAYER_HEAD",
            "<aqua>Members",
            List.of(
                    "<gray>Shows all claim members",
                    "",
                    "<yellow>Click to manage"));

    @Comment("Trusted Players Button")
    public ItemConfig trusted = new ItemConfig(
            "TOTEM_OF_UNDYING",
            "<gold>Trusted Players",
            List.of(
                    "<gray>Players who bypass protections",
                    "",
                    "<yellow>Click to manage"));

    @Comment("Visitor Settings Button")
    public ItemConfig visitors = new ItemConfig(
            "EMERALD",
            "<green>Visitor Settings",
            List.of(
                    "<gray>Configure what visitors can do",
                    "",
                    "<yellow>Click to configure"));

    @Comment("Claim Warps Button")
    public ItemConfig warps = new ItemConfig(
            "RECOVERY_COMPASS",
            "<light_purple>Claim Warps",
            List.of(
                    "<gray>Create and teleport to warps",
                    "",
                    "<yellow>Click to manage warps"));

    @Comment("Claim Settings Button")
    public ItemConfig settings = new ItemConfig(
            "FLOWER_BANNER_PATTERN",
            "<yellow>Claim Settings",
            List.of(
                    "<gray>Core configuration",
                    "<gray>Durable system settings",
                    "",
                    "<yellow>Click to open settings"));

    @Comment("Center Claim Display Icon (Anchor)")
    public ItemConfig claimAnchor = new ItemConfig(
            "GRASS_BLOCK",
            "<green>{claim_name}",
            List.of(
                    "<gray>World: <white>{world}",
                    "<gray>Location: <white>{x}, {z}",
                    "<gray>Chunk size: <white>{size}"));

    @Comment("Close Menu Button")
    public ItemConfig close = new ItemConfig(
            "BARRIER",
            "<red>Close",
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
