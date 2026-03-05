package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("ClaimMapInfo GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class ClaimMapInfoConfig extends OkaeriConfig {

        public String title = "Claim Map Info";

        @Comment("Filler Item")
        public ItemConfig filler = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Filler Item 1")
        public ItemConfig filler1 = new ItemConfig("BLUE_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Your Owned Claim Legend")
        public ItemConfig owned = new ItemConfig("LIME_STAINED_GLASS_PANE", "<green>Your Claim",
                        List.of("<gray>Chunks you own"));

        @Comment("Ally's Claim Legend")
        public ItemConfig ally = new ItemConfig("YELLOW_STAINED_GLASS_PANE", "<gold>Ally Claim",
                        List.of("<gray>Chunks owned by allies"));

        @Comment("Other's Claim Legend")
        public ItemConfig other = new ItemConfig("RED_STAINED_GLASS_PANE", "<red>Other Claim",
                        List.of("<gray>Chunks owned by others"));

        @Comment("Member/Trusted's Claim Legend")
        public ItemConfig member = new ItemConfig("CYAN_STAINED_GLASS_PANE", "<aqua>Member Claim",
                        List.of("<gray>Chunks where you are a member/trusted"));

        @Comment("Enemy territory")
        public ItemConfig enemy = new ItemConfig(
                        "RED_STAINED_GLASS_PANE",
                        "<red><bold>Enemy Claim",
                        List.of("<gray>Territory owned by your enemies."));

        @Comment("Wilderness Legend")
        public ItemConfig wilderness = new ItemConfig("WHITE_STAINED_GLASS_PANE", "<gray>Wilderness",
                        List.of("<gray>Unclaimed land"));

        @Comment("Back to Map Button")
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
