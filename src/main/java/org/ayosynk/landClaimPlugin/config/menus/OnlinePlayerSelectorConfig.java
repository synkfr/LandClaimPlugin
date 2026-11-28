package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("OnlinePlayerSelector GUI Configuration")
public class OnlinePlayerSelectorConfig extends OkaeriConfig {

    public String title = "<color:#1E293B>Select Player";

    @Comment("Outer border frame")
    public ItemConfig frame = new ItemConfig("BLACK_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation bar background")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Back",
            List.of("<color:#9AA5B4>Return to previous menu"));

    @Comment("Previous Page Button")
    public ItemConfig previousPage = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>← Previous Page",
            List.of("<color:#9AA5B4>View previous items"));

    @Comment("Next Page Button")
    public ItemConfig nextPage = new ItemConfig(
            "ARROW",
            "<color:#38BDF8>Next Page →",
            List.of("<color:#9AA5B4>View next items"));

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
