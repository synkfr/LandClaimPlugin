package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

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

        @Comment("Text added to lore when a flag is enabled for this ally")
        public String enabledStatus = "<green>Enabled";

        @Comment("Text added to lore when a flag is disabled for this ally")
        public String disabledStatus = "<red>Disabled";

        @Comment("List of configurable ally flags.")
        public Map<String, ItemConfig> flags = new LinkedHashMap<>() {
                {
                        put("use", new ItemConfig("OAK_DOOR", "<gold>Use Blocks",
                                        List.of("<gray>Allow ally to use doors, buttons, etc.")));
                        put("interact", new ItemConfig("ARMOR_STAND", "<gold>Interact",
                                        List.of("<gray>Allow ally to interact with entities.")));
                        put("chest-access", new ItemConfig("CHEST", "<gold>Chest Access",
                                        List.of("<gray>Allow ally to open chests.")));
                        put("ride", new ItemConfig("SADDLE", "<gold>Ride Vehicles",
                                        List.of("<gray>Allow ally to ride horses/boats.")));
                        put("vehicle-place", new ItemConfig("OAK_BOAT", "<gold>Place Vehicles",
                                        List.of("<gray>Allow ally to place boats/minecarts.")));
                        put("vehicle-destroy", new ItemConfig("GOLDEN_SWORD", "<gold>Destroy Vehicles",
                                        List.of("<gray>Allow ally to break boats/minecarts.")));
                        put("drop-items", new ItemConfig("ROTTEN_FLESH", "<gold>Drop Items",
                                        List.of("<gray>Allow ally to drop items.")));
                        put("pickup-items", new ItemConfig("DIAMOND", "<gold>Pickup Items",
                                        List.of("<gray>Allow ally to pick up items.")));
                }
        };

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
