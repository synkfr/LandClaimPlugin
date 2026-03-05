package org.ayosynk.landClaimPlugin.gui;

import net.kyori.adventure.text.Component;
import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.config.menus.RoleSetupConfig;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.SlotDefinition;
import org.ayosynk.landClaimPlugin.models.ClaimProfile;
import org.ayosynk.landClaimPlugin.models.Role;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RoleSetupGUI implements Listener {

        private static final Map<UUID, RoleSetupSession> activeSessions = new ConcurrentHashMap<>();

        // Static initialization to register the listener once
        private static boolean listenerRegistered = false;

        private record RoleSetupSession(ClaimProfile profile, Role role) {
        }

        public static void open(Player player, ClaimProfile profile, LandClaimPlugin plugin, Role role) {
                if (!listenerRegistered) {
                        Bukkit.getPluginManager().registerEvents(new RoleSetupGUI(), plugin);
                        listenerRegistered = true;
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        RoleSetupConfig config = plugin.getConfigManager().getRoleSetupConfig();

                        // Setup a temporary role representation if creating new
                        final Role workingRole;
                        final boolean isNew;
                        if (role == null) {
                                isNew = true;
                                workingRole = new Role(UUID.randomUUID(), profile.getOwnerId(), "NewRole", 0);
                        } else {
                                isNew = false;
                                workingRole = role; // Edit existing by reference
                        }

                        String[] structure = {
                                        "F F F F F F F F F",
                                        "F F N F P F T F F",
                                        "F F F F F F F F F",
                                        "G G G < G S G G G"
                        };

                        Map<Character, SlotDefinition> ingredients = new HashMap<>();
                        ingredients.put('F', GuiHelper.buildSlot(config.frame.material, config.frame.name,
                                        config.frame.lore));
                        ingredients.put('G', GuiHelper.buildSlot(config.navFill.material, config.navFill.name,
                                        config.navFill.lore));

                        ingredients.put('N', GuiHelper.buildSlot(config.setName.material,
                                        config.setName.name.replace("<name>", workingRole.getName()),
                                        config.setName.lore, (p, e) -> {
                                                // Chat input for role name
                                                p.closeInventory();
                                                activeSessions.put(p.getUniqueId(),
                                                                new RoleSetupSession(profile, workingRole));
                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                "<yellow>Please type the new role name in chat. Type <red>'cancel' <yellow>to abort."));
                                        }));

                        ingredients.put('P', GuiHelper.buildSlot(config.permissions.material, config.permissions.name,
                                        config.permissions.lore, (p, e) -> {
                                                p.closeInventory();
                                                RoleEditGUI.open(p, profile, plugin, workingRole, isNew);
                                        }));

                        ingredients.put('T', GuiHelper.buildSlot(config.setPriority.material, config.setPriority.name,
                                        List.of("<gray>Priority hierarchy is coming soon!"), (p, e) -> {
                                                p.sendMessage(GuiHelper.MM.deserialize(
                                                                "<yellow>Priority management is coming soon!"));
                                        }));

                        ingredients.put('<',
                                        GuiHelper.buildSlot(config.back.material, config.back.name, config.back.lore,
                                                        (p, e) -> {
                                                                p.closeInventory();
                                                                RoleManagementGUI.open(p, profile, plugin);
                                                        }));

                        ingredients.put('S', GuiHelper.buildSlot(config.saveExit.material, config.saveExit.name,
                                        config.saveExit.lore, (p, e) -> {
                                                if (isNew) {
                                                        profile.addRole(workingRole);
                                                }

                                                plugin.getCacheManager().getProfileCache().put(profile.getOwnerId(),
                                                                profile);
                                                plugin.getClaimManager().saveAndSync(profile);

                                                p.sendMessage(GuiHelper.MM
                                                                .deserialize("<green>Role saved successfully!"));
                                                p.closeInventory();
                                                RoleManagementGUI.open(p, profile, plugin);
                                        }));

                        Component title = GuiHelper.MM.deserialize(config.title);
                        CustomGui gui = new CustomGui(title, 4);
                        gui.fillFromStructure(structure, ingredients);
                        gui.open(player);
                });
        }

        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
                Player player = event.getPlayer();
                RoleSetupSession session = activeSessions.remove(player.getUniqueId());

                if (session != null) {
                        event.setCancelled(true);
                        String input = event.getMessage().trim();

                        if (input.equalsIgnoreCase("cancel")) {
                                player.sendMessage(GuiHelper.MM.deserialize("<red>Name change cancelled."));
                        } else {
                                // Remove spaces and special chars to keep names clean
                                String cleanName = input.replaceAll("[^a-zA-Z0-9_-]", "");
                                if (cleanName.isEmpty()) {
                                        player.sendMessage(GuiHelper.MM.deserialize(
                                                        "<red>Invalid role name. Must contain letters/numbers."));
                                } else if (cleanName.equalsIgnoreCase("Member")
                                                || cleanName.equalsIgnoreCase("CoOwner")) {
                                        player.sendMessage(GuiHelper.MM
                                                        .deserialize("<red>You cannot use default role names."));
                                } else {
                                        session.role().setName(cleanName);
                                        player.sendMessage(GuiHelper.MM
                                                        .deserialize("<green>Role name set to <white>" + cleanName));
                                }
                        }

                        // Re-open GUI
                        Bukkit.getScheduler().runTask(LandClaimPlugin.getPlugin(LandClaimPlugin.class), () -> {
                                RoleSetupGUI.open(player, session.profile(),
                                                LandClaimPlugin.getPlugin(LandClaimPlugin.class),
                                                session.role());
                        });
                }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
                activeSessions.remove(event.getPlayer().getUniqueId());
        }
}
