package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
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
        TitleSettingsConfig config = plugin.getConfigManager().getTitleSettingsConfig();

        String[] structure = {
                "F F F F F F F F F",
                "F F T F E F O F F",
                "F F F F F F F F F",
                "S S S S < S S S S"
        };

        Map<Character, SlotDefinition> ingredients = new HashMap<>();
        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
        ingredients.put('S', GuiHelper.buildSlot(config.navSpacer.material, config.navSpacer.name, config.navSpacer.lore));

        var cm = plugin.getConfigManager();
        String statusVal = cm.getRawTemplate(profile.isEnterTitleEnabled() ? "gui.status-enabled" : "gui.status-disabled");

        List<String> toggleLore = new ArrayList<>(config.titleToggle.lore);
        toggleLore.add("");
        toggleLore.add(cm.getRawTemplate("gui.title-status-label", "<value>", statusVal));
        ingredients.put('T', GuiHelper.buildSlot(config.titleToggle.material, config.titleToggle.name,
                toggleLore, (p, e) -> {
                    profile.setEnterTitleEnabled(!profile.isEnterTitleEnabled());
                    plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                    open(p, profile, plugin);
                }));

        boolean enterIsSub = "SUBTITLE".equalsIgnoreCase(profile.getEnterTitleMode());
        List<String> entryLore = new ArrayList<>(config.onEntry.lore);
        entryLore.add("");
        entryLore.add(cm.getRawTemplate("gui.title-current-text", "<text>", profile.getEnterTitle()));
        entryLore.add(cm.getRawTemplate("gui.title-mode-label", "<value>",
                cm.getRawTemplate(enterIsSub ? "gui.title-mode-subtitle" : "gui.title-mode-title")));
        entryLore.add("");
        entryLore.add(cm.getRawTemplate("gui.title-left-click-edit"));
        entryLore.add(cm.getRawTemplate("gui.title-right-click-mode"));

        ingredients.put('E', GuiHelper.buildSlot(config.onEntry.material, config.onEntry.name,
                entryLore, (p, e) -> {
                    if (e.getClick() == ClickType.RIGHT) {
                        profile.setEnterTitleMode(enterIsSub ? "TITLE" : "SUBTITLE");
                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                        open(p, profile, plugin);
                    } else {
                        AnvilInputGUI.open(plugin, p, cm.getRawTemplate("gui.prompt-enter-title"), profile.getEnterTitle(),
                                input -> {
                                    if (input == null) {
                                        p.sendMessage(cm.getMessage("titles.title-cancelled"));
                                    } else {
                                        profile.setEnterTitle(input);
                                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                                        p.sendMessage(cm.getMessage("titles.title-updated"));
                                    }
                                    FoliaScheduler.runForPlayer(plugin, p, () -> open(p, profile, plugin));
                                });
                    }
                }));

        boolean leaveIsSub = "SUBTITLE".equalsIgnoreCase(profile.getLeaveTitleMode());
        List<String> leaveLore = new ArrayList<>(config.onLeaveTitle.lore);
        leaveLore.add("");
        leaveLore.add(cm.getRawTemplate("gui.title-current-text", "<text>", profile.getLeaveTitle()));
        leaveLore.add(cm.getRawTemplate("gui.title-mode-label", "<value>",
                cm.getRawTemplate(leaveIsSub ? "gui.title-mode-subtitle" : "gui.title-mode-title")));
        leaveLore.add("");
        leaveLore.add(cm.getRawTemplate("gui.title-left-click-edit"));
        leaveLore.add(cm.getRawTemplate("gui.title-right-click-mode"));

        ingredients.put('O', GuiHelper.buildSlot(config.onLeaveTitle.material, config.onLeaveTitle.name,
                leaveLore, (p, e) -> {
                    if (e.getClick() == ClickType.RIGHT) {
                        profile.setLeaveTitleMode(leaveIsSub ? "TITLE" : "SUBTITLE");
                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                        open(p, profile, plugin);
                    } else {
                        AnvilInputGUI.open(plugin, p, cm.getRawTemplate("gui.prompt-leave-title"), profile.getLeaveTitle(),
                                input -> {
                                    if (input == null) {
                                        p.sendMessage(cm.getMessage("titles.title-cancelled"));
                                    } else {
                                        profile.setLeaveTitle(input);
                                        plugin.getDatabaseManager().getProfileDao().saveProfile(profile);
                                        p.sendMessage(cm.getMessage("titles.title-updated"));
                                    }
                                    FoliaScheduler.runForPlayer(plugin, p, () -> open(p, profile, plugin));
                                });
                    }
                }));

        ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                (p, e) -> ClaimSettingsGUI.open(p, profile, plugin)));

        Component title = GuiHelper.MM.deserialize(config.title);
        CustomGui gui = new CustomGui(title, 4);
        gui.fillFromStructure(structure, ingredients);
        gui.open(player);
    }
}
