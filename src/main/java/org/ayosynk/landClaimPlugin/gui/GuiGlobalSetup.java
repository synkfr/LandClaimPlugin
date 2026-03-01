package org.ayosynk.landClaimPlugin.gui;

import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.Structure;

/**
 * Registers global InvUI ingredients at plugin startup.
 * Global ingredients are automatically applied to every Gui builder,
 * eliminating per-GUI addIngredient() calls and reducing object allocation.
 */
public final class GuiGlobalSetup {

    private GuiGlobalSetup() {
    }

    /**
     * Call once during plugin enable (before any GUI is opened).
     * Registers content list slot marker as a global ingredient.
     */
    public static void init() {
        Structure.addGlobalIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL);
    }
}
