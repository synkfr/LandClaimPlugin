package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("RoleSetup GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class RoleSetupConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Role Setup";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation background")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Set Name Button")
    public ItemConfig setName = new ItemConfig(
            "NAME_TAG",
            "<color:#38BDF8><b>Set Name",
            List.of("<color:#9AA5B4>Set the display name", "<color:#9AA5B4>for this custom role."));

    @Comment("Permissions Button")
    public ItemConfig permissions = new ItemConfig(
            "WRITABLE_BOOK",
            "<color:#60A5FA><b>Permissions",
            List.of("<color:#9AA5B4>Edit permission flags", "<color:#9AA5B4>granted to this role."));

    @Comment("Set Priority Button")
    public ItemConfig setPriority = new ItemConfig(
            "COMPARATOR",
            "<color:#FBBF24><b>Set Priority",
            List.of("<color:#9AA5B4>Higher priority overrides lower", "<color:#9AA5B4>roles inside the hierarchy."));

    @Comment("Cancel and Return")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#F87171>← Cancel",
            List.of("<color:#9AA5B4>Discard unsaved changes", "<color:#9AA5B4>and return to Roles."));

    @Comment("Save and Exit")
    public ItemConfig saveExit = new ItemConfig(
            "LIME_DYE",
            "<color:#34D399><b>Save Role",
            List.of("<color:#9AA5B4>Save changes and return", "<color:#9AA5B4>to Role Management."));

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
