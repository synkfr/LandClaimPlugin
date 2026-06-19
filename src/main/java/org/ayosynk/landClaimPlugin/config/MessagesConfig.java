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
    public String adminAddChunkSuccess = "<green>Successfully gave <gold><amount></gold> bonus chunks to <gold><player></gold>!";
    public String adminEditingProfile = "<red>[ADMIN] <green>Now editing <gold><player></gold>'s claim profile.";
    public String adminTrustAdded = "<red>[ADMIN] <green>Added trust override for <gold><player></gold> in <gold><owner></gold>'s claim.";
    public String adminTrustRemoved = "<red>[ADMIN] <green>Removed trust override for <gold><player></gold> in <gold><owner></gold>'s claim.";
    public String noProfileFound = "<red>That player does not have a claim profile.";
    public String memberInviteSent = "<green>Invite sent to <gold><player></gold>.";
    public String allyAdded = "<green>You are now allied with <gold><player></gold>'s claim!";
    public String cannotInviteSelf = "<red>You cannot invite yourself to your own claim.";
    public String alreadyMember = "<red>That player is already a member of this claim.";
    public String invalidCommand = "<red>Invalid command usage.";
    public String reloaded = "<green>Configuration and messages reloaded successfully!";
    public String inCombat = "<red>You cannot execute LandClaim commands while in combat!</red>";
    
    public String adminChunkClaimed = "<green>Admin bypass: Chunk claimed successfully for the Admin Profile.";
    public String adminAlreadyClaimed = "<red>This chunk is already claimed by <owner>. Unclaim it first.";

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
    public String actionbarBanned = "<gradient:#8a0303:#ff416c>⛔ Banned from <owner>'s Claim ⛔</gradient>";

    @Comment("Variables: <player>, <count>, <limit>, <seconds>, <world>, <x>, <z>")

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
    public String unstuckNotInClaim = "<red>You are not inside a claim.";
    public String unstuckSearching = "<yellow>Searching for a safe wilderness location...";
    public String unstuckFailed = "<red>Could not find a safe wilderness location nearby.";

    // Missing messages from various managers and commands
    public String accessDeniedInteract = "<red>You do not have permission to interact here.";
    public String autoUnclaimed = "<green>Chunk automatically unclaimed because you broke the last block.";
    public String bucketDenied = "<red>You do not have permission to use buckets here.";
    public String harmEntityDenied = "<red>You do not have permission to harm entities here.";
    public String pvpDenied = "<red>PvP is disabled in this claim.";
    public String pvpEnabled = "<red><b>[!]</b> PvP has been ENABLED in this claim!";
    public String pvpEnabledTemp = "<red><b>[!]</b> PvP has been ENABLED in this claim for <time> seconds!";
    public String pvpDisabled = "<green><b>[!]</b> PvP has been DISABLED in this claim. You are safe again.";
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
    public String invalidVisualizationMode = "<red>Invalid visualization mode. Use: display_entities, particles, or off";
    public String visualizationModeChanged = "<green>Visualization mode set to <gold><mode></gold>.";

    // CommandHandler specifics
    public String memberInvited = "<green>You have invited <gold><player></gold> to your claim.";
    public String memberInviteReceived = "<green>You have been invited to <gold><owner></gold>'s claim!\n<gray>[<green><click:run_command:'/claim accept'><hover:show_text:'Click to accept the invite!'>Accept</hover></click></green>] [<red><click:run_command:'/claim deny'><hover:show_text:'Click to deny the invite'>Deny</hover></click></red>]</gray>";
    public String memberKicked = "<red>You have kicked <gold><player></gold> from the claim.";
    public String playerBanned = "<dark_red>Banned <gold><player></gold> from your claim. They can no longer enter.";
    public String playerUnbanned = "<green>Unbanned <gold><player></gold>. They may enter your claim again.";
    public String alreadyBanned = "<red><gold><player></gold> is already banned from this claim.";
    public String notBanned = "<red><gold><player></gold> is not banned from this claim.";
    public String cannotBanSelf = "<red>You cannot ban yourself from your own claim.";
    public String bannedFromClaim = "<dark_red>You have been banned from <gold><owner></gold>'s claim. Entry denied.";
    public String unbannedByOwner = "<green><gold><owner></gold> has unbanned you from their claim.";
    public String bannedTeleportedOut = "<dark_red>You were ejected from <gold><owner></gold>'s claim (you are banned).";
    public String bannedNoEscape = "<red>Could not find a safe location outside the claim. Try /claim unstuck.";
    public String banlistEmpty = "<gray>No players are currently banned from your claim.";
    public String banlistHeader = "<dark_red><bold>Banned players:</bold>";
    public String banlistEntry = "<dark_red>- <gold><player>";
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

    public String trustAdded = "<green>Added trust to <player>.\n<yellow>Open <gold>/claim<yellow> → <gold>Trust Management<yellow> and left-click <player>'s head to grant per-player flags (block break/place, etc.).";
    public String trustRemoved = "<red>Removed trust from <player>";
    public String claimVisibilityToggled = "<green>Claim visibility toggled.";
    public String adminBypassedUnclaim = "<green>Bypassed ownership and unclaimed chunk.";
    public String adminClaimInfoOwnedBy = "<red>[ADMIN] <green>Claim owned by: <gold><owner> <gray>(<uuid>)";
    public String adminClaimInfoId = "<green>Claim ID: <gray><id>";

    // --- Profile Lifecycle ---
    public String noProfile = "<red>You don't have a claim profile yet. Claim some land first!";
    public String cannotClaimAsMember = "<red>You cannot claim land while you are a member of another claim.";
    public String profileCreated = "<green>Your claim profile <gold><name></gold> has been created!";
    public String profileChanged = "<green>Your active claim profile has been changed to <gold><name></gold>.";
    public String profileLimitReached = "<red>You have reached your maximum profile limit of <limit>.";
    public String multiProfilesDisabled = "<red>The multi-profile system is currently disabled on this server.";
    public String alreadyHasProfile = "<red>You already have a claim profile.";
    public String profileAbandoned = "<red>All your claims have been abandoned.";
    public String unclaimAllConfirm = "<yellow>Are you sure? Type <click:run_command:'/claim unclaimall confirm'><gold>/claim unclaimall confirm</gold></click> to proceed.";

    // --- Trust / Member ---
    public String notAMember = "<red>That player is not a member of this claim.";
    public String notTrusted = "<red>That player is not trusted in this claim.";
    public String alreadyTrusted = "<red>That player is already trusted.";
    public String cannotTrustSelf = "<red>You cannot trust yourself.";
    public String targetAlreadyInClaim = "<red>That player is already in another claim.";
    public String youWereKicked = "<red>You have been kicked from the claim.";

    // --- Invite Flow ---
    public String inviteExpired = "<red>That invite has expired.";
    public String inviteReceived = "<green>You have been invited to join <owner>'s claim! Type <click:run_command:/claim accept>/claim accept</click> to join or <click:run_command:/claim deny>/claim deny</click> to decline.";
    public String inviteAccepted = "<green>You have accepted the invite!";
    public String inviteDenied = "<red>You have denied the invite.";
    public String inviteWasDenied = "<red>Your invite was denied.";
    public String memberJoined = "<green><player> has joined your claim!";
    
    // Trust Invites
    public String trustInvited = "<green>Invited <player> to be trusted in your claim.";
    public String trustInviteReceived = "<green>You have been invited to be trusted in <owner>'s claim! Type <click:run_command:/claim trust accept>/claim trust accept</click> to accept or <click:run_command:/claim trust deny>/claim trust deny</click> to decline.";
    public String noPendingTrustInvite = "<red>You have no pending trust invites.";
    public String trustInviteAccepted = "<green>You have accepted the trust invite!";
    public String trustInviteDenied = "<red>You have denied the trust invite.";
    public String trustInviteWasDenied = "<red>Your trust invite was denied.";
    // --- Leave Claim ---
    public String claimLeft = "<green>You have left <gold><name></gold>'s claim.";
    public String notInThatClaim = "<red>You are not a member or trusted player in that claim.";
    public String memberLeftClaim = "<yellow><player> has left your claim.";

    public String memberListHeader = "<gold>Members:";
    public String memberListEntry = "<gray>- <gold><player> <gray>(<role>)";
    public String memberListEmpty = "<gray>No members.";
    public String trustListHeader = "<gold>Trusted Players:";
    public String trustListEntry = "<gray>- <gold><player>";
    public String trustListEmpty = "<gray>No trusted players.";

    // --- Rename ---
    public String claimRenamed = "<green>Claim renamed to <gold><name></gold>.";
    public String claimNameInvalid = "<red>Invalid name. Use 3-32 characters (letters, numbers, spaces).";
    public String bannedClaimName = "<red>That name contains banned words. Please choose a different name.";
    public String renamePrompt = "<yellow>Type the new claim name in chat (or 'cancel' to abort):";
    public String renameCancelled = "<gray>Rename cancelled.";

    // --- Color ---
    public String claimColorChanged = "<green>Claim color changed!";
    public String claimColorInvalid = "<red>Invalid hex color. Use format: #RRGGBB";
    public String colorHexPrompt = "<yellow>Type a hex color code in chat (e.g. #FF5500, or 'cancel'):";

    // --- Visibility ---
    public String visibilityModeChanged = "<green>Visualization mode set to <gold><mode></gold>.";

    // --- Titles ---
    public String titleEnterPrompt = "<yellow>Type the new entry title in chat (or 'cancel' to abort):";
    public String titleLeavePrompt = "<yellow>Type the new leave title in chat (or 'cancel' to abort):";
    public String titleUpdated = "<green>Claim title successfully updated!";
    public String titleCancelled = "<gray>Title update cancelled.";

    // --- Geyser MC / Bedrock Support ---
    public String geyserChatPrompt = "<yellow>[Bedrock] Please type '<title>' in chat (or type 'cancel' to abort):";
    public String geyserChatCancelled = "<red>[Bedrock] Input cancelled.";
    public String geyserFormSent = "<yellow>[Bedrock] A form for '<title>' has been sent. Please check your screen.";
    public String geyserFormCancelled = "<red>[Bedrock] Form closed.";

    // --- Bedrock confirmation dialogs ---
    @Comment("Used when Bedrock players need to confirm a destructive action (e.g. abandoning a claim, unclaiming all).")
    public String geyserConfirmTitle = "<gold>Confirm Action";
    @Comment("Shown on the ModalForm when a Bedrock player attempts /claim abandon.")
    public String geyserAbandonContent = "<red>Are you sure you want to abandon your claim?\n<gray>This will release all of your claimed chunks.";
    @Comment("Shown on the ModalForm when a Bedrock player attempts /unclaim all or /claim unclaimall.")
    public String geyserUnclaimAllContent = "<red>Are you sure you want to unclaim ALL of your claimed chunks?\n<gray>This action cannot be undone.";
    @Comment("Shown on the ModalForm when a Bedrock player attempts to ban another player from their claim.")
    public String geyserBanContent = "<red>Ban <gold><player></gold> from your claim?\n<gray>They will no longer be able to enter.";
}
