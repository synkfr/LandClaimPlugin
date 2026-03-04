package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("MainMenu GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class MainMenuConfig extends OkaeriConfig {

        public String title = "Claim: {claim_name}";

        @Comment("Filler 1")
        public ItemConfig filler1 = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Filler 2")
        public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Claim Map Button")
        public ItemConfig claimMap = new ItemConfig("MAP", "Claim Map", List.of());

        @Comment("Claim Warps Button")
        public ItemConfig warps = new ItemConfig("RED_BED", "Warps", List.of());

        @Comment("Allies Button")
        public ItemConfig allies = new ItemConfig("ENDER_CHEST", "Allies", List.of());

        @Comment("Claim Settings Button")
        public ItemConfig settings = new ItemConfig("EMERALD", "Settings", List.of());

        @Comment("Trusted Members Management Button")
        public ItemConfig trusted = new ItemConfig("COPPER_CHESTPLATE", "Trusted Members Management", List.of());

        @Comment("Members Management Button")
        public ItemConfig members = new ItemConfig("PLAYER_HEAD", "Members Management", List.of());

        @Comment("Visitor Settings Button")
        public ItemConfig visitors = new ItemConfig("SKULL_BANNER_PATTERN", "Visitor Settings", List.of());

        @Comment("Close Menu Button")
        public ItemConfig close = new ItemConfig("STRUCTURE_VOID", "close", List.of());

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
