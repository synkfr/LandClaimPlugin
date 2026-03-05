package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;
import java.util.Map;

@Header("VisitorSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class VisitorSettingsConfig extends OkaeriConfig {

        public String title = "Visitor Settings";

        @Comment("Decorative border")
        public ItemConfig frame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Navigation bar background")
        public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Text appended to lore when a flag is allowed")
        public String enabledStatus = "<green>▶ Allowed";

        @Comment("Text appended to lore when a flag is restricted")
        public String disabledStatus = "<red>▶ Restricted";

        @Comment("All configurable visitor flags and their display icons")
        public Map<String, ItemConfig> flags = Map.ofEntries(
                        // Block Flags
                        Map.entry("BLOCK_BREAK",
                                        new ItemConfig("IRON_PICKAXE", "<yellow>Block Break",
                                                        List.of("<gray>Allow visitors to break blocks."))),
                        Map.entry("BLOCK_PLACE",
                                        new ItemConfig("BRICK", "<yellow>Block Place",
                                                        List.of("<gray>Allow visitors to place blocks."))),
                        Map.entry("BLOCK_IGNITE",
                                        new ItemConfig("FLINT_AND_STEEL", "<yellow>Block Ignite",
                                                        List.of("<gray>Allow visitors to ignite blocks."))),
                        Map.entry("USE_BUCKETS",
                                        new ItemConfig("WATER_BUCKET", "<yellow>Use Buckets",
                                                        List.of("<gray>Allow visitors to use buckets."))),
                        Map.entry("USE_FERTILIZER",
                                        new ItemConfig("BONE_MEAL", "<yellow>Use Fertilizer",
                                                        List.of("<gray>Allow visitors to use bone meal."))),
                        Map.entry("MODIFY_SIGNS",
                                        new ItemConfig("OAK_SIGN", "<yellow>Modify Signs",
                                                        List.of("<gray>Allow visitors to edit signs."))),

                        // Interact Flags
                        Map.entry("USE_DOORS",
                                        new ItemConfig("OAK_DOOR", "<yellow>Use Doors",
                                                        List.of("<gray>Allow visitors to open doors."))),
                        Map.entry("USE_TRAPDOORS",
                                        new ItemConfig("OAK_TRAPDOOR", "<yellow>Use Trapdoors",
                                                        List.of("<gray>Allow visitors to open trapdoors."))),
                        Map.entry("USE_FENCE_GATES",
                                        new ItemConfig("OAK_FENCE_GATE", "<yellow>Use Fence Gates",
                                                        List.of("<gray>Allow visitors to open fence gates."))),
                        Map.entry("USE_CONTAINERS",
                                        new ItemConfig("CHEST", "<yellow>Use Containers",
                                                        List.of("<gray>Allow visitors to open chests, barrels, etc."))),
                        Map.entry("USE_WORKSTATIONS", new ItemConfig("CRAFTING_TABLE", "<yellow>Use Workstations",
                                        List.of("<gray>Allow visitors to use crafting tables, furnaces, etc."))),
                        Map.entry("USE_BEDS",
                                        new ItemConfig("RED_BED", "<yellow>Use Beds",
                                                        List.of("<gray>Allow visitors to sleep in beds."))),
                        Map.entry("USE_REDSTONE",
                                        new ItemConfig("LEVER", "<yellow>Use Redstone",
                                                        List.of("<gray>Allow visitors to use levers, buttons, etc."))),
                        Map.entry("USE_LECTERNS",
                                        new ItemConfig("LECTERN", "<yellow>Use Lecterns",
                                                        List.of("<gray>Allow visitors to read lecterns."))),
                        Map.entry("USE_BELLS",
                                        new ItemConfig("BELL", "<yellow>Use Bells",
                                                        List.of("<gray>Allow visitors to ring bells."))),

                        // Entity Flags
                        Map.entry("DAMAGE_ANIMALS",
                                        new ItemConfig("PORKCHOP", "<yellow>Damage Animals",
                                                        List.of("<gray>Allow visitors to hurt animals."))),
                        Map.entry("DAMAGE_MONSTERS",
                                        new ItemConfig("ZOMBIE_HEAD", "<yellow>Damage Monsters",
                                                        List.of("<gray>Allow visitors to hurt monsters."))),
                        Map.entry("BREED_ANIMALS",
                                        new ItemConfig("WHEAT", "<yellow>Breed Animals",
                                                        List.of("<gray>Allow visitors to breed animals."))),
                        Map.entry("SHEAR_ENTITIES",
                                        new ItemConfig("SHEARS", "<yellow>Shear Entities",
                                                        List.of("<gray>Allow visitors to shear sheep, etc."))),
                        Map.entry("TRADE_VILLAGERS",
                                        new ItemConfig("EMERALD", "<yellow>Trade with Villagers",
                                                        List.of("<gray>Allow visitors to trade."))),
                        Map.entry("FEED_ANIMALS",
                                        new ItemConfig("GOLDEN_CARROT", "<yellow>Feed Animals",
                                                        List.of("<gray>Allow visitors to feed animals."))),
                        Map.entry("LEASH_ENTITIES",
                                        new ItemConfig("LEAD", "<yellow>Leash Entities",
                                                        List.of("<gray>Allow visitors to use leads."))),
                        Map.entry("MODIFY_ARMOR_STANDS",
                                        new ItemConfig("ARMOR_STAND", "<yellow>Modify Armor Stands", List
                                                        .of("<gray>Allow visitors to interact with armor stands."))),
                        Map.entry("MODIFY_ITEM_FRAMES",
                                        new ItemConfig("ITEM_FRAME", "<yellow>Modify Item Frames",
                                                        List.of("<gray>Allow visitors to interact with item frames."))),

                        // Vehicle/Misc Flags
                        Map.entry("RIDE_VEHICLES",
                                        new ItemConfig("MINECART", "<yellow>Ride Vehicles",
                                                        List.of("<gray>Allow visitors to ride minecarts and boats."))),
                        Map.entry("PLACE_VEHICLES",
                                        new ItemConfig("OAK_BOAT", "<yellow>Place Vehicles",
                                                        List.of("<gray>Allow visitors to place vehicles."))),
                        Map.entry("DESTROY_VEHICLES",
                                        new ItemConfig("TNT", "<yellow>Destroy Vehicles",
                                                        List.of("<gray>Allow visitors to break vehicles."))),
                        Map.entry("USE_ENDER_PEARLS",
                                        new ItemConfig("ENDER_PEARL", "<yellow>Use Ender Pearls",
                                                        List.of("<gray>Allow visitors to throw ender pearls."))),
                        Map.entry("USE_CHORUS_FRUIT",
                                        new ItemConfig("CHORUS_FRUIT", "<yellow>Use Chorus Fruit",
                                                        List.of("<gray>Allow visitors to eat chorus fruit."))),
                        Map.entry("PICKUP_ITEMS",
                                        new ItemConfig("HOPPER", "<yellow>Pickup Items",
                                                        List.of("<gray>Allow visitors to pick up dropped items."))),
                        Map.entry("DROP_ITEMS", new ItemConfig("DROPPER", "<yellow>Drop Items",
                                        List.of("<gray>Allow visitors to drop items."))));

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
