package org.ayosynk.landClaimPlugin.managers;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.GuiGlobalSetup;
import org.ayosynk.landClaimPlugin.listeners.ChatInputListener;
import org.ayosynk.landClaimPlugin.listeners.CommandBlocker;
import org.ayosynk.landClaimPlugin.listeners.EventListener;
import org.ayosynk.landClaimPlugin.listeners.PlayerJoinListener;
import org.ayosynk.landClaimPlugin.listeners.protections.*;
import org.bukkit.plugin.PluginManager;

/**
 * Manages registration of all plugin event listeners.
 */
public class ListenerManager {

    private final LandClaimPlugin plugin;
    private final ClaimManager claimManager;
    private final ConfigManager configManager;
    private final VisualizationManager visualizationManager;
    private EventListener eventListener;

    public ListenerManager(LandClaimPlugin plugin, ClaimManager claimManager, ConfigManager configManager,
            VisualizationManager visualizationManager) {
        this.plugin = plugin;
        this.claimManager = claimManager;
        this.configManager = configManager;
        this.visualizationManager = visualizationManager;
    }

    public void registerAll() {
        PluginManager pm = plugin.getServer().getPluginManager();

        // Initialize custom GUI framework (listener + scheduler)
        GuiGlobalSetup.init(plugin);
        ChatInputListener.init(plugin);

        // Core Listeners
        eventListener = new EventListener(plugin, claimManager, configManager);
        pm.registerEvents(eventListener, plugin);
        pm.registerEvents(new CommandBlocker(plugin, claimManager), plugin);
        pm.registerEvents(new PlayerJoinListener(plugin, visualizationManager), plugin);

        // Protection Listeners
        pm.registerEvents(new BlockProtectionListener(plugin, claimManager, configManager), plugin);
        pm.registerEvents(new EntityProtectionListener(plugin, claimManager, configManager), plugin);
        pm.registerEvents(new InteractProtectionListener(plugin, claimManager, configManager), plugin);
        pm.registerEvents(new ExplosionProtectionListener(plugin, claimManager), plugin);
        pm.registerEvents(new PistonProtectionListener(plugin, claimManager), plugin);
        pm.registerEvents(new PvpProtectionListener(plugin, claimManager, configManager), plugin);
        pm.registerEvents(new VehicleProtectionListener(plugin, claimManager, configManager), plugin);
        pm.registerEvents(new ItemProtectionListener(plugin, claimManager, configManager), plugin);
    }

    public EventListener getEventListener() {
        return eventListener;
    }
}
