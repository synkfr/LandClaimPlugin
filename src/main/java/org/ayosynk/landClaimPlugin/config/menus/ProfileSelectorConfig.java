package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("ProfileSelector GUI Configuration")
@Header("Materials must be valid Bukkit Material enum names.")
@Header("Names and lore support MiniMessage formatting.")
public class ProfileSelectorConfig extends OkaeriConfig {

    public String title = "<color:#1F1F1F>Claim Profiles";

    @Comment("The material used for a profile that is NOT active.")
    public String inactiveProfileMaterial = "BOOK";

    @Comment("The material used for the active profile.")
    public String activeProfileMaterial = "ENCHANTED_BOOK";

    @Comment("Format for the profile item name. Variables: <name>")
    public String profileNameFormat = "<i:false><color:#00E262><b><name>";

    @Comment("Lore added to the profile item if you are the Owner.")
    public String roleOwnerLore = "<i:false><color:#A0A0A0>Role <color:#FFFFFF>Owner";

    @Comment("Lore added to the profile item if you are a Member. Variables: <role>")
    public String roleMemberLore = "<i:false><color:#A0A0A0>Role <color:#FFFFFF><role>";

    @Comment("Lore showing the chunk count. Variables: <count>")
    public String chunksClaimedLore = "<i:false><color:#A0A0A0>Chunks <color:#FFFFFF><count>";

    @Comment("Lore added if the profile is currently active.")
    public String activeProfileLore = "<i:false><color:#00E262>● Active Profile";

    @Comment("Lore added if the profile is NOT active.")
    public String clickToActivateLore = "<i:false><color:#E2B700>Click to activate";

    @Comment("Filler item used in the background.")
    public ItemConfig filler = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Close button.")
    public ItemConfig close = new ItemConfig("BARRIER", "<color:#E20000>Close", List.of("<color:#A0A0A0>Close this menu"));

    @Comment("Previous Page button.")
    public ItemConfig previousPage = new ItemConfig("ARROW", "<color:#00E262>← Previous Page", List.of("<color:#A0A0A0>View previous items"));

    @Comment("Next Page button.")
    public ItemConfig nextPage = new ItemConfig("ARROW", "<color:#00E262>Next Page →", List.of("<color:#A0A0A0>View next items"));

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
