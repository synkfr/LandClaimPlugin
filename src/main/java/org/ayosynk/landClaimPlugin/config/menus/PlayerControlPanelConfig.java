package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("PlayerControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class PlayerControlPanelConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Member <color:#64748B>· <color:#0369A1><Player>";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom separator")
    public ItemConfig accent = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Change Role Button")
    public ItemConfig changeRole = new ItemConfig(
            "NAME_TAG",
            "<color:#38BDF8>Change Role",
            List.of("<color:#9AA5B4>Assign a different claim role", "<color:#9AA5B4>to <color:#F1F5F9><Player><color:#9AA5B4>."));

    @Comment("Transfer Ownership Button")
    public ItemConfig transferOwnership = new ItemConfig(
            "SKULL_BANNER_PATTERN",
            "<color:#FBBF24><b>Transfer Ownership",
            List.of("<color:#9AA5B4>Transfer full claim ownership", "<color:#9AA5B4>to <color:#F1F5F9><Player><color:#9AA5B4>."));

    @Comment("Kick Player Button")
    public ItemConfig kickPlayer = new ItemConfig(
            "OAK_DOOR",
            "<color:#F87171>Kick <Player>",
            List.of("<color:#9AA5B4>Remove <color:#F1F5F9><Player> <color:#9AA5B4>from the claim."));

    @Comment("Ban Player Button — blocks entry to the claim entirely.")
    public ItemConfig banPlayer = new ItemConfig(
            "BARRIER",
            "<color:#F87171><b>Ban <Player>",
            List.of("<color:#9AA5B4>Block <color:#F1F5F9><Player> <color:#9AA5B4>from entering", "<color:#9AA5B4>your claim. Reverse via /claim unban."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to Members"));

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
