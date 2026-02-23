package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

@Header("LandClaimPlugin - Messages Configuration")
@Header("Uses MiniMessage formatting exclusively: https://docs.advntr.dev/minimessage/format.html")
public class MessagesConfig extends OkaeriConfig {

    public String accessDenied = "<red>You do not have permission to do this.";
    public String chunkClaimed = "<green>Chunk successfully claimed!";
    public String chunkUnclaimed = "<green>Chunk successfully unclaimed!";
    public String notOwner = "<red>You do not own this claim.";
    public String playerNotFound = "<red>Player not found.";
    public String invalidCommand = "<red>Invalid command usage.";
    public String reloaded = "<green>Configuration and messages reloaded successfully!";

    public String claimInfoOwner = "<gray>Claim Owner: <gold><owner>";
    public String claimInfoTrusted = "<gray>Trusted: <green><players>";
    public String claimInfoManage = "\n<gray>[<green><click:run_command:'/claim member'><hover:show_text:'Click to manage claim members'>Manage Members</hover></click></green>] [<gold><click:run_command:'/claim'><hover:show_text:'Click to manage claim setings'>Manage Claim</hover></click></gold>]</gray>";

    public String actionbarWilderness = "<green>Wilderness";
    public String actionbarOwnedByYou = "<green>Your Claim";
    public String actionbarOwnedByOther = "<red><owner>'s Claim";

    @Comment("Variables: <player>, <count>, <limit>, <seconds>, <world>, <x>, <z>")
    public String homeSet = "<green>Home <gold><name></gold> has been set!";
    public String homeTeleported = "<green>Teleported to home <gold><name></gold>.";
    public String homeNotFound = "<red>Home <gold><name></gold> not found.";
    public String unstuckCooldown = "<red>You must wait <seconds> seconds before using unstuck again.";
    public String unstuckSuccess = "<green>You have been safely unstuck.";
}
