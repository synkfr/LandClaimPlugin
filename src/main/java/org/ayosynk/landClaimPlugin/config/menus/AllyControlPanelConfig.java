package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("AllyControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class AllyControlPanelConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Ally Controls";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation bar background")
    public ItemConfig spacer = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Ally Permissions Button")
    public ItemConfig allyPermissions = new ItemConfig(
            "WRITABLE_BOOK",
            "<color:#38BDF8>Ally Permissions",
            List.of("<color:#9AA5B4>Configure flags shared with", "<color:#9AA5B4>this allied claim."));

    @Comment("Ally Warps Button (Coming Soon)")
    public ItemConfig allyWarps = new ItemConfig(
            "RECOVERY_COMPASS",
            "<color:#60A5FA>Ally Warps <color:#9AA5B4>(Soon)",
            List.of("<color:#9AA5B4>Shared ally warp access is", "<color:#9AA5B4>planned for a future update."));

    @Comment("Remove from Ally Button")
    public ItemConfig removeAlly = new ItemConfig(
            "CREEPER_BANNER_PATTERN",
            "<color:#F87171><b>End Alliance",
            List.of("<color:#9AA5B4>Remove this claim from your", "<color:#9AA5B4>alliance list."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to Ally Management"));

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
