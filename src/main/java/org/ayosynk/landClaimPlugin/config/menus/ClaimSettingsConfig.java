package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("Claim Settings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting")
public class ClaimSettingsConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Claim Settings · {claim_name}";

    @Comment("Outer border frame")
    public ItemConfig filler1 = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Inner background glass")
    public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Overview Button")
    public ItemConfig overview = new ItemConfig(
            "NETHER_STAR",
            "<color:#00E262>Territory Overview",
            List.of(
                    "<color:#A0A0A0>Owner <color:#FFFFFF>{owner}",
                    "<color:#A0A0A0>Chunks <color:#FFFFFF>{size}",
                    "<color:#A0A0A0>Members <color:#FFFFFF>{members}"));

    @Comment("Rename Button")
    public ItemConfig rename = new ItemConfig(
            "NAME_TAG",
            "<color:#FFFFFF>Rename Claim",
            List.of(
                    "<color:#A0A0A0>Edit claim name or owner alias."));

    @Comment("Color Button")
    public ItemConfig color = new ItemConfig(
            "BRUSH",
            "<color:#00E262>Map Color",
            List.of(
                    "<color:#A0A0A0>Choose display color for web maps."));

    @Comment("Roles Button")
    public ItemConfig roles = new ItemConfig(
            "WRITABLE_BOOK",
            "<color:#E2B700>Claim Roles",
            List.of(
                    "<color:#A0A0A0>Create roles and assign flags."));

    @Comment("PvP Toggle Button")
    public ItemConfig pvpToggle = new ItemConfig(
            "IRON_SWORD",
            "<color:#E20000>PvP Protection",
            List.of(
                    "<color:#A0A0A0>Toggle combat inside this claim.",
                    "<color:#A0A0A0>Status <pvp_status>"));

    @Comment("Visibility Button")
    public ItemConfig visibility = new ItemConfig(
            "ECHO_SHARD",
            "<color:#FFFFFF>Map Visibility",
            List.of(
                    "<color:#A0A0A0>Toggle visibility on web maps."));

    @Comment("Title Toggle Button")
    public ItemConfig titleToggle = new ItemConfig(
            "GOAT_HORN",
            "<color:#00E262>Territory Titles",
            List.of(
                    "<color:#A0A0A0>Customize entry and exit titles."));

    @Comment("TNT Explosions Toggle Button")
    public ItemConfig tntToggle = new ItemConfig(
            "TNT",
            "<color:#E2B700>TNT Explosions",
            List.of(
                    "<color:#A0A0A0>Allow or block TNT blasts inside",
                    "<color:#A0A0A0>this claim. Status <tnt_status>"));

    @Comment("Mob Spawning Toggle Button")
    public ItemConfig mobSpawningToggle = new ItemConfig(
            "SPAWNER",
            "<color:#00E262>Mob Spawning",
            List.of(
                    "<color:#A0A0A0>Allow or block natural mob spawns",
                    "<color:#A0A0A0>inside this claim. Status <mob_spawning_status>"));

    @Comment("Abandon All Button")
    public ItemConfig abandonAll = new ItemConfig(
            "FLOWER_BANNER_PATTERN",
            "<color:#E20000><b>Abandon All Claims",
            List.of(
                    "<color:#A0A0A0>Release every chunk in this profile.",
                    "<color:#E20000>Permanent action."));

    @Comment("Back Button")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#00E262>← Back",
            List.of(
                    "<color:#A0A0A0>Return to Main Menu"));

    public static class ItemConfig extends OkaeriConfig {
        public String material;
        public String name;
        public List<String> lore;

        public ItemConfig() {
        }

        public ItemConfig(String material, String name, List<String> lore) {
            this.material = material;
            this.name = name;
            this.lore = lore;
        }
    }
}
