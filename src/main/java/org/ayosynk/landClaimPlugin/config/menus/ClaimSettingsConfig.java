package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("Claim Settings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting")
public class ClaimSettingsConfig extends OkaeriConfig {

    public String title = "Claim Settings: {claim_name}";

    @Comment("Filler 1 (Primary filler)")
    public ItemConfig filler1 = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Filler 2 (Secondary filler)")
    public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Overview Button")
    public ItemConfig overview = new ItemConfig(
            "NETHER_STAR",
            "<yellow>Claim Settings Overview",
            List.of(
                    "<gray>View general claim settings."));

    @Comment("Rename Button")
    public ItemConfig rename = new ItemConfig(
            "NAME_TAG",
            "<aqua>Change Claim Name",
            List.of(
                    "<gray>Click to rename this claim."));

    @Comment("Color Button")
    public ItemConfig color = new ItemConfig(
            "BRUSH",
            "<light_purple>Change Claim Color",
            List.of(
                    "<gray>Change the display color for map integrations."));

    @Comment("Roles Button")
    public ItemConfig roles = new ItemConfig(
            "WRITABLE_BOOK",
            "<gold>Manage Roles",
            List.of(
                    "<gray>Manage custom roles and permissions within this claim."));

    @Comment("Warps Button")
    public ItemConfig warps = new ItemConfig(
            "ENDER_EYE",
            "<green>Warp Settings",
            List.of(
                    "<gray>Manage warps for this claim."));

    @Comment("Visibility Button")
    public ItemConfig visibility = new ItemConfig(
            "ECHO_SHARD",
            "<blue>Claim Visibility",
            List.of(
                    "<gray>Toggle claim visibility on web maps."));

    @Comment("Title Toggle Button")
    public ItemConfig titleToggle = new ItemConfig(
            "GOAT_HORN",
            "<yellow>Title Change on Enter/Leave",
            List.of(
                    "<gray>Toggle showing a title when players enter/leave."));

    @Comment("Abandon All Button")
    public ItemConfig abandonAll = new ItemConfig(
            "FLOWER_BANNER_PATTERN",
            "<red>Abandon All Claims",
            List.of(
                    "<gray>Abandon all your claims.",
                    "<dark_red><bold>WARNING: Irreversible!"));

    @Comment("Back Button")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<red>Back to MainMenu",
            List.of(
                    "<gray>Return to the previous menu."));

    public static class ItemConfig extends OkaeriConfig {
        public String material;
        public String name;
        public List<String> lore;

        public ItemConfig() {
        }

        public ItemConfig(String material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }
    }
}
