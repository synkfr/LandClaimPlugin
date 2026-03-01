package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("TitleSettings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class TitleSettingsConfig extends OkaeriConfig {

    public String title = "Title Settings";

    @Comment("Main background spacing")
    public ItemConfig frame = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Bottom alignment spacer")
    public ItemConfig navSpacer = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Enable / disable claim titles globally")
    public ItemConfig titleToggle = new ItemConfig(
            "BAMBOO_SIGN",
            "<yellow><bold>Title Toggle",
            List.of("<gray>Toggle on and off"));

    @Comment("Configure entry title behavior")
    public ItemConfig onEntry = new ItemConfig(
            "BELL",
            "<gold><bold>On Entry",
            List.of("<gray>Shows Title on Entering the claim"));

    @Comment("Configure leave title behavior")
    public ItemConfig onLeaveTitle = new ItemConfig(
            "FIELD_MASONED_BANNER_PATTERN",
            "<light_purple><bold>On Leave Title",
            List.of("<gray>Shows Title on leaving the claim"));

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<red>Back",
            List.of("<gray>Return to Claim Settings"));

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
