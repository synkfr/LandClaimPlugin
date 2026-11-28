package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("RenameClaim GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class RenameClaimConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Rename Claim";

    @Comment("Outer border frame")
    public ItemConfig outerFrame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Inner background glass")
    public ItemConfig background = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom navigation separator")
    public ItemConfig navSpacer = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Opens chat/anvil input for claim name")
    public ItemConfig changeName = new ItemConfig(
            "LECTERN",
            "<color:#38BDF8><b>Rename Claim",
            List.of("<color:#9AA5B4>Enter a custom display name", "<color:#9AA5B4>for this claim."));

    @Comment("Restores default claim name")
    public ItemConfig resetToDefault = new ItemConfig(
            "FLOW_BANNER_PATTERN",
            "<color:#F87171><b>Reset Name",
            List.of("<color:#9AA5B4>Remove custom claim name and", "<color:#9AA5B4>restore the default label."));

    @Comment("Change the owner alias")
    public ItemConfig ownerAlias = new ItemConfig(
            "NAME_TAG",
            "<color:#60A5FA><b>Owner Alias",
            List.of("<color:#9AA5B4>Set the owner name displayed in", "<color:#9AA5B4>menus and titles (or 'reset')."));

    @Comment("Return to Claim Settings")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to Claim Settings"));

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
