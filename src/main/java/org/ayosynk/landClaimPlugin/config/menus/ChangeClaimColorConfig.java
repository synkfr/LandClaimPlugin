package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("ChangeClaimColor GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class ChangeClaimColorConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Claim Color";

    @Comment("Outer border frame")
    public ItemConfig frameFill = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom navigation separator")
    public ItemConfig navFrame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Opens HEX color input")
    public ItemConfig customColor = new ItemConfig(
            "BRUSH",
            "<color:#38BDF8>Custom Hex Color",
            List.of("<color:#9AA5B4>Enter a custom hex code", "<color:#9AA5B4>such as <color:#F1F5F9>#38BDF8<color:#9AA5B4>."));

    @Comment("Return to Claim Settings")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to Claim Settings"));

    @Comment("Color Selection Options")
    public ItemConfig colorBlack = new ItemConfig("BLACK_WOOL", "<black><bold>Black",
            List.of("<gray>Set claim color to Black."));
    public ItemConfig colorBlue = new ItemConfig("BLUE_WOOL", "<blue><bold>Blue",
            List.of("<gray>Set claim color to Blue."));
    public ItemConfig colorBrown = new ItemConfig("BROWN_WOOL", "<color:#8B4513><bold>Brown",
            List.of("<gray>Set claim color to Brown."));
    public ItemConfig colorCyan = new ItemConfig("CYAN_WOOL", "<dark_aqua><bold>Cyan",
            List.of("<gray>Set claim color to Cyan."));
    public ItemConfig colorGray = new ItemConfig("GRAY_WOOL", "<dark_gray><bold>Gray",
            List.of("<gray>Set claim color to Gray."));
    public ItemConfig colorGreen = new ItemConfig("GREEN_WOOL", "<dark_green><bold>Green",
            List.of("<gray>Set claim color to Green."));
    public ItemConfig colorLightBlue = new ItemConfig("LIGHT_BLUE_WOOL", "<aqua><bold>Light Blue",
            List.of("<gray>Set claim color to Light Blue."));
    public ItemConfig colorLime = new ItemConfig("LIME_WOOL", "<green><bold>Lime",
            List.of("<gray>Set claim color to Lime."));
    public ItemConfig colorLightGray = new ItemConfig("LIGHT_GRAY_WOOL", "<gray><bold>Light Gray",
            List.of("<gray>Set claim color to Light Gray."));
    public ItemConfig colorMagenta = new ItemConfig("MAGENTA_WOOL", "<color:#FF00FF><bold>Magenta",
            List.of("<gray>Set claim color to Magenta."));
    public ItemConfig colorOrange = new ItemConfig("ORANGE_WOOL", "<gold><bold>Orange",
            List.of("<gray>Set claim color to Orange."));
    public ItemConfig colorPink = new ItemConfig("PINK_WOOL", "<light_purple><bold>Pink",
            List.of("<gray>Set claim color to Pink."));
    public ItemConfig colorPurple = new ItemConfig("PURPLE_WOOL", "<dark_purple><bold>Purple",
            List.of("<gray>Set claim color to Purple."));
    public ItemConfig colorRed = new ItemConfig("RED_WOOL", "<red><bold>Red", List.of("<gray>Set claim color to Red."));
    public ItemConfig colorWhite = new ItemConfig("WHITE_WOOL", "<white><bold>White",
            List.of("<gray>Set claim color to White."));
    public ItemConfig colorYellow = new ItemConfig("YELLOW_WOOL", "<yellow><bold>Yellow",
            List.of("<gray>Set claim color to Yellow."));

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
