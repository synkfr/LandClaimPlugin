package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("AllyManagement GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class AllyManagementConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Alliances";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("No allies indicator")
    public ItemConfig emptyIndicator = new ItemConfig(
            "PAPER",
            "<color:#9AA5B4>No Allies Yet",
            List.of("<color:#64748B>Click Invite Ally below to add one."));

    @Comment("Navigation pane background filler")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Add Ally Button")
    public ItemConfig addAlly = new ItemConfig(
            "EMERALD",
            "<color:#34D399><b>+ Add Ally",
            List.of("<color:#9AA5B4>Send an alliance request to", "<color:#9AA5B4>another player's claim."));

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
            List.of("<color:#9AA5B4>Return to Main Menu"));

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
