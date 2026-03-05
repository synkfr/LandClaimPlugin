package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.MemberManagementConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class MemberManagementGUI {

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        MemberManagementConfig config = plugin.getConfigManager().getMemberManagementConfig();

                        List<GuiItem> contentItems = new ArrayList<>();

                        // Build player head items for each member
                        for (Map.Entry<UUID, String> entry : profile.getMemberRoles().entrySet()) {
                                UUID memberId = entry.getKey();
                                String role = entry.getValue();

                                String memberName = Bukkit.getOfflinePlayer(memberId).getName();
                                if (memberName == null)
                                        memberName = memberId.toString();
                                final String displayName = memberName;

                                contentItems.add(new GuiItem() {
                                        @Override
                                        public ItemStack render(Player viewer) {
                                                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                                                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                                                if (meta != null) {
                                                        meta.setOwningPlayer(Bukkit.getOfflinePlayer(memberId));
                                                        meta.displayName(GuiHelper.MM
                                                                        .deserialize("<yellow>" + displayName));

                                                        List<Component> lore = new ArrayList<>();
                                                        lore.add(GuiHelper.MM
                                                                        .deserialize("<gray>Role: <white>" + role));
                                                        lore.add(Component.empty());
                                                        lore.add(GuiHelper.MM.deserialize("<yellow>Click to manage"));
                                                        meta.lore(lore);
                                                        skull.setItemMeta(meta);
                                                }
                                                return skull;
                                        }

                                        @Override
                                        public ClickAction clickAction() {
                                                return (p, e) -> {
                                                        p.closeInventory();
                                                        PlayerControlPanelGUI.open(p, profile, plugin, memberId,
                                                                        displayName);
                                                };
                                        }
                                });
                        }

                        String[] structure = {
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "x x x x x x x x x",
                                        "P B B B < B B B N"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('B', GuiHelper.buildSlot(config.bottomFill.material, config.bottomFill.name,
                                        config.bottomFill.lore));
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
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));
                        gui.setNextButton(35,
                                        GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name,
                                                        config.nextPage.lore),
                                        GuiHelper.buildItemStack(config.bottomFill.material, config.bottomFill.name,
                                                        config.bottomFill.lore));

                        gui.setContent(contentItems, player);
                        gui.open(player);
                });
        }
}
