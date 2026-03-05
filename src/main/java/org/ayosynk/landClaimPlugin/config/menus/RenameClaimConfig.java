package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("RenameClaim GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class RenameClaimConfig extends OkaeriConfig {

    public String title = "Rename Claim";

    @Comment("Visual border separation")
    public ItemConfig outerFrame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Neutral inactive space filler")
    public ItemConfig background = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom alignment spacer")
    public ItemConfig navSpacer = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Opens chat/anvil input for claim name")
    public ItemConfig changeName = new ItemConfig(
            "LECTERN",
            "<yellow><bold>Change Name",
            List.of("<gray>Click to enter a new name for the claim."));

    @Comment("Restores default claim name")
    public ItemConfig resetToDefault = new ItemConfig(
            "FLOW_BANNER_PATTERN",
            "<red><bold>Reset to Default",
            List.of("<gray>Remove the custom name and", "<gray>restore the default name."));

    @Comment("Return to Claim Settings")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Claim Settings"));

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
