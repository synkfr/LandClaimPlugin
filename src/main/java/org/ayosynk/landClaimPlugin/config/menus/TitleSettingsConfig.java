package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("TitleSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class TitleSettingsConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Territory Titles";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom navigation separator")
    public ItemConfig navSpacer = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Enable / disable claim titles globally")
    public ItemConfig titleToggle = new ItemConfig(
            "LEVER",
            "<color:#38BDF8><b>Titles Toggle",
            List.of("<color:#9AA5B4>Enable or disable claim", "<color:#9AA5B4>enter and exit titles."));

    @Comment("Configure entry title behavior")
    public ItemConfig onEntry = new ItemConfig(
            "OAK_SIGN",
            "<color:#34D399><b>Entry Title",
            List.of("<color:#9AA5B4>Text shown when a player", "<color:#9AA5B4>steps into your claim."));

    @Comment("Configure leave title behavior")
    public ItemConfig onLeaveTitle = new ItemConfig(
            "DARK_OAK_SIGN",
            "<color:#60A5FA><b>Exit Title",
            List.of("<color:#9AA5B4>Text shown when a player", "<color:#9AA5B4>leaves your claim."));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to Claim Settings"));

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
