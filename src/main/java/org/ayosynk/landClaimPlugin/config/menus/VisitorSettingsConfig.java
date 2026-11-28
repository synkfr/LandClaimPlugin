package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;
import java.util.Map;

@Header("VisitorSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class VisitorSettingsConfig extends OkaeriConfig {

        public String title = "<color:#1E293B>Visitor Rules";

        @Comment("Outer border frame")
        public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Navigation bar background")
        public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Text appended to lore when a flag is allowed")
        public String enabledStatus = "<color:#34D399>● Allowed";

        @Comment("Text appended to lore when a flag is restricted")
        public String disabledStatus = "<color:#F87171>○ Restricted";

        @Comment("All configurable visitor flags and their display icons")
        public Map<String, ItemConfig> flags = Map.ofEntries(
                        Map.entry("BLOCK_BREAK", new ItemConfig("IRON_PICKAXE", "<color:#60A5FA>Break Blocks", List.of("<color:#9AA5B4>Allow breaking blocks inside the claim."))),
                        Map.entry("BLOCK_PLACE", new ItemConfig("BRICKS", "<color:#60A5FA>Place Blocks", List.of("<color:#9AA5B4>Allow placing blocks inside the claim."))),
                        Map.entry("BLOCK_IGNITE", new ItemConfig("FLINT_AND_STEEL", "<color:#60A5FA>Ignite Blocks", List.of("<color:#9AA5B4>Allow starting fires inside the claim."))),
                        Map.entry("USE_BUCKETS", new ItemConfig("WATER_BUCKET", "<color:#60A5FA>Use Buckets", List.of("<color:#9AA5B4>Allow filling and emptying buckets."))),
                        Map.entry("USE_FERTILIZER", new ItemConfig("BONE_MEAL", "<color:#60A5FA>Use Fertilizer", List.of("<color:#9AA5B4>Allow applying bone meal to crops."))),
                        Map.entry("MODIFY_SIGNS", new ItemConfig("OAK_SIGN", "<color:#60A5FA>Edit Signs", List.of("<color:#9AA5B4>Allow editing or waxing placed signs."))),
                        Map.entry("TRAMPLE_CROPS", new ItemConfig("WHEAT", "<color:#60A5FA>Trample Crops", List.of("<color:#9AA5B4>Allow trampling farmland soil."))),

                        Map.entry("USE_DOORS", new ItemConfig("OAK_DOOR", "<color:#60A5FA>Use Doors", List.of("<color:#9AA5B4>Allow opening and closing doors."))),
                        Map.entry("USE_TRAPDOORS", new ItemConfig("OAK_TRAPDOOR", "<color:#60A5FA>Use Trapdoors", List.of("<color:#9AA5B4>Allow toggling trapdoors."))),
                        Map.entry("USE_FENCE_GATES", new ItemConfig("OAK_FENCE_GATE", "<color:#60A5FA>Use Fence Gates", List.of("<color:#9AA5B4>Allow opening fence gates."))),
                        Map.entry("USE_CONTAINERS", new ItemConfig("CHEST", "<color:#60A5FA>Open Containers", List.of("<color:#9AA5B4>Allow accessing chests, barrels, and hoppers."))),
                        Map.entry("USE_WORKSTATIONS", new ItemConfig("CRAFTING_TABLE", "<color:#60A5FA>Use Workstations", List.of("<color:#9AA5B4>Allow using crafting tables, furnaces, and anvils."))),
                        Map.entry("USE_BEDS", new ItemConfig("RED_BED", "<color:#60A5FA>Use Beds", List.of("<color:#9AA5B4>Allow sleeping in claim beds."))),
                        Map.entry("USE_REDSTONE", new ItemConfig("LEVER", "<color:#60A5FA>Use Redstone", List.of("<color:#9AA5B4>Allow activating buttons, levers, and plates."))),
                        Map.entry("USE_LECTERNS", new ItemConfig("LECTERN", "<color:#60A5FA>Use Lecterns", List.of("<color:#9AA5B4>Allow reading books placed on lecterns."))),
                        Map.entry("USE_BELLS", new ItemConfig("BELL", "<color:#60A5FA>Ring Bells", List.of("<color:#9AA5B4>Allow ringing claim bells."))),

                        Map.entry("DAMAGE_ANIMALS", new ItemConfig("PORKCHOP", "<color:#60A5FA>Damage Animals", List.of("<color:#9AA5B4>Allow harming passive animals."))),
                        Map.entry("DAMAGE_MONSTERS", new ItemConfig("ZOMBIE_HEAD", "<color:#60A5FA>Damage Monsters", List.of("<color:#9AA5B4>Allow fighting hostile mobs."))),
                        Map.entry("BREED_ANIMALS", new ItemConfig("WHEAT", "<color:#60A5FA>Breed Animals", List.of("<color:#9AA5B4>Allow breeding animals inside the claim."))),
                        Map.entry("SHEAR_ENTITIES", new ItemConfig("SHEARS", "<color:#60A5FA>Shear Entities", List.of("<color:#9AA5B4>Allow shearing sheep and mooshrooms."))),
                        Map.entry("TRADE_VILLAGERS", new ItemConfig("EMERALD", "<color:#60A5FA>Trade Villagers", List.of("<color:#9AA5B4>Allow trading with claim villagers."))),
                        Map.entry("FEED_ANIMALS", new ItemConfig("GOLDEN_CARROT", "<color:#60A5B4>Feed Animals", List.of("<color:#9AA5B4>Allow feeding tamed and passive mobs."))),
                        Map.entry("LEASH_ENTITIES", new ItemConfig("LEAD", "<color:#60A5FA>Leash Entities", List.of("<color:#9AA5B4>Allow attaching and detaching leads."))),
                        Map.entry("MODIFY_ARMOR_STANDS", new ItemConfig("ARMOR_STAND", "<color:#60A5FA>Armor Stands", List.of("<color:#9AA5B4>Allow swapping armor stand equipment."))),
                        Map.entry("MODIFY_ITEM_FRAMES", new ItemConfig("ITEM_FRAME", "<color:#60A5FA>Item Frames", List.of("<color:#9AA5B4>Allow editing or rotating item frames."))),

                        Map.entry("RIDE_VEHICLES", new ItemConfig("MINECART", "<color:#60A5FA>Ride Vehicles", List.of("<color:#9AA5B4>Allow entering boats and minecarts."))),
                        Map.entry("MOUNT_ANIMALS", new ItemConfig("SADDLE", "<color:#60A5FA>Mount Animals", List.of("<color:#9AA5B4>Allow riding horses and donkeys."))),
                        Map.entry("PLACE_VEHICLES", new ItemConfig("OAK_BOAT", "<color:#60A5FA>Place Vehicles", List.of("<color:#9AA5B4>Allow placing boats and minecarts."))),
                        Map.entry("DESTROY_VEHICLES", new ItemConfig("TNT", "<color:#60A5FA>Break Vehicles", List.of("<color:#9AA5B4>Allow breaking placed vehicles."))),
                        Map.entry("USE_ENDER_PEARLS", new ItemConfig("ENDER_PEARL", "<color:#60A5FA>Ender Pearls", List.of("<color:#9AA5B4>Allow throwing ender pearls."))),
                        Map.entry("USE_CHORUS_FRUIT", new ItemConfig("CHORUS_FRUIT", "<color:#60A5FA>Chorus Fruit", List.of("<color:#9AA5B4>Allow teleporting with chorus fruit."))),
                        Map.entry("PICKUP_ITEMS", new ItemConfig("HOPPER", "<color:#60A5FA>Pick Up Items", List.of("<color:#9AA5B4>Allow picking up dropped items."))),
                        Map.entry("DROP_ITEMS", new ItemConfig("DROPPER", "<color:#60A5FA>Drop Items", List.of("<color:#9AA5B4>Allow dropping items inside the claim."))),
                        Map.entry("CLAIM_LAND", new ItemConfig("GOLDEN_SHOVEL", "<color:#38BDF8>Claim Land", List.of("<color:#9AA5B4>Allow claiming new chunks for the profile."))),
                        Map.entry("ADMIN_MENU", new ItemConfig("COMMAND_BLOCK", "<color:#FBBF24>Admin Menu Access", List.of("<color:#9AA5B4>Allow members to open /claim menu.", "<color:#9AA5B4>Does not permit abandoning the claim."))),
                        Map.entry("MANAGE_SETTINGS", new ItemConfig("COMPARATOR", "<color:#38BDF8>Manage Settings", List.of("<color:#9AA5B4>Allow changing claim toggles, color, and name."))),
                        Map.entry("MANAGE_MEMBERS", new ItemConfig("PLAYER_HEAD", "<color:#38BDF8>Manage Members", List.of("<color:#9AA5B4>Allow inviting and managing members or trust."))),
                        Map.entry("MANAGE_ROLES", new ItemConfig("WRITABLE_BOOK", "<color:#38BDF8>Manage Roles", List.of("<color:#9AA5B4>Allow creating, editing, and deleting roles.")))
        );

        @Comment("Return to previous menu")
        public ItemConfig back = new ItemConfig(
                        "ARROW",
                        "<color:#38BDF8>← Back",
                        List.of("<color:#9AA5B4>Return to Main Menu"));

        @Comment("Previous Page Button")
        public ItemConfig previousPage = new ItemConfig(
                        "ARROW",
                        "<color:#38BDF8>← Previous Page",
                        List.of("<color:#9AA5B4>View previous items"));

        @Comment("Next Page Button")
        public ItemConfig nextPage = new ItemConfig(
                        "ARROW",
                        "<color:#38BDF8>Next Page →",
                        List.of("<color:#9AA5B4>View next items"));

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
