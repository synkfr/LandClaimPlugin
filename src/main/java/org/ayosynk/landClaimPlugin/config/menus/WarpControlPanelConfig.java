package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("WarpControlPanel GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class WarpControlPanelConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Warp · {name}";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Inner background glass")
    public ItemConfig spacer = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Change Location Button")
    public ItemConfig changeLocation = new ItemConfig(
            "LIME_BED",
            "<color:#00E262><b>Update Location",
            List.of("<color:#A0A0A0>Move this warp to your", "<color:#A0A0A0>current standing position."));

    @Comment("Change Icon Button")
    public ItemConfig changeIcon = new ItemConfig(
            "ITEM_FRAME",
            "<color:#00E262><b>Change Icon",
            List.of("<color:#A0A0A0>Pick a new display icon", "<color:#A0A0A0>for this claim warp."));

    @Comment("Delete Warp Button")
    public ItemConfig deleteWarp = new ItemConfig(
            "BARRIER",
            "<color:#E20000><b>Delete Warp",
            List.of("<color:#A0A0A0>Permanently remove this warp."));

    @Comment("Privacy toggle — shown when the warp is private. Click to make it public.")
    public ItemConfig makePublic = new ItemConfig(
            "LIME_DYE",
            "<color:#00E262><b>Make Public",
            List.of("<color:#A0A0A0>Publish to /claim publicwarps", "<color:#A0A0A0>so anyone on the server", "<color:#A0A0A0>can teleport here."));

    @Comment("Privacy toggle — shown when the warp is public. Click to make it private.")
    public ItemConfig makePrivate = new ItemConfig(
            "GRAY_DYE",
            "<color:#E2B700><b>Make Private",
            List.of("<color:#A0A0A0>Remove from /claim publicwarps.", "<color:#A0A0A0>Only claim members can use it."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#00E262>← Back",
            List.of("<color:#A0A0A0>Return to Warps"));

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
