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
    public String claimInfoManage = "\n<gray>[<green><click:run_command:'/claim member'><hover:show_text:'Click to manage claim members'>Manage Members</hover></click></green>] [<gold><click:run_command:'/claim'><hover:show_text:'Click to manage claim setings'>Manage Claim</hover></click></gold>]</gray>";

    public String wandGiven = "<green>Claim Wand received! Left-click to set Position 1, Right-click to set Position 2.";
    public String wandPos1 = "<green>Position 1 set to chunk <gold><x>, <z></gold> in <gold><world></gold>.";
    public String wandPos2 = "<green>Position 2 set to chunk <gold><x>, <z></gold> in <gold><world></gold>.";

    public String actionbarWilderness = "<green>Wilderness";
    public String actionbarOwnedByYou = "<green>Your Claim";
    public String actionbarOwnedByOther = "<red><owner>'s Claim";
    public String actionbarTrusted = "<yellow><owner>'s Claim <gray>(Trusted)";
    public String actionbarAdmin = "<red><owner>'s Claim <gray>(Admin Override)";

    @Comment("Variables: <player>, <count>, <limit>, <seconds>, <world>, <x>, <z>")
    public String homeSet = "<green>Home <gold><name></gold> has been set!";
    public String homeTeleported = "<green>Teleported to home <gold><name></gold>.";
    public String homeNotFound = "<red>Home <gold><name></gold> not found.";
    public String unstuckCooldown = "<red>You must wait <seconds> seconds before using unstuck again.";
    public String unstuckSuccess = "<green>You have been safely unstuck.";

    // Missing messages from various managers and commands
    public String accessDeniedInteract = "<red>You do not have permission to interact here.";
    public String autoUnclaimed = "<green>Chunk automatically unclaimed because you broke the last block.";
    public String bucketDenied = "<red>You do not have permission to use buckets here.";
    public String harmEntityDenied = "<red>You do not have permission to harm entities here.";
    public String pvpDenied = "<red>PvP is disabled in this claim.";
    public String worldBlocked = "<red>You cannot claim land in this world.";
    public String alreadyClaimed = "<red>This chunk is already claimed by <owner>.";
    public String claimLimitReached = "<red>You have reached your claim limit of <limit>.";
    public String notConnected = "<red>Your claims must be connected.";
    public String tooCloseToWorldguard = "<red>You are too close to a WorldGuard region. Minimum gap: <gap>";
    public String tooCloseToOtherClaim = "<red>You are too close to another player's claim. Minimum gap: <gap>";
    public String commandBlocked = "<red>This command is blocked in claims.";
    public String roleRemoved = "<green>Role assigned to player successfully removed.";
    public String roleAssigned = "<green>Role successfully assigned.";
    public String claimListTotal = "<green>Total claims: <gold><count>";
    public String claimListWorld = "<green><world> Claims:";
    public String claimListEntry = "<gray>- <gold><x>, <z>";
    public String homeListEntry = "<gray>- <gold><name> <gray>(<x>, <z>)";
    public String claimInfoTrusted = "<green>Trusted: <gold><players>";

    // Commands hardcoded messages
    public String chunksClaimedSuccess = "<green>Successfully claimed <count> chunks!";
    public String claimFailed = "<red>Failed to claim chunks. Limit reached or already claimed.";
    public String selectionRequired = "<red>You must make a selection first using the Claim Wand.";
    public String subclaimMustBeOwned = "<red>All chunks in a sub-claim must be within your own claim.";
    public String subclaimAlreadyExists = "<red>A sub-claim already exists here.";
    public String subclaimOverlap = "<red>A sub-claim cannot overlap multiple different claims.";
    public String subclaimCreated = "<green>Successfully created sub-claim/zone with <count> chunks!";
    public String notInClaim = "<red>You are not standing in a claim.";
    public String autoClaimEnabled = "<green>Auto-claim enabled.";
    public String autoClaimDisabled = "<red>Auto-claim disabled.";
    public String autoUnclaimEnabled = "<green>Auto-unclaim enabled.";
    public String autoUnclaimDisabled = "<red>Auto-unclaim disabled.";

    // CommandHandler specifics
    public String menuOpenedStub = "<green>Opened <menu>";
    public String memberListStub = "<green>Member list functionality coming soon";
    public String memberInvited = "<green>Invited <player>";
    public String memberKicked = "<red>Kicked <player>";
    public String trustListStub = "<green>Trust list functionality coming soon";
    public String trustAdded = "<green>Added trust to <player>";
    public String trustRemoved = "<red>Removed trust from <player>";
    public String claimVisibilityToggled = "<green>Claim visibility toggled.";
    public String adminBypassedUnclaim = "<green>Bypassed ownership and unclaimed chunk.";
    public String adminClaimInfoOwnedBy = "<red>[ADMIN] <green>Claim owned by: <gold><owner> <gray>(<uuid>)";
    public String adminClaimInfoId = "<green>Claim ID: <gray><id>";
}
