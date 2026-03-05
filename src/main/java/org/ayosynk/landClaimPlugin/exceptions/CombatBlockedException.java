package org.ayosynk.landClaimPlugin.exceptions;

public class CombatBlockedException extends RuntimeException {
    public CombatBlockedException() {
        super("Player is currently in combat and cannot perform this action.");
    }
}
