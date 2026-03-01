package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("RoleSetup GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class RoleSetupConfig extends OkaeriConfig {

    public String title = "Role Setup";

    @Comment("Visual spacing frame")
    public ItemConfig frame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation background")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Set Name Button")
    public ItemConfig setName = new ItemConfig(
            "BIRCH_SIGN",
            "<yellow><bold>Set a Name",
            List.of("<gray>Click to enter a name for the role."));

    @Comment("Permissions Button")
    public ItemConfig permissions = new ItemConfig(
            "BELL",
            "<gold><bold>Permissions",
            List.of("<gray>Manage what this role can do.", "<gray>(Opens RoleEditGUI)"));

    @Comment("Set Priority Button")
    public ItemConfig setPriority = new ItemConfig(
            "TOTEM_OF_UNDYING",
            "<light_purple><bold>Set Priority",
            List.of("<gray>Adjust role hierarchy.", "", "<gray>Higher priority overrides lower ones."));

    @Comment("Cancel and Return")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<red>Back/Cancel",
            List.of("<gray>Discard unsaved changes", "<gray>and return to Role Management."));

    @Comment("Save and Exit")
    public ItemConfig saveExit = new ItemConfig(
            "WRITABLE_BOOK",
            "<green><bold>Save and Exit",
            List.of("<gray>Create the role and return."));

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
