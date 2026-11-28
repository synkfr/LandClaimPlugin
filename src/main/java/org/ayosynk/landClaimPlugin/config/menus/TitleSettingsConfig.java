package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("TitleSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class TitleSettingsConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Territory Titles";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom navigation separator")
    public ItemConfig navSpacer = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Enable / disable claim titles globally")
    public ItemConfig titleToggle = new ItemConfig(
            "LEVER",
            "<color:#00E262><b>Titles Toggle",
            List.of("<color:#A0A0A0>Enable or disable claim", "<color:#A0A0A0>enter and exit titles."));

    @Comment("Configure entry title behavior")
    public ItemConfig onEntry = new ItemConfig(
            "OAK_SIGN",
            "<color:#00E262><b>Entry Title",
            List.of("<color:#A0A0A0>Text shown when a player", "<color:#A0A0A0>steps into your claim."));

    @Comment("Configure leave title behavior")
    public ItemConfig onLeaveTitle = new ItemConfig(
            "DARK_OAK_SIGN",
            "<color:#FFFFFF><b>Exit Title",
            List.of("<color:#A0A0A0>Text shown when a player", "<color:#A0A0A0>leaves your claim."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#00E262>← Back",
            List.of("<color:#A0A0A0>Return to Claim Settings"));

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
