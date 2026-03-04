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
    public String inCombat = "<red>You cannot execute LandClaim commands while in combat!</red>";

    public String claimInfoOwner = "<gray>Claim Owner: <gold><owner>";
    public String claimInfoManage = "\n<gray>[<green><click:run_command:'/claim member'><hover:show_text:'Click to manage claim members'>Manage Members</hover></click></green>] [<gold><click:run_command:'/claim'><hover:show_text:'Click to manage claim setings'>Manage Claim</hover></click></gold>]</gray>";

    public String wandGiven = "<green>Claim Wand received! Left-click to set Position 1, Right-click to set Position 2.";
    public String wandPos1 = "<green>Position 1 set to chunk <gold><x>, <z></gold> in <gold><world></gold>.";
    public String wandPos2 = "<green>Position 2 set to chunk <gold><x>, <z></gold> in <gold><world></gold>.";

    public String actionbarWilderness = "<gradient:#5ee7df:#b490ca>🌲 Wilderness 🌲</gradient>";
    public String actionbarOwnedByYou = "<gradient:#a8ff78:#78ffd6>🏠 Your Claim 🏠</gradient>";
    public String actionbarOwnedByOther = "<gradient:#ff416c:#ff4b2b>🔒 <owner>'s Claim 🔒</gradient>";
    public String actionbarTrusted = "<gradient:#f6d365:#fda085>🤝 <owner>'s Claim 🤝</gradient> <gray>(Trusted)</gray>";
    public String actionbarAdmin = "<gradient:#ff416c:#ff4b2b>🛡️ <owner>'s Claim 🛡️</gradient> <gray>(Admin Override)</gray>";

    @Comment("Variables: <player>, <count>, <limit>, <seconds>, <world>, <x>, <z>")
    public String homeSet = "<green>Home <gold><name></gold> has been set!";
    public String homeTeleported = "<green>Teleported to home <gold><name></gold>.";
    public String homeNotFound = "<red>Home <gold><name></gold> not found.";
    public String warpSet = "<green>Warp <gold><name></gold> has been set!";
    public String warpDeleted = "<red>Warp <gold><name></gold> has been deleted.";
    public String warpTeleport = "<green>Teleported to warp <gold><name></gold>.";
    public String warpNotFound = "<red>Warp <gold><name></gold> not found.";
    public String warpLimitReached = "<red>You have reached your warp limit.";
    public String warpLocationUpdated = "<green>Warp <gold><name></gold> location updated!";
    public String warpIconUpdated = "<green>Warp <gold><name></gold> icon updated!";
    public String notInOwnClaim = "<red>You must be in your own claim to set a warp.";
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
    public String nameAlreadyInUse = "<red>That claim name is already taken. Please choose another one.";
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
    public String memberInvited = "<green>You have invited <gold><player></gold> to your claim.";
    public String memberInviteReceived = "<green>You have been invited to <gold><owner></gold>'s claim!\n<gray>[<green><click:run_command:'/claim accept'><hover:show_text:'Click to accept the invite!'>Accept</hover></click></green>] [<red><click:run_command:'/claim deny'><hover:show_text:'Click to deny the invite'>Deny</hover></click></red>]</gray>";
    public String memberKicked = "<red>You have kicked <gold><player></gold> from the claim.";
    public String inviteAcceptedOwner = "<green><gold><player></gold> has accepted your invite and joined the claim!";
    public String inviteAcceptedTarget = "<green>You joined <gold><owner></gold>'s claim!";
    public String inviteDeniedTarget = "<red>You denied the invite to <gold><owner></gold>'s claim.";
    public String noPendingInvites = "<red>You have no pending invites.";
    public String playerNotOnline = "<red>That player is not currently online.";
    public String alreadyInClaim = "<red>That player is already a member of this claim.";

    // Ally Messages
    public String claimNotFound = "<red>Could not find a claim with that name.";
    public String cannotAllySelf = "<red>You cannot ally your own claim.";
    public String alreadyAllied = "<red>You are already allied with that claim.";
    public String allyInviteSent = "<green>Ally invite sent to <gold><name></gold>.";
    public String allyInviteReceived = "<green>Your claim has received an ally invite from <gold><name></gold>!\n<gray>[<green><click:run_command:'/claim ally accept <name>'><hover:show_text:'Accept'>Accept</hover></click></green>] [<red><click:run_command:'/claim ally deny <name>'><hover:show_text:'Deny'>Deny</hover></click></red>]</gray>";
    public String noAllyInviteFrom = "<red>No pending ally invite from claim <gold><name></gold>.";
    public String specifyAllyName = "<red>Please specify the claim name: /claim ally accept <name>";
    public String allyAccepted = "<green>You are now allied with <gold><name></gold>!";
    public String allyAcceptedTarget = "<green><gold><name></gold> accepted your ally invite!";
    public String allyDenied = "<red>Ally invite from <gold><name></gold> was denied.";
    public String notAllied = "<red>You are not allied with that claim.";
    public String allyRemoved = "<red>Alliance with <gold><name></gold> has been removed.";

    public String trustListStub = "<green>Trust list functionality coming soon";
    public String trustAdded = "<green>Added trust to <player>";
    public String trustRemoved = "<red>Removed trust from <player>";
    public String claimVisibilityToggled = "<green>Claim visibility toggled.";
    public String adminBypassedUnclaim = "<green>Bypassed ownership and unclaimed chunk.";
    public String adminClaimInfoOwnedBy = "<red>[ADMIN] <green>Claim owned by: <gold><owner> <gray>(<uuid>)";
    public String adminClaimInfoId = "<green>Claim ID: <gray><id>";

    // --- Profile Lifecycle ---
    public String noProfile = "<red>You don't have a claim profile yet. Claim some land first!";
    public String cannotClaimAsMember = "<red>You cannot claim land while you are a member of another claim.";
    public String profileCreated = "<green>Your claim profile has been created!";
    public String alreadyHasProfile = "<red>You already have a claim profile.";
    public String profileAbandoned = "<red>All your claims have been abandoned.";
    public String unclaimAllConfirm = "<yellow>Are you sure? Type /claim abandon confirm to proceed.";

    // --- Trust / Member ---
    public String notAMember = "<red>That player is not a member of this claim.";
    public String notTrusted = "<red>That player is not trusted in this claim.";
    public String alreadyTrusted = "<red>That player is already trusted.";
    public String cannotTrustSelf = "<red>You cannot trust yourself.";
    public String targetAlreadyInClaim = "<red>That player is already in another claim.";
    public String youWereKicked = "<red>You have been kicked from the claim.";

    // --- Invite Flow ---
    public String inviteExpired = "<red>That invite has expired.";
    public String inviteReceived = "<green>You have been invited to join a claim!";
    public String inviteAccepted = "<green>You have accepted the invite!";
    public String inviteDenied = "<red>You have denied the invite.";
    public String inviteWasDenied = "<red>Your invite was denied.";
    public String memberJoined = "<green><player> has joined your claim!";
    public String memberListHeader = "<gold>Members:";
    public String memberListEntry = "<gray>- <gold><player> <gray>(<role>)";
    public String memberListEmpty = "<gray>No members.";
    public String trustListHeader = "<gold>Trusted Players:";
    public String trustListEntry = "<gray>- <gold><player>";
    public String trustListEmpty = "<gray>No trusted players.";

    // --- Rename ---
    public String claimRenamed = "<green>Claim renamed to <gold><name></gold>.";
    public String claimNameInvalid = "<red>Invalid name. Use 3-32 characters (letters, numbers, spaces).";
    public String renamePrompt = "<yellow>Type the new claim name in chat (or 'cancel' to abort):";
    public String renameCancelled = "<gray>Rename cancelled.";

    // --- Color ---
    public String claimColorChanged = "<green>Claim color changed!";
    public String claimColorInvalid = "<red>Invalid hex color. Use format: #RRGGBB";
    public String colorHexPrompt = "<yellow>Type a hex color code in chat (e.g. #FF5500, or 'cancel'):";

    // --- Visibility ---
    public String visibilityModeChanged = "<green>Visualization mode set to <gold><mode></gold>.";
}
