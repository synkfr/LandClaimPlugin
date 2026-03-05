package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.ChangeClaimColorConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.listeners.ChatInputListener;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChangeClaimColorGUI {

        private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

        // Minecraft dye → hex mapping
        private static final Map<String, String> COLOR_HEX = Map.ofEntries(
                        Map.entry("BLACK", "#1D1D21"),
                        Map.entry("BLUE", "#3C44AA"),
                        Map.entry("BROWN", "#835432"),
                        Map.entry("CYAN", "#169C9C"),
                        Map.entry("GRAY", "#474F52"),
                        Map.entry("GREEN", "#5E7C16"),
                        Map.entry("LIGHT_BLUE", "#3AB3DA"),
                        Map.entry("LIME", "#80C71F"),
                        Map.entry("LIGHT_GRAY", "#9D9D97"),
                        Map.entry("MAGENTA", "#C74EBD"),
                        Map.entry("ORANGE", "#F9801D"),
                        Map.entry("PINK", "#F38BAA"),
                        Map.entry("PURPLE", "#8932B8"),
                        Map.entry("RED", "#B02E26"),
                        Map.entry("WHITE", "#F9FFFE"),
                        Map.entry("YELLOW", "#FED83D"));

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        ChangeClaimColorConfig config = plugin.getConfigManager().getChangeClaimColorConfig();

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F 0 1 2 3 4 5 6 F",
                                        "F 7 8 9 A B C D F",
                                        "F F F E X Y F F F",
                                        "F F F N < N F F F"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frameFill.material, config.frameFill.name,
                                        config.frameFill.lore));
                        ingredients.put('N', GuiHelper.buildSlot(config.navFrame.material, config.navFrame.name,
                                        config.navFrame.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));

                        // Custom HEX color input
                        ingredients.put('X', GuiHelper.buildSlot(config.customColor.material, config.customColor.name,
                                        config.customColor.lore, (p, e) -> {
                                                p.closeInventory();
                                                p.sendMessage(
                                                                plugin.getConfigManager()
                                                                                .getMessage("color-hex-prompt"));

                                                ChatInputListener.awaitInput(p, input -> {
                                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                                                if (input == null) {
                                                                        p.sendMessage(plugin.getConfigManager()
                                                                                        .getMessage("rename-cancelled"));
                                                                        return;
                                                                }

                                                                String hex = input.startsWith("#") ? input
                                                                                : "#" + input;
                                                                if (!HEX_PATTERN.matcher(hex).matches()) {
                                                                        p.sendMessage(plugin.getConfigManager()
                                                                                        .getMessage("claim-color-invalid"));
                                                                        return;
                                                                }

                                                                applyColor(p, profile, plugin,
                                                                                hex.toUpperCase());
                                                        });
                                                });
                                        }));

                        // Preset colors
                        ingredients.put('0', buildColorSlot(config.colorBlack, "BLACK", profile, plugin));
                        ingredients.put('1', buildColorSlot(config.colorBlue, "BLUE", profile, plugin));
                        ingredients.put('2', buildColorSlot(config.colorBrown, "BROWN", profile, plugin));
                        ingredients.put('3', buildColorSlot(config.colorCyan, "CYAN", profile, plugin));
                        ingredients.put('4', buildColorSlot(config.colorGray, "GRAY", profile, plugin));
                        ingredients.put('5', buildColorSlot(config.colorGreen, "GREEN", profile, plugin));
                        ingredients.put('6', buildColorSlot(config.colorLightBlue, "LIGHT_BLUE", profile, plugin));
                        ingredients.put('7', buildColorSlot(config.colorLime, "LIME", profile, plugin));
                        ingredients.put('8', buildColorSlot(config.colorLightGray, "LIGHT_GRAY", profile, plugin));
                        ingredients.put('9', buildColorSlot(config.colorMagenta, "MAGENTA", profile, plugin));
                        ingredients.put('A', buildColorSlot(config.colorOrange, "ORANGE", profile, plugin));
                        ingredients.put('B', buildColorSlot(config.colorPink, "PINK", profile, plugin));
                        ingredients.put('C', buildColorSlot(config.colorPurple, "PURPLE", profile, plugin));
                        ingredients.put('D', buildColorSlot(config.colorRed, "RED", profile, plugin));
                        ingredients.put('E', buildColorSlot(config.colorWhite, "WHITE", profile, plugin));
                        ingredients.put('Y', buildColorSlot(config.colorYellow, "YELLOW", profile, plugin));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                                CustomGui gui = new CustomGui(title, 5);
                                gui.fillFromStructure(structure, ingredients);
                                gui.open(player);
                        });
                });
        }

        private static SlotDefinition buildColorSlot(ChangeClaimColorConfig.ItemConfig itemConfig,
                        String colorName, ClaimProfile profile, LandClaimPlugin plugin) {
                String hex = COLOR_HEX.getOrDefault(colorName, "#FFFFFF");
                return GuiHelper.buildSlot(itemConfig.material, itemConfig.name, itemConfig.lore, (p, e) -> {
                        p.closeInventory();
                        applyColor(p, profile, plugin, hex);
                });
        }

        private static void applyColor(Player player, ClaimProfile profile, LandClaimPlugin plugin, String hex) {
                profile.setClaimColor(hex);
                plugin.getDatabaseManager().getProfileDao().saveProfile(profile);

                // Refresh map hooks
                plugin.getHookManager().refreshMapHooks();

                // Refresh visualization if active
                plugin.getVisualizationManager().invalidateCache(profile.getOwnerId());

                player.sendMessage(plugin.getConfigManager().getMessage("claim-color-changed"));
                ChangeClaimColorGUI.open(player, profile, plugin);
        }
}
