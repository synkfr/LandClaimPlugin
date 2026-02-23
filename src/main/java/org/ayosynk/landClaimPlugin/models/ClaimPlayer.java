package org.ayosynk.landClaimPlugin.models;

import java.util.UUID;

public class ClaimPlayer {

    private final UUID uniqueId;
    private boolean autoClaim;
    private boolean autoUnclaim;
    private String visualizationMode;
    private int bonusClaimBlocks;

    public ClaimPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.autoClaim = false;
        this.autoUnclaim = false;
        this.visualizationMode = "DEFAULT";
        this.bonusClaimBlocks = 0;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean isAutoClaim() {
        return autoClaim;
    }

    public void setAutoClaim(boolean autoClaim) {
        this.autoClaim = autoClaim;
    }

    public boolean isAutoUnclaim() {
        return autoUnclaim;
    }

    public void setAutoUnclaim(boolean autoUnclaim) {
        this.autoUnclaim = autoUnclaim;
    }

    public String getVisualizationMode() {
        return visualizationMode;
    }

    public void setVisualizationMode(String visualizationMode) {
        this.visualizationMode = visualizationMode;
    }

    public int getBonusClaimBlocks() {
        return bonusClaimBlocks;
    }

    public void setBonusClaimBlocks(int bonusClaimBlocks) {
        this.bonusClaimBlocks = bonusClaimBlocks;
    }
}
