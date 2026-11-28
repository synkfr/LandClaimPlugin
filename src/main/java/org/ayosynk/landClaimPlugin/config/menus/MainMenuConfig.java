package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("MainMenu GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting (e.g., <red>Test</red>, <gradient:blue:aqua>Gradient</gradient>)")
public class MainMenuConfig extends OkaeriConfig {

        public String title = "<color:#1E293B>Claim Menu <color:#64748B>· <color:#0369A1>{claim_name}";

        @Comment("Outer border frame")
        public ItemConfig filler1 = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Inner divider frame")
        public ItemConfig filler2 = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

        @Comment("Claim Map Button")
        public ItemConfig claimMap = new ItemConfig(
                        "FILLED_MAP",
                        "<color:#38BDF8>Territory Map",
                        List.of("<color:#9AA5B4>Inspect claimed chunks around", "<color:#9AA5B4>{claim_name} <color:#64748B>({size} chunks)<color:#9AA5B4>."));

        @Comment("Claim Warps Button")
        public ItemConfig warps = new ItemConfig(
                        "COMPASS",
                        "<color:#60A5FA>Claim Warps",
                        List.of("<color:#9AA5B4>Teleport to or configure warps", "<color:#9AA5B4>inside this territory."));

        @Comment("Allies Button")
        public ItemConfig allies = new ItemConfig(
                        "ENDER_CHEST",
                        "<color:#38BDF8>Alliances",
                        List.of("<color:#9AA5B4>Manage allied claims and shared", "<color:#9AA5B4>alliance permissions."));

        @Comment("Claim Settings Button")
        public ItemConfig settings = new ItemConfig(
                        "EMERALD",
                        "<color:#34D399>Claim Settings",
                        List.of("<color:#9AA5B4>Configure PvP, TNT, mob spawning,", "<color:#9AA5B4>roles, rename, and map color."));

        @Comment("Trusted Members Management Button")
        public ItemConfig trusted = new ItemConfig(
                        "GOLDEN_CHESTPLATE",
                        "<color:#60A5FA>Trusted Players",
                        List.of("<color:#9AA5B4>Grant temporary permissions without", "<color:#9AA5B4>full claim membership."));

        @Comment("Members Management Button")
        public ItemConfig members = new ItemConfig(
                        "PLAYER_HEAD",
                        "<color:#38BDF8>Claim Members",
                        List.of("<color:#9AA5B4>Invite players, assign roles,", "<color:#9AA5B4>and manage <color:#F1F5F9>{members} <color:#9AA5B4>member(s)."));

        @Comment("Visitor Settings Button")
        public ItemConfig visitors = new ItemConfig(
                        "OAK_SIGN",
                        "<color:#FBBF24>Visitor Rules",
                        List.of("<color:#9AA5B4>Control what non-members can", "<color:#9AA5B4>interact with inside your claim."));

        @Comment("Close Menu Button")
        public ItemConfig close = new ItemConfig(
                        "BARRIER",
                        "<color:#F87171>Close",
                        List.of("<color:#9AA5B4>Close this menu"));

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
