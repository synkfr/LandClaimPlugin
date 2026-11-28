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

        public String title = "<color:#1E293B>Ally Permissions <color:#64748B>· <color:#0369A1><name>";

        @Comment("Outer border frame")
        public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Navigation background")
        public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Information Button")
        public ItemConfig info = new ItemConfig(
                        "NETHER_STAR",
                        "<color:#38BDF8>Ally Permissions",
                        List.of("<color:#9AA5B4>Toggle flags granted to", "<color:#9AA5B4>this allied claim."));

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

        @Comment("Return to previous menu")
        public ItemConfig back = new ItemConfig(
                        "ARROW",
                        "<color:#38BDF8>← Back",
                        List.of("<color:#9AA5B4>Return to Ally Control Panel"));

        @Comment("Text added to lore when a flag is enabled for this ally")
        public String enabledStatus = "<color:#34D399>● Enabled";

        @Comment("Text added to lore when a flag is disabled for this ally")
        public String disabledStatus = "<color:#F87171>○ Disabled";

        @Comment("List of configurable ally flags.")
        public Map<String, ItemConfig> flags = new LinkedHashMap<>() {
                {
                        put("use", new ItemConfig("OAK_DOOR", "<color:#60A5FA>Use Blocks",
                                        List.of("<color:#9AA5B4>Allow ally to use doors and switches.")));
                        put("interact", new ItemConfig("ARMOR_STAND", "<color:#60A5FA>Interact",
                                        List.of("<color:#9AA5B4>Allow ally to interact with entities.")));
                        put("chest-access", new ItemConfig("CHEST", "<color:#60A5FA>Chest Access",
                                        List.of("<color:#9AA5B4>Allow ally to open containers.")));
                        put("ride", new ItemConfig("SADDLE", "<color:#60A5FA>Ride Vehicles",
                                        List.of("<color:#9AA5B4>Allow ally to ride mounts and vehicles.")));
                        put("vehicle-place", new ItemConfig("OAK_BOAT", "<color:#60A5FA>Place Vehicles",
                                        List.of("<color:#9AA5B4>Allow ally to place boats or minecarts.")));
                        put("vehicle-destroy", new ItemConfig("GOLDEN_SWORD", "<color:#60A5FA>Destroy Vehicles",
                                        List.of("<color:#9AA5B4>Allow ally to break boats or minecarts.")));
                        put("drop-items", new ItemConfig("DROPPER", "<color:#60A5FA>Drop Items",
                                        List.of("<color:#9AA5B4>Allow ally to drop items.")));
                        put("pickup-items", new ItemConfig("HOPPER", "<color:#60A5FA>Pickup Items",
                                        List.of("<color:#9AA5B4>Allow ally to pick up dropped items.")));
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
