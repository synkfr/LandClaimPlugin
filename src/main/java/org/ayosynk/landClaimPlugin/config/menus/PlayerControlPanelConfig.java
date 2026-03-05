package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("PlayerControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class PlayerControlPanelConfig extends OkaeriConfig {

    public String title = "Player Control Panel";

    @Comment("Main UI framing")
    public ItemConfig frame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom separator")
    public ItemConfig accent = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Change Role Button")
    public ItemConfig changeRole = new ItemConfig(
            "SHELTER_POTTERY_SHERD",
            "<yellow>Change Role",
            List.of("<gray>Click to assign a different", "<gray>role to this player."));

    @Comment("Transfer Ownership Button")
    public ItemConfig transferOwnership = new ItemConfig(
            "SKULL_BANNER_PATTERN",
            "<red><bold>Transfer Ownership",
            List.of("<gray>Transfer your claim ownership", "<gray>to this player."));

    @Comment("Kick Player Button")
    public ItemConfig kickPlayer = new ItemConfig(
            "BED",
            "<red>Kick <Player>",
            List.of("<gray>Remove this player from the claim."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Members Management"));

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
