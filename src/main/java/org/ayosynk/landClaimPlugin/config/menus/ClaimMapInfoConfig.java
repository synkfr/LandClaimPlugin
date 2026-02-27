package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("ClaimMapInfo GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class ClaimMapInfoConfig extends OkaeriConfig {

    public String title = "Claim Map Info";

    @Comment("Decorative background")
    public ItemConfig filler = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom visual framing")
    public ItemConfig filler1 = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Player-owned territory")
    public ItemConfig owned = new ItemConfig(
            "LIME_STAINED_GLASS_PANE",
            "<green><bold>Owned Claim",
            List.of("<gray>Territory currently owned by you", "<gray>or your claim members."));

    @Comment("Allied claims")
    public ItemConfig ally = new ItemConfig(
            "MAGENTA_STAINED_GLASS_PANE",
            "<light_purple><bold>Ally Claim",
            List.of("<gray>Territory owned by your allies."));

    @Comment("Neutral players")
    public ItemConfig other = new ItemConfig(
            "LIGHT_BLUE_STAINED_GLASS_PANE",
            "<aqua><bold>Other Claim",
            List.of("<gray>Territory owned by neutral players."));

    @Comment("Enemy territory")
    public ItemConfig enemy = new ItemConfig(
            "RED_STAINED_GLASS_PANE",
            "<red><bold>Enemy Claim",
            List.of("<gray>Territory owned by your enemies."));

    @Comment("Unclaimed land")
    public ItemConfig wilderness = new ItemConfig(
            "WHITE_STAINED_GLASS_PANE",
            "<white><bold>Wilderness",
            List.of("<gray>Unclaimed land that is free to claim."));

    @Comment("Back Button")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to the Claim Map"));

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
