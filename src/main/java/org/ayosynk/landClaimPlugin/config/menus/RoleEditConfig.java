package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("Role Edit Permissions GUI Configuration")
public class RoleEditConfig extends OkaeriConfig {

    public String title = "Role Permissions - <role>";

    @Comment("Frame item on the top and sides")
    public VisitorSettingsConfig.ItemConfig frame = new VisitorSettingsConfig.ItemConfig("GRAY_STAINED_GLASS_PANE", " ",
            List.of());

    @Comment("Info item at the top middle")
    public VisitorSettingsConfig.ItemConfig info = new VisitorSettingsConfig.ItemConfig(
            "PAPER",
            "<yellow>Role Permissions",
            List.of(
                    "<gray>Toggle permissions for this role.",
                    "<gray>Click an item to change its setting."));

    @Comment("Nav frame item at the bottom")
    public VisitorSettingsConfig.ItemConfig bottomFill = new VisitorSettingsConfig.ItemConfig("GRAY_STAINED_GLASS_PANE",
            " ", List.of());

    @Comment("Return to previous menu")
    public VisitorSettingsConfig.ItemConfig back = new VisitorSettingsConfig.ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Role Setup"));

    @Comment("Previous Page Button")
    public VisitorSettingsConfig.ItemConfig previousPage = new VisitorSettingsConfig.ItemConfig(
            "LIGHT_BLUE_CANDLE",
            "<aqua>Previous Page",
            List.of("<gray>Go to the previous page."));

    @Comment("Next Page Button")
    public VisitorSettingsConfig.ItemConfig nextPage = new VisitorSettingsConfig.ItemConfig(
            "LIME_CANDLE",
            "<green>Next Page",
            List.of("<gray>Go to the next page."));

    @Comment("Lore for Enabled/Disabled states")
    public String enabledStatus = "<green>Currently: <bold>Enabled";
    public String disabledStatus = "<red>Currently: <bold>Disabled";
}
