package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("Role Edit Permissions GUI Configuration")
public class RoleEditConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Role Flags · <role>";

    @Comment("Outer border frame")
    public VisitorSettingsConfig.ItemConfig frame = new VisitorSettingsConfig.ItemConfig("BLACK_STAINED_GLASS_PANE", " ",
            List.of());

    @Comment("Info item at the top middle")
    public VisitorSettingsConfig.ItemConfig info = new VisitorSettingsConfig.ItemConfig(
            "PAPER",
            "<color:#00E262>Role Permissions",
            List.of(
                    "<color:#A0A0A0>Toggle flags granted to",
                    "<color:#A0A0A0>members assigned this role."));

    @Comment("Navigation bar background")
    public VisitorSettingsConfig.ItemConfig bottomFill = new VisitorSettingsConfig.ItemConfig("GRAY_STAINED_GLASS_PANE",
            " ", List.of());

    @Comment("Return to previous menu")
    public VisitorSettingsConfig.ItemConfig back = new VisitorSettingsConfig.ItemConfig(
            "ARROW",
            "<color:#00E262>← Back",
            List.of("<color:#A0A0A0>Return to Role Setup"));

    @Comment("Previous Page Button")
    public VisitorSettingsConfig.ItemConfig previousPage = new VisitorSettingsConfig.ItemConfig(
            "ARROW",
            "<color:#00E262>← Previous Page",
            List.of("<color:#A0A0A0>View previous items"));

    @Comment("Next Page Button")
    public VisitorSettingsConfig.ItemConfig nextPage = new VisitorSettingsConfig.ItemConfig(
            "ARROW",
            "<color:#00E262>Next Page →",
            List.of("<color:#A0A0A0>View next items"));

    @Comment("Lore for Enabled/Disabled states")
    public String enabledStatus = "<color:#00E262>● Enabled";
    public String disabledStatus = "<color:#E20000>○ Disabled";
}
