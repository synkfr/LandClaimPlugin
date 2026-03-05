package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("AllyControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class AllyControlPanelConfig extends OkaeriConfig {

    public String title = "Ally Control Panel";

    @Comment("Locked UI background")
    public ItemConfig frame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Visual separation")
    public ItemConfig spacer = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Ally Permissions Button")
    public ItemConfig allyPermissions = new ItemConfig(
            "WRITABLE_BOOK",
            "<gold>Ally Permissions",
            List.of("<gray>Manage permissions for this allied claim."));

    @Comment("Ally Warps Button (Coming Soon)")
    public ItemConfig allyWarps = new ItemConfig(
            "RECOVERY_COMPASS",
            "<aqua>Ally Warps <gray>(Coming Soon)",
            List.of("<gray>View and use warps from this ally."));

    @Comment("Remove from Ally Button")
    public ItemConfig removeAlly = new ItemConfig(
            "CREEPER_BANNER_PATTERN",
            "<red><bold>Remove from Ally",
            List.of("<gray>Sever the alliance with this claim."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Ally Management"));

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
