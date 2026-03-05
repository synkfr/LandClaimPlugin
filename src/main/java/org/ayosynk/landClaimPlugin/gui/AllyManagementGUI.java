package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.AllyManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllyManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        AllyManagementConfig config = plugin.getConfigManager().getAllyManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        for (UUID allyOwnerId : profile.getAllyFlags().keySet()) {
                                ClaimProfile allyProfile = plugin.getClaimManager().getProfile(allyOwnerId);
                                if (allyProfile == null)
                                        continue;

                                String allyName = allyProfile.getName();
                                String ownerName = Bukkit.getOfflinePlayer(allyOwnerId).getName();
                                if (ownerName == null)
                                        ownerName = "Unknown";
                                final String finalOwnerName = ownerName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                                                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                                                if (meta != null) {
                                                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(allyOwnerId));
                                                        meta.displayName(GuiHelper.MM.deserialize("<gold>" + allyName));

                                                        List<Component> lore = new ArrayList<>();
                                                        lore.add(GuiHelper.MM.deserialize(
                                                                        "<gray>Owner: <white>" + finalOwnerName));
                                                        lore.add(Component.empty());
                                                        lore.add(GuiHelper.MM.deserialize(
                                                                        "<yellow>Click to manage permissions"));
                                                        meta.lore(lore);
                                                        skull.setItemMeta(meta);
                                                }
                                                return skull;
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        p.closeInventory();
                                                        AllyControlPanelGUI.open(p, profile, plugin, allyProfile);
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P N N N < N N N V"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('N', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));
                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                MainMenuGUI.open(p, profile, plugin);
                                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        PaginatedGui gui = new PaginatedGui(title, 4, structure, ingredients, 'x');

                        gui.setPrevButton(27,
                                        GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name,
                                                        config.previousPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.navFill.material, config.navFill.name,
                                                        config.navFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
