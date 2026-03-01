package org.ayosynk.landClaimPlugin.commands;

import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * Interface for modular command registration.
 * Each implementing class handles a specific command group
 * and registers its sub-commands via the shared /claim builder.
 */
public interface LandClaimCommand {

    /**
     * Register this command group's sub-commands.
     *
     * @param manager      the Paper command manager
     * @param claimBuilder the shared /claim root builder (senderType already set to
     *                     PlayerSource)
     */
    void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder);
}
