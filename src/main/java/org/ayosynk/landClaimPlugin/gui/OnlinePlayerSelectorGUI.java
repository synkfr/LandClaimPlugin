package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.util.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.OnlinePlayerSelectorConfig;
import org.ayosynk.landClaimPlugin.gui.framework.ClickAction;
import org.ayosynk.landClaimPlugin.gui.framework.GuiItem;
import org.ayosynk.landClaimPlugin.gui.framework.PaginatedGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OnlinePlayerSelectorGUI {

    public static void open(Player player, LandClaimPlugin plugin, Consumer<Player> onSelect, Runnable onBack) {
        FoliaScheduler.runAsync(plugin, () -> {
            OnlinePlayerSelectorConfig config = plugin.getConfigManager().getOnlinePlayerSelectorConfig();

            List<GuiItem> contentItems = new ArrayList<>();

            for (Player online : Bukkit.getOnlinePlayers()) {
                // Don't show the viewer themselves in the list if they are picking someone else
                // Actually, for trust/invite, you usually want someone else.
                if (online.getUniqueId().equals(player.getUniqueId())) continue;

                contentItems.add(new GuiItem() {
                    @Override
                    public ItemStack render(Player viewer) {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        if (meta != null) {
                            meta.setOwningPlayer(online);
                            meta.displayName(GuiHelper.MM.deserialize("<yellow>" + online.getName()));
                            List<Component> lore = new ArrayList<>();
                            lore.add(GuiHelper.MM.deserialize("<gray>Click to select"));
                            meta.lore(lore);
                            head.setItemMeta(meta);
                        }
                        return head;
                    }

                    @Override
                    public ClickAction clickAction() {
                        return (p, e) -> {
                            p.closeInventory();
                            onSelect.accept(online);
                        };
                    }
                });
            }

            String[] structure = {
                    "F F F F F F F F F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "F x x x x x x x F",
                    "P B B B < B B B N"
            };

            Map<Character, SlotDefinition> ingredients = new HashMap<>();
            ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name, config.frame.lore));
            ingredients.put('B', GuiHelper.buildSlot(config.navFill.material, config.navFill.name, config.navFill.lore));
            ingredients.put('<', GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore, (p, e) -> {
                p.closeInventory();
                onBack.run();
            }));

            Component title = GuiHelper.MM.deserialize(config.title);
            PaginatedGui gui = new PaginatedGui(title, 6, structure, ingredients, 'x');

            gui.setPrevButton(45,
                    GuiHelper.buildItemStack(config.previousPage.material, config.previousPage.name, config.previousPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));
            gui.setNextButton(53,
                    GuiHelper.buildItemStack(config.nextPage.material, config.nextPage.name, config.nextPage.lore),
                    GuiHelper.buildItemStack(config.navFill.material, config.navFill.name, config.navFill.lore));

            gui.setContent(contentItems, player);
            gui.open(player);
        });
    }
}
