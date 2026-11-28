package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("Claim Settings GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting")
public class ClaimSettingsConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Claim Settings <color:#64748B>· <color:#0369A1>{claim_name}";

    @Comment("Outer border frame")
    public ItemConfig filler1 = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Inner background glass")
    public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Overview Button")
    public ItemConfig overview = new ItemConfig(
            "NETHER_STAR",
            "<color:#38BDF8>Territory Overview",
            List.of(
                    "<color:#9AA5B4>Owner <color:#F1F5F9>{owner}",
                    "<color:#9AA5B4>Chunks <color:#F1F5F9>{size}",
                    "<color:#9AA5B4>Members <color:#F1F5F9>{members}"));

    @Comment("Rename Button")
    public ItemConfig rename = new ItemConfig(
            "NAME_TAG",
            "<color:#60A5FA>Rename Claim",
            List.of(
                    "<color:#9AA5B4>Edit claim name or owner alias."));

    @Comment("Color Button")
    public ItemConfig color = new ItemConfig(
            "BRUSH",
            "<color:#38BDF8>Map Color",
            List.of(
                    "<color:#9AA5B4>Choose display color for web maps."));

    @Comment("Roles Button")
    public ItemConfig roles = new ItemConfig(
            "WRITABLE_BOOK",
            "<color:#FBBF24>Claim Roles",
            List.of(
                    "<color:#9AA5B4>Create roles and assign flags."));

    @Comment("PvP Toggle Button")
    public ItemConfig pvpToggle = new ItemConfig(
            "IRON_SWORD",
            "<color:#F87171>PvP Protection",
            List.of(
                    "<color:#9AA5B4>Toggle combat inside this claim.",
                    "<color:#9AA5B4>Status <pvp_status>"));

    @Comment("Visibility Button")
    public ItemConfig visibility = new ItemConfig(
            "ECHO_SHARD",
            "<color:#60A5FA>Map Visibility",
            List.of(
                    "<color:#9AA5B4>Toggle visibility on web maps."));

    @Comment("Title Toggle Button")
    public ItemConfig titleToggle = new ItemConfig(
            "GOAT_HORN",
            "<color:#38BDF8>Territory Titles",
            List.of(
                    "<color:#9AA5B4>Customize entry and exit titles."));

    @Comment("TNT Explosions Toggle Button")
    public ItemConfig tntToggle = new ItemConfig(
            "TNT",
            "<color:#FBBF24>TNT Explosions",
            List.of(
                    "<color:#9AA5B4>Allow or block TNT blasts inside",
                    "<color:#9AA5B4>this claim. Status <tnt_status>"));

    @Comment("Mob Spawning Toggle Button")
    public ItemConfig mobSpawningToggle = new ItemConfig(
            "SPAWNER",
            "<color:#34D399>Mob Spawning",
            List.of(
                    "<color:#9AA5B4>Allow or block natural mob spawns",
                    "<color:#9AA5B4>inside this claim. Status <mob_spawning_status>"));

    @Comment("Abandon All Button")
    public ItemConfig abandonAll = new ItemConfig(
            "FLOWER_BANNER_PATTERN",
            "<color:#F87171><b>Abandon All Claims",
            List.of(
                    "<color:#9AA5B4>Release every chunk in this profile.",
                    "<color:#F87171>Permanent action."));

    @Comment("Back Button")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of(
                    "<color:#9AA5B4>Return to Main Menu"));

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
