package org.ayosynk.landClaimPlugin.gui;

import org.ayosynk.landClaimPlugin.LandClaimPlugin;
import org.ayosynk.landClaimPlugin.gui.framework.CustomGui;
import org.ayosynk.landClaimPlugin.gui.framework.GuiListener;

/**
 * Registers the custom GUI framework at plugin startup.
 * Must be called once during {@code onEnable()}, before any GUI is opened.
 */
public final class GuiGlobalSetup {

    private GuiGlobalSetup() {
    }

    /**
     * Initialize the GUI framework.
     * <ul>
     * <li>Sets the plugin reference for scheduler access</li>
     * <li>Registers the global {@link GuiListener}</li>
     * </ul>
     *
     * @param plugin the main plugin instance
     */
    public static void init(LandClaimPlugin plugin) {
        CustomGui.setPlugin(plugin);
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
    }
}
