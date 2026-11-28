package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("MainMenu GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class MainMenuConfig extends OkaeriConfig {

        public String title = "<color:#1F1F1F>Claim Menu · {claim_name}";

        @Comment("Outer border frame")
        public ItemConfig filler1 = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Inner divider frame")
        public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Claim Map Button")
        public ItemConfig claimMap = new ItemConfig(
                        "FILLED_MAP",
                        "<color:#00E262>Territory Map",
                        List.of("<color:#A0A0A0>Inspect claimed chunks around", "<color:#A0A0A0>{claim_name} <color:#A0A0A0>({size} chunks)<color:#A0A0A0>."));

        @Comment("Claim Warps Button")
        public ItemConfig warps = new ItemConfig(
                        "COMPASS",
                        "<color:#FFFFFF>Claim Warps",
                        List.of("<color:#A0A0A0>Teleport to or configure warps", "<color:#A0A0A0>inside this territory."));

        @Comment("Allies Button")
        public ItemConfig allies = new ItemConfig(
                        "ENDER_CHEST",
                        "<color:#00E262>Alliances",
                        List.of("<color:#A0A0A0>Manage allied claims and shared", "<color:#A0A0A0>alliance permissions."));

        @Comment("Claim Settings Button")
        public ItemConfig settings = new ItemConfig(
                        "EMERALD",
                        "<color:#00E262>Claim Settings",
                        List.of("<color:#A0A0A0>Configure PvP, TNT, mob spawning,", "<color:#A0A0A0>roles, rename, and map color."));

        @Comment("Trusted Members Management Button")
        public ItemConfig trusted = new ItemConfig(
                        "GOLDEN_CHESTPLATE",
                        "<color:#FFFFFF>Trusted Players",
                        List.of("<color:#A0A0A0>Grant temporary permissions without", "<color:#A0A0A0>full claim membership."));

        @Comment("Members Management Button")
        public ItemConfig members = new ItemConfig(
                        "PLAYER_HEAD",
                        "<color:#00E262>Claim Members",
                        List.of("<color:#A0A0A0>Invite players, assign roles,", "<color:#A0A0A0>and manage <color:#FFFFFF>{members} <color:#A0A0A0>member(s)."));

        @Comment("Visitor Settings Button")
        public ItemConfig visitors = new ItemConfig(
                        "OAK_SIGN",
                        "<color:#E2B700>Visitor Rules",
                        List.of("<color:#A0A0A0>Control what non-members can", "<color:#A0A0A0>interact with inside your claim."));

        @Comment("Close Menu Button")
        public ItemConfig close = new ItemConfig(
                        "BARRIER",
                        "<color:#E20000>Close",
                        List.of("<color:#A0A0A0>Close this menu"));

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
