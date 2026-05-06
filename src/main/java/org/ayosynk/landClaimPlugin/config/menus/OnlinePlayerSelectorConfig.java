package org.ayosynk.landClaimPlugin.config.menus;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import java.util.List;

@Header("OnlinePlayerSelector GUI Configuration")
public class OnlinePlayerSelectorConfig extends OkaeriConfig {

    public String title = "Select Player";

    @Comment("Decorative border")
    public ItemConfig frame = new ItemConfig("WHITE_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Navigation bar background")
    public ItemConfig navFill = new ItemConfig("GRAY_STAINED_GLASS_PANE", " ", List.of());

    @Comment("Return to previous menu")
    public ItemConfig back = new ItemConfig(
            "SPECTRAL_ARROW",
            "<yellow>Back",
            List.of("<gray>Return to Previous Menu"));

    @Comment("Previous Page Button")
    public ItemConfig previousPage = new ItemConfig(
            "LIGHT_BLUE_CANDLE",
            "<aqua>Previous Page",
            List.of("<gray>Go to the previous page"));

    @Comment("Next Page Button")
    public ItemConfig nextPage = new ItemConfig(
            "LIME_CANDLE",
            "<green>Next Page",
            List.of("<gray>Go to the next page"));

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
