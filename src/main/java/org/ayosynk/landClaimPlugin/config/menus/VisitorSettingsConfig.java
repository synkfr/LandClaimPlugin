package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;
import java.util.Map;

@Header("VisitorSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class VisitorSettingsConfig extends OkaeriConfig {

        public String title = "<color:#1F1F1F>Visitor Rules";

        @Comment("Outer border frame")
        public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Navigation bar background")
        public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Text appended to lore when a flag is allowed")
        public String enabledStatus = "<color:#00E262>● Allowed";

        @Comment("Text appended to lore when a flag is restricted")
        public String disabledStatus = "<color:#E20000>○ Restricted";

        @Comment("All configurable visitor flags and their display icons")
        public Map<String, ItemConfig> flags = Map.ofEntries(
                        Map.entry("BLOCK_BREAK", new ItemConfig("IRON_PICKAXE", "<color:#FFFFFF>Break Blocks", List.of("<color:#A0A0A0>Allow breaking blocks inside the claim."))),
                        Map.entry("BLOCK_PLACE", new ItemConfig("BRICKS", "<color:#FFFFFF>Place Blocks", List.of("<color:#A0A0A0>Allow placing blocks inside the claim."))),
                        Map.entry("BLOCK_IGNITE", new ItemConfig("FLINT_AND_STEEL", "<color:#FFFFFF>Ignite Blocks", List.of("<color:#A0A0A0>Allow starting fires inside the claim."))),
                        Map.entry("USE_BUCKETS", new ItemConfig("WATER_BUCKET", "<color:#FFFFFF>Use Buckets", List.of("<color:#A0A0A0>Allow filling and emptying buckets."))),
                        Map.entry("USE_FERTILIZER", new ItemConfig("BONE_MEAL", "<color:#FFFFFF>Use Fertilizer", List.of("<color:#A0A0A0>Allow applying bone meal to crops."))),
                        Map.entry("MODIFY_SIGNS", new ItemConfig("OAK_SIGN", "<color:#FFFFFF>Edit Signs", List.of("<color:#A0A0A0>Allow editing or waxing placed signs."))),
                        Map.entry("TRAMPLE_CROPS", new ItemConfig("WHEAT", "<color:#FFFFFF>Trample Crops", List.of("<color:#A0A0A0>Allow trampling farmland soil."))),

                        Map.entry("USE_DOORS", new ItemConfig("OAK_DOOR", "<color:#FFFFFF>Use Doors", List.of("<color:#A0A0A0>Allow opening and closing doors."))),
                        Map.entry("USE_TRAPDOORS", new ItemConfig("OAK_TRAPDOOR", "<color:#FFFFFF>Use Trapdoors", List.of("<color:#A0A0A0>Allow toggling trapdoors."))),
                        Map.entry("USE_FENCE_GATES", new ItemConfig("OAK_FENCE_GATE", "<color:#FFFFFF>Use Fence Gates", List.of("<color:#A0A0A0>Allow opening fence gates."))),
                        Map.entry("USE_CONTAINERS", new ItemConfig("CHEST", "<color:#FFFFFF>Open Containers", List.of("<color:#A0A0A0>Allow accessing chests, barrels, and hoppers."))),
                        Map.entry("USE_WORKSTATIONS", new ItemConfig("CRAFTING_TABLE", "<color:#FFFFFF>Use Workstations", List.of("<color:#A0A0A0>Allow using crafting tables, furnaces, and anvils."))),
                        Map.entry("USE_BEDS", new ItemConfig("RED_BED", "<color:#FFFFFF>Use Beds", List.of("<color:#A0A0A0>Allow sleeping in claim beds."))),
                        Map.entry("USE_REDSTONE", new ItemConfig("LEVER", "<color:#FFFFFF>Use Redstone", List.of("<color:#A0A0A0>Allow activating buttons, levers, and plates."))),
                        Map.entry("USE_LECTERNS", new ItemConfig("LECTERN", "<color:#FFFFFF>Use Lecterns", List.of("<color:#A0A0A0>Allow reading books placed on lecterns."))),
                        Map.entry("USE_BELLS", new ItemConfig("BELL", "<color:#FFFFFF>Ring Bells", List.of("<color:#A0A0A0>Allow ringing claim bells."))),

                        Map.entry("DAMAGE_ANIMALS", new ItemConfig("PORKCHOP", "<color:#FFFFFF>Damage Animals", List.of("<color:#A0A0A0>Allow harming passive animals."))),
                        Map.entry("DAMAGE_MONSTERS", new ItemConfig("ZOMBIE_HEAD", "<color:#FFFFFF>Damage Monsters", List.of("<color:#A0A0A0>Allow fighting hostile mobs."))),
                        Map.entry("BREED_ANIMALS", new ItemConfig("WHEAT", "<color:#FFFFFF>Breed Animals", List.of("<color:#A0A0A0>Allow breeding animals inside the claim."))),
                        Map.entry("SHEAR_ENTITIES", new ItemConfig("SHEARS", "<color:#FFFFFF>Shear Entities", List.of("<color:#A0A0A0>Allow shearing sheep and mooshrooms."))),
                        Map.entry("TRADE_VILLAGERS", new ItemConfig("EMERALD", "<color:#FFFFFF>Trade Villagers", List.of("<color:#A0A0A0>Allow trading with claim villagers."))),
                        Map.entry("FEED_ANIMALS", new ItemConfig("GOLDEN_CARROT", "<color:#60A5B4>Feed Animals", List.of("<color:#A0A0A0>Allow feeding tamed and passive mobs."))),
                        Map.entry("LEASH_ENTITIES", new ItemConfig("LEAD", "<color:#FFFFFF>Leash Entities", List.of("<color:#A0A0A0>Allow attaching and detaching leads."))),
                        Map.entry("MODIFY_ARMOR_STANDS", new ItemConfig("ARMOR_STAND", "<color:#FFFFFF>Armor Stands", List.of("<color:#A0A0A0>Allow swapping armor stand equipment."))),
                        Map.entry("MODIFY_ITEM_FRAMES", new ItemConfig("ITEM_FRAME", "<color:#FFFFFF>Item Frames", List.of("<color:#A0A0A0>Allow editing or rotating item frames."))),

                        Map.entry("RIDE_VEHICLES", new ItemConfig("MINECART", "<color:#FFFFFF>Ride Vehicles", List.of("<color:#A0A0A0>Allow entering boats and minecarts."))),
                        Map.entry("MOUNT_ANIMALS", new ItemConfig("SADDLE", "<color:#FFFFFF>Mount Animals", List.of("<color:#A0A0A0>Allow riding horses and donkeys."))),
                        Map.entry("PLACE_VEHICLES", new ItemConfig("OAK_BOAT", "<color:#FFFFFF>Place Vehicles", List.of("<color:#A0A0A0>Allow placing boats and minecarts."))),
                        Map.entry("DESTROY_VEHICLES", new ItemConfig("TNT", "<color:#FFFFFF>Break Vehicles", List.of("<color:#A0A0A0>Allow breaking placed vehicles."))),
                        Map.entry("USE_ENDER_PEARLS", new ItemConfig("ENDER_PEARL", "<color:#FFFFFF>Ender Pearls", List.of("<color:#A0A0A0>Allow throwing ender pearls."))),
                        Map.entry("USE_CHORUS_FRUIT", new ItemConfig("CHORUS_FRUIT", "<color:#FFFFFF>Chorus Fruit", List.of("<color:#A0A0A0>Allow teleporting with chorus fruit."))),
                        Map.entry("PICKUP_ITEMS", new ItemConfig("HOPPER", "<color:#FFFFFF>Pick Up Items", List.of("<color:#A0A0A0>Allow picking up dropped items."))),
                        Map.entry("DROP_ITEMS", new ItemConfig("DROPPER", "<color:#FFFFFF>Drop Items", List.of("<color:#A0A0A0>Allow dropping items inside the claim."))),
                        Map.entry("CLAIM_LAND", new ItemConfig("GOLDEN_SHOVEL", "<color:#00E262>Claim Land", List.of("<color:#A0A0A0>Allow claiming new chunks for the profile."))),
                        Map.entry("ADMIN_MENU", new ItemConfig("COMMAND_BLOCK", "<color:#E2B700>Admin Menu Access", List.of("<color:#A0A0A0>Allow members to open /claim menu.", "<color:#A0A0A0>Does not permit abandoning the claim."))),
                        Map.entry("MANAGE_SETTINGS", new ItemConfig("COMPARATOR", "<color:#00E262>Manage Settings", List.of("<color:#A0A0A0>Allow changing claim toggles, color, and name."))),
                        Map.entry("MANAGE_MEMBERS", new ItemConfig("PLAYER_HEAD", "<color:#00E262>Manage Members", List.of("<color:#A0A0A0>Allow inviting and managing members or trust."))),
                        Map.entry("MANAGE_ROLES", new ItemConfig("WRITABLE_BOOK", "<color:#00E262>Manage Roles", List.of("<color:#A0A0A0>Allow creating, editing, and deleting roles.")))
        );

        @Comment("Return to previous menu")
        public ItemConfig back = new ItemConfig(
                        "ARROW",
                        "<color:#00E262>← Back",
                        List.of("<color:#A0A0A0>Return to Main Menu"));

        @Comment("Previous Page Button")
        public ItemConfig previousPage = new ItemConfig(
                        "ARROW",
                        "<color:#00E262>← Previous Page",
                        List.of("<color:#A0A0A0>View previous items"));

        @Comment("Next Page Button")
        public ItemConfig nextPage = new ItemConfig(
                        "ARROW",
                        "<color:#00E262>Next Page →",
                        List.of("<color:#A0A0A0>View next items"));

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
