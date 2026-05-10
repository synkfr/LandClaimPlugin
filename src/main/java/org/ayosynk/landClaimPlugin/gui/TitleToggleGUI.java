package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.TitleSettingsConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TitleToggleGUI {

    public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TitleSettingsConfig config = plugin.getConfigManager().getTitleSettingsConfig();

            String[] structure = {
                    "F F F F F F F F F",
                    "F F T F E F O F F",
                    "F F F F F F F F F",
                    "F F F S < S F F F"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                    config.frame.lore));
            ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name,
                    config.navSpacer.lore));

            // --- Toggle button ---
            List<String> toggleLore = new ArrayList<>(config.titleToggle.lore);
            toggleLore.add("");
            toggleLore.add("<gray>Status: "
                    + (profile.isEnterTitleEnabled() ? "<green>Enabled" : "<red>Disabled"));
            ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                    toggleLore, (p, e) -> {
                        profile.setEnterTitleEnabled(!profile.isEnterTitleEnabled());
                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                        open(player, profile, plugin);
                    }));

            // --- Entry title button ---
            boolean enterIsSub = "SUBTITLE".equalsIgnoreCase(profile.getEnterTitleMode());
            List<String> entryLore = new ArrayList<>(config.onEntry.lore);
            entryLore.add("");
            entryLore.add("<gray>Text: <reset>" + profile.getEnterTitle());
            entryLore.add("<gray>Mode: " + (enterIsSub ? "<aqua>Subtitle" : "<gold>Title"));
            entryLore.add("");
            entryLore.add("<dark_gray><i>Left-click to set text");
            entryLore.add("<dark_gray><i>Right-click to toggle Title/Subtitle");

            ingredients.put('E', GuiHelper.buildSlot(config.onEntry.material, config.onEntry.name,
                    entryLore, (p, e) -> {
                        if (e.getClick() == ClickType.RIGHT) {
                            // Toggle mode
                            profile.setEnterTitleMode(enterIsSub ? "TITLE" : "SUBTITLE");
                            plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                            open(player, profile, plugin);
                        } else {
                            // Open chat input to set text
                            p.closeInventory();
                            p.sendMessage(GuiHelper.MM.deserialize(plugin.getConfigManager()
                                    .getMessagesConfig().titleEnterPrompt));
                            org.ayosynk.landClaimPlugin.listeners.ChatInputListener.awaitInput(p,
                                    input -> {
                                        if (input == null) {
                                            p.sendMessage(GuiHelper.MM.deserialize(
                                                    plugin.getConfigManager()
                                                            .getMessagesConfig().titleCancelled));
                                        } else {
                                            profile.setEnterTitle(input);
                                            plugin.getDatabaseManager()
                                                    .getProfileDao()
                                                    .saveProfile(profile);
                                            p.sendMessage(GuiHelper.MM.deserialize(
                                                    plugin.getConfigManager()
                                                            .getMessagesConfig().titleUpdated));
                                        }
                                        Bukkit.getScheduler().runTask(plugin,
                                                () -> open(p, profile, plugin));
                                    });
                        }
                    }));

            // --- Leave title button ---
            boolean leaveIsSub = "SUBTITLE".equalsIgnoreCase(profile.getLeaveTitleMode());
            List<String> leaveLore = new ArrayList<>(config.onLeaveTitle.lore);
            leaveLore.add("");
            leaveLore.add("<gray>Text: <reset>" + profile.getLeaveTitle());
            leaveLore.add("<gray>Mode: " + (leaveIsSub ? "<aqua>Subtitle" : "<gold>Title"));
            leaveLore.add("");
            leaveLore.add("<dark_gray><i>Left-click to set text");
            leaveLore.add("<dark_gray><i>Right-click to toggle Title/Subtitle");

            ingredients.put('O', GuiHelper.buildSlot(config.onLeaveTitle.material, config.onLeaveTitle.name,
                    leaveLore, (p, e) -> {
                        if (e.getClick() == ClickType.RIGHT) {
                            // Toggle mode
                            profile.setLeaveTitleMode(leaveIsSub ? "TITLE" : "SUBTITLE");
                            plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                            open(player, profile, plugin);
                        } else {
                            // Open chat input to set text
                            p.closeInventory();
                            p.sendMessage(GuiHelper.MM.deserialize(plugin.getConfigManager()
                                    .getMessagesConfig().titleLeavePrompt));
                            org.ayosynk.landClaimPlugin.listeners.ChatInputListener.awaitInput(p,
                                    input -> {
                                        if (input == null) {
                                            p.sendMessage(GuiHelper.MM.deserialize(
                                                    plugin.getConfigManager()
                                                            .getMessagesConfig().titleCancelled));
                                        } else {
                                            profile.setLeaveTitle(input);
                                            plugin.getDatabaseManager()
                                                    .getProfileDao()
                                                    .saveProfile(profile);
                                            p.sendMessage(GuiHelper.MM.deserialize(
                                                    plugin.getConfigManager()
                                                            .getMessagesConfig().titleUpdated));
                                        }
                                        Bukkit.getScheduler().runTask(plugin,
                                                () -> open(p, profile, plugin));
                                    });
                        }
                    }));

            ingredients.put('<',
                    GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                            (p, e) -> {
                                p.closeInventory();
                                ClaimSettingsGUI.open(p, profile, plugin);
                            }));

            Component title = GuiHelper.MM.deserialize(config.title);
            CustomGui gui = new CustomGui(title, 4);
            gui.fillFromStructure(structure, ingredients);
            gui.open(player);
        });
    }
}
