package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RenameClaimConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.listeners.ChatInputListener;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RenameClaimGUI {

        private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{3,32}$");

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        RenameClaimConfig config = plugin.getConfigManager().getRenameClaimConfig();

                        String[] structure = {
                                        "O O B B B B B O O",
                                        "O B B C B R B B O",
                                        "B B B B B B B B B",
                                        "O S < S B B B B B"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('O', GuiHelper.buildSlot(config.outerFrame.material, config.outerFrame.name,
                                        config.outerFrame.lore));
                        ingredients.put('B', GuiHelper.buildSlot(config.background.material, config.background.name,
                                        config.background.lore));
                        ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name,
                                        config.navSpacer.lore));

                        // Change Name: close GUI → chat prompt
                        ingredients.put('C', GuiHelper.buildSlot(config.changeName.material, config.changeName.name,
                                        config.changeName.lore, (p, e) -> {
                                                p.closeInventory();
                                                p.sendMessage(plugin.getConfigManager().getMessage("rename-prompt"));

                                                ChatInputListener.awaitInput(p, input -> {
                                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                                                if (input == null) {
                                                                        p.sendMessage(plugin.getConfigManager()
                                                                                        .getMessage("rename-cancelled"));
                                                                        return;
                                                                }

                                                                if (!NAME_PATTERN.matcher(input).matches()) {
                                                                        p.sendMessage(plugin.getConfigManager()
                                                                                        .getMessage("claim-name-invalid"));
                                                                        return;
                                                                }

                                                                // Check uniqueness
                                                                boolean unique = plugin.getClaimManager()
                                                                                .getAllProfiles().stream()
                                                                                .noneMatch(cp -> cp.getName()
                                                                                                .equalsIgnoreCase(input)
                                                                                                && !cp.getOwnerId()
                                                                                                                .equals(profile.getOwnerId()));

                                                                if (!unique) {
                                                                        p.sendMessage(plugin.getConfigManager()
                                                                                        .getMessage("name-already-in-use"));
                                                                        return;
                                                                }

                                                                profile.setName(input);
                                                                plugin.getDatabaseManager().getProfileDao()
                                                                                .saveProfile(profile);
                                                                p.sendMessage(plugin.getConfigManager().getMessage(
                                                                                "claim-renamed", "<name>", input));
                                                                RenameClaimGUI.open(p, profile, plugin);
                                                        });
                                                });
                                        }));

                        // Reset to Default
                        ingredients.put('R',
                                        GuiHelper.buildSlot(config.resetToDefault.material, config.resetToDefault.name,
                                                        config.resetToDefault.lore, (p, e) -> {
                                                                p.closeInventory();
                                                                String defaultName = Bukkit.getOfflinePlayer(
                                                                                profile.getOwnerId()).getName();
                                                                if (defaultName == null)
                                                                        defaultName = "Unnamed Claim";
                                                                else
                                                                        defaultName = defaultName + "'s Claim";
                                                                String finalName = defaultName;
                                                                ConfirmationGUI.open(p,
                                                                                "<red>Reset claim name?",
                                                                                () -> {
                                                                                        profile.setName(finalName);
                                                                                        plugin.getDatabaseManager()
                                                                                                        .getProfileDao()
                                                                                                        .saveProfile(profile);
                                                                                        p.sendMessage(plugin
                                                                                                        .getConfigManager()
                                                                                                        .getMessage("claim-renamed",
                                                                                                                        "<name>",
                                                                                                                        finalName));
                                                                                        RenameClaimGUI.open(p, profile,
                                                                                                        plugin);
                                                                                },
                                                                                () -> RenameClaimGUI.open(p, profile,
                                                                                                plugin));
                                                        }));

                        // Back
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                ClaimSettingsGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        Bukkit.getScheduler().runTask(plugin, () -> {
                                CustomGui gui = new CustomGui(title, 4);
                                gui.fillFromStructure(structure, ingredients);
                                gui.open(player);
                        });
                });
        }
}
