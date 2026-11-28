package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("TrustManagement GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class TrustManagementConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Trusted Players";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Empty state indicator")
    public ItemConfig emptyIndicator = new ItemConfig(
            "PAPER",
            "<color:#A0A0A0>No Trusted Players Yet",
            List.of("<color:#A0A0A0>Click Trust Player below to add one."));

    @Comment("Navigation bar background")
    public ItemConfig bottomFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Add Player Button")
    public ItemConfig addPlayer = new ItemConfig(
            "EMERALD",
            "<color:#00E262><b>+ Trust Player",
            List.of("<color:#A0A0A0>Grant custom permission flags", "<color:#A0A0A0>to a non-member player."));

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
