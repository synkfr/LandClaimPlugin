package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

@Header("LandClaimPlugin - Economy Configuration")
public class EconomyConfig extends OkaeriConfig {

    @Comment("Cost to claim a chunk")
    public double chunkClaimCost = 100.0;

    @Comment("Cost to claim additional allowance (claim blocks)")
    public double claimBlockCost = 10.0;

    @Comment("Taxes and Upkeep")
    public boolean upkeepEnabled = false;
    public double dailyUpkeepCost = 10.0;

    @Comment("Whether claims auto-expire if upkeep is not met")
    public boolean expireOnFailure = false;
    public int gracePeriodDays = 3;
}
