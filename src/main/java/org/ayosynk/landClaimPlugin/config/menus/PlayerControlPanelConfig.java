package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("PlayerControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class PlayerControlPanelConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Member · <Player>";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom separator")
    public ItemConfig accent = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Change Role Button")
    public ItemConfig changeRole = new ItemConfig(
            "NAME_TAG",
            "<color:#00E262>Change Role",
            List.of("<color:#A0A0A0>Assign a different claim role", "<color:#A0A0A0>to <color:#FFFFFF><Player><color:#A0A0A0>."));

    @Comment("Transfer Ownership Button")
    public ItemConfig transferOwnership = new ItemConfig(
            "SKULL_BANNER_PATTERN",
            "<color:#E2B700><b>Transfer Ownership",
            List.of("<color:#A0A0A0>Transfer full claim ownership", "<color:#A0A0A0>to <color:#FFFFFF><Player><color:#A0A0A0>."));

    @Comment("Kick Player Button")
    public ItemConfig kickPlayer = new ItemConfig(
            "OAK_DOOR",
            "<color:#E20000>Kick <Player>",
            List.of("<color:#A0A0A0>Remove <color:#FFFFFF><Player> <color:#A0A0A0>from the claim."));

    @Comment("Ban Player Button — blocks entry to the claim entirely.")
    public ItemConfig banPlayer = new ItemConfig(
            "BARRIER",
            "<color:#E20000><b>Ban <Player>",
            List.of("<color:#A0A0A0>Block <color:#FFFFFF><Player> <color:#A0A0A0>from entering", "<color:#A0A0A0>your claim. Reverse via /claim unban."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#00E262>← Back",
            List.of("<color:#A0A0A0>Return to Members"));

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
