package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

@Header("LandClaimPlugin - Messages Configuration")
@Header("MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html")
@Header("Palette: #00E262 green, #E20000 red, #E2B700 yellow, #A0A0A0 gray, #FFFFFF white, #1F1F1F GUI title")
public class MessagesConfig extends OkaeriConfig {

    @Comment("Chat prefix prepended to player and console notifications.")
    public String prefix = "<color:#A0A0A0>[<color:#00E262>LandClaim<color:#A0A0A0>] ";

    @Comment("Core permissions, combat checks, and plugin reload feedback.")
    public GeneralMessages general = new GeneralMessages();

    @Comment("Chunk claiming, unclaiming, radius, gaps, and boundary visibility.")
    public ClaimingMessages claiming = new ClaimingMessages();

    @Comment("Profile switching, renaming, owner alias, and claim color.")
    public ProfileMessages profile = new ProfileMessages();

    @Comment("Member invitations, kicks, leaving, and member listings.")
    public MemberMessages members = new MemberMessages();

    @Comment("Trusted builder invitations and per-player permission prompts.")
    public TrustMessages trust = new TrustMessages();

    @Comment("Inter-claim alliances and alliance requests.")
    public AllyMessages allies = new AllyMessages();

    @Comment("Claim ban and ejection notifications.")
    public BanMessages bans = new BanMessages();

    @Comment("Public and private claim warps.")
    public WarpMessages warps = new WarpMessages();

    @Comment("Per-claim protection alerts, PvP, TNT, mob spawning, and unstuck.")
    public ProtectionMessages protection = new ProtectionMessages();

    @Comment("Entry and exit title editor messages.")
    public TitleMessages titles = new TitleMessages();

    @Comment("Chat enter/leave territory messages and /claim notify.")
    public ChatNotificationMessages chatNotifications = new ChatNotificationMessages();

    @Comment("Action bar status text (no chat prefix applied).")
    public ActionbarMessages actionbar = new ActionbarMessages();

    @Comment("Held 128x128 territory radar map notifications.")
    public MinimapMessages minimap = new MinimapMessages();

    @Comment("Limit purchases and marketplace commands.")
    public EconomyMessages economy = new EconomyMessages();

    @Comment("Administrative bypass, chunk allowance, decay, and inspection messages.")
    public AdminMessages admin = new AdminMessages();

    @Comment("GUI titles, confirmation prompts, role labels, and editor feedback.")
    public GuiMessages gui = new GuiMessages();

    @Comment("Geyser / Bedrock fallback prompts and native form text.")
    public BedrockMessages bedrock = new BedrockMessages();

    @Comment("Modrinth update check notifications.")
    public UpdateMessages updates = new UpdateMessages();

    public static class GeneralMessages extends OkaeriConfig {
        public String accessDenied = "<color:#E20000>Permission denied.";
        public String noPermission = "<color:#E20000>Permission denied.";
        public String wildernessProtected = "<color:#E20000>Action blocked in wilderness. Claim land to build here.";
        public String playerNotFound = "<color:#E20000>Player not found.";
        public String playerNotOnline = "<color:#E20000>That player is offline.";
        public String invalidCommand = "<color:#E20000>Invalid command syntax.";
        public String inCombat = "<color:#E20000>You cannot use LandClaim commands while in combat.";
        public String reloaded = "<color:#00E262>Configuration and messages reloaded.";
    }

    public static class ClaimingMessages extends OkaeriConfig {
        public String chunkClaimed = "<color:#00E262>Claimed chunk. <color:#A0A0A0>(<color:#FFFFFF><chunks><color:#A0A0A0>/<color:#FFFFFF><limit><color:#A0A0A0>)";
        public String chunkUnclaimed = "<color:#E2B700>Unclaimed chunk. <color:#A0A0A0>(<color:#FFFFFF><chunks><color:#A0A0A0>/<color:#FFFFFF><limit><color:#A0A0A0>)";
        public String claimFailed = "<color:#E20000>Could not claim this chunk.";
        public String claimRadiusSuccess = "<color:#00E262>Claimed <color:#FFFFFF><count> <color:#00E262>chunks <color:#A0A0A0>(radius <color:#FFFFFF><radius><color:#A0A0A0>, <color:#FFFFFF><chunks><color:#A0A0A0>/<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String unclaimRadiusSuccess = "<color:#E2B700>Unclaimed <color:#FFFFFF><count> <color:#E2B700>chunks <color:#A0A0A0>(radius <color:#FFFFFF><radius><color:#A0A0A0>, <color:#FFFFFF><chunks><color:#A0A0A0>/<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String invalidRadius = "<color:#E20000>Radius must be between <color:#FFFFFF>1 <color:#E20000>and <color:#FFFFFF>5<color:#E20000>.";
        public String worldBlocked = "<color:#E20000>Claiming is disabled in this world.";
        public String alreadyClaimed = "<color:#E20000>Chunk claimed by <color:#FFFFFF><owner><color:#E20000>.";
        public String claimLimitReached = "<color:#E20000>Claim limit reached <color:#A0A0A0>(<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String notConnected = "<color:#E20000>New claims must touch your existing territory.";
        public String tooCloseToWorldguard = "<color:#E20000>Too close to a WorldGuard region <color:#A0A0A0>(min gap <color:#FFFFFF><gap><color:#A0A0A0>).";
        public String tooCloseToOtherClaim = "<color:#E20000>Too close to another claim <color:#A0A0A0>(min gap <color:#FFFFFF><gap><color:#A0A0A0>).";
        public String notInClaim = "<color:#E20000>You are not inside a claim.";
        public String notOwner = "<color:#E20000>You do not own this claim.";
        public String autoClaimEnabled = "<color:#00E262>Auto-claim enabled.";
        public String autoClaimDisabled = "<color:#A0A0A0>Auto-claim disabled.";
        public String autoUnclaimEnabled = "<color:#00E262>Auto-unclaim enabled.";
        public String autoUnclaimDisabled = "<color:#A0A0A0>Auto-unclaim disabled.";
        public String invalidVisualizationMode = "<color:#E20000>Invalid mode. Use <color:#FFFFFF>display_entities<color:#A0A0A0>, <color:#FFFFFF>particles<color:#A0A0A0>, <color:#E20000>or <color:#FFFFFF>off<color:#E20000>.";
        public String visualizationModeChanged = "<color:#00E262>Visualization set to <color:#FFFFFF><mode><color:#00E262>.";
        public String claimVisibilityToggled = "<color:#00E262>Claim visibility toggled.";
        public String claimInfoOwner = "<color:#A0A0A0>Owner <color:#00E262><owner>";
    }

    public static class ProfileMessages extends OkaeriConfig {
        public String noProfile = "<color:#E20000>You do not have a claim profile yet.";
        public String noProfileFound = "<color:#E20000>That player has no claim profile.";
        public String notOwnedByName = "<color:#E20000>No owned claim profile found named <color:#FFFFFF><name><color:#E20000>.";
        public String cannotClaimAsMember = "<color:#E20000>You cannot create a claim while belonging to another claim.";
        public String profileCreated = "<color:#00E262>Created claim profile <color:#FFFFFF><name><color:#00E262>.";
        public String profileChanged = "<color:#00E262>Active profile switched to <color:#FFFFFF><name><color:#00E262>.";
        public String profileLimitReached = "<color:#E20000>Profile limit reached <color:#A0A0A0>(<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String multiProfilesDisabled = "<color:#E20000>Multi-profile mode is disabled.";
        public String alreadyHasProfile = "<color:#E20000>You already own a claim profile.";
        public String profileAbandoned = "<color:#E2B700>Abandoned claim profile <color:#A0A0A0>(<color:#FFFFFF><chunks> chunks freed<color:#A0A0A0>).";
        public String unclaimAllConfirm = "<color:#E2B700>Confirm unclaiming all chunks <color:#A0A0A0>· <click:run_command:'/unclaim all confirm'><hover:show_text:'<color:#A0A0A0>Click to release all chunks'><color:#00E262><u>[/unclaim all confirm]</u></hover></click>";
        public String claimRenamed = "<color:#00E262>Renamed claim to <color:#FFFFFF><name><color:#00E262>.";
        public String claimNameInvalid = "<color:#E20000>Use 3-32 letters, numbers, or spaces.";
        public String bannedClaimName = "<color:#E20000>That name contains a restricted word.";
        public String nameAlreadyInUse = "<color:#E20000>That claim name is already taken.";
        public String ownerAliasReset = "<color:#00E262>Owner alias reset.";
        public String ownerAliasSet = "<color:#00E262>Owner alias set to <color:#FFFFFF><alias><color:#00E262>.";
        public String claimColorChanged = "<color:#00E262>Claim color updated.";
        public String claimColorInvalid = "<color:#E20000>Invalid hex color. Example <color:#FFFFFF>#00E262";
    }

    public static class MemberMessages extends OkaeriConfig {
        public String cannotInviteSelf = "<color:#E20000>You cannot invite yourself.";
        public String alreadyMember = "<color:#E20000>That player is already a member.";
        public String alreadyInClaim = "<color:#E20000>That player is already in this claim.";
        public String targetAlreadyInClaim = "<color:#E20000><player> already belongs to another claim.";
        public String limitReached = "<color:#E20000>Member limit reached <color:#A0A0A0>(<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String membershipLimitReached = "<color:#E20000>You cannot join more than <color:#FFFFFF><limit> <color:#E20000>claims.";
        public String memberInvited = "<color:#00E262>Invited <player> to your claim.";
        public String memberKicked = "<color:#E20000>Removed <color:#FFFFFF><player> <color:#E20000>from the claim.";
        public String youWereKicked = "<color:#E20000>You were removed from <color:#FFFFFF><owner><color:#E20000>'s claim.";
        public String notAMember = "<color:#E20000><player> is not a member of this claim.";
        public String inviteExpired = "<color:#E20000>That invitation has expired.";
        public String inviteReceived = "<color:#A0A0A0>Invite from <color:#00E262><owner> <color:#A0A0A0>· <click:run_command:'/claim accept'><hover:show_text:'<color:#00E262>Join claim'><color:#00E262><u>[Accept]</u></hover></click> <click:run_command:'/claim deny'><hover:show_text:'<color:#E20000>Decline invite'><color:#E20000><u>[Decline]</u></hover></click>";
        public String inviteAccepted = "<color:#00E262>You have joined <owner>'s claim.";
        public String inviteDenied = "<color:#A0A0A0>Invitation declined.";
        public String inviteWasDenied = "<color:#E2B700><player> declined your claim invite.";
        public String noPendingInvite = "<color:#E20000>You have no pending invitations.";
        public String memberJoined = "<color:#00E262><player> joined your claim.";
        public String claimLeft = "<color:#E2B700>You left <color:#FFFFFF><name><color:#E2B700>.";
        public String notInThatClaim = "<color:#E20000>You are not listed in that claim.";
        public String memberLeftClaim = "<color:#E2B700><player> left your claim.";
        public String listHeader = "<color:#E2B700>Members";
        public String listEntry = "<color:#A0A0A0>- <color:#00E262><player> <color:#A0A0A0>(<color:#FFFFFF><role><color:#A0A0A0>)";
        public String listEmpty = "<color:#A0A0A0>No members listed.";
    }

    public static class TrustMessages extends OkaeriConfig {
        public String cannotTrustSelf = "<color:#E20000>You cannot trust yourself.";
        public String alreadyTrusted = "<color:#E20000>That player is already trusted.";
        public String notTrusted = "<color:#E20000>That player is not trusted in this claim.";
        public String trustInvited = "<color:#00E262>Sent trust invite to <player>.";
        public String trustInviteReceived = "<color:#A0A0A0>Trust invite from <color:#00E262><owner> <color:#A0A0A0>· <click:run_command:'/claim trust accept'><hover:show_text:'<color:#00E262>Accept trust'><color:#00E262><u>[Accept]</u></hover></click> <click:run_command:'/claim trust deny'><hover:show_text:'<color:#E20000>Decline trust'><color:#E20000><u>[Decline]</u></hover></click>";
        public String trustInviteAccepted = "<color:#00E262>Trust invitation accepted.";
        public String trustInviteDenied = "<color:#A0A0A0>Trust invitation declined.";
        public String trustInviteWasDenied = "<color:#E2B700><player> declined your trust invite.";
        public String noPendingTrustInvite = "<color:#E20000>You have no pending trust invitations.";
        public String trustAdded = "<color:#00E262>Added trust for <player>. <color:#A0A0A0>Edit flags in <click:run_command:'/claim menu trusted'><hover:show_text:'<color:#A0A0A0>Open Trust Management'><color:#00E262><u>/claim menu trusted</u></hover></click><color:#A0A0A0>.";
        public String trustRemoved = "<color:#E20000>Removed trust for <color:#FFFFFF><player><color:#E20000>.";
        public String listHeader = "<color:#E2B700>Trusted players";
        public String listEntry = "<color:#A0A0A0>- <color:#00E262><player>";
        public String listEmpty = "<color:#A0A0A0>No trusted players listed.";
    }

    public static class AllyMessages extends OkaeriConfig {
        public String claimNotFound = "<color:#E20000>Claim not found.";
        public String cannotAllySelf = "<color:#E20000>You cannot ally your own claim.";
        public String alreadyAllied = "<color:#E20000>Already allied with that claim.";
        public String notAllied = "<color:#E20000>Not allied with that claim.";
        public String allyInviteSent = "<color:#00E262>Sent alliance invite to <color:#FFFFFF><name><color:#00E262>.";
        public String allyInviteReceived = "<color:#A0A0A0>Alliance request from <color:#00E262><name> <color:#A0A0A0>· <click:run_command:'/claim ally accept <name>'><hover:show_text:'<color:#00E262>Accept alliance'><color:#00E262><u>[Accept]</u></hover></click> <click:run_command:'/claim ally deny <name>'><hover:show_text:'<color:#E20000>Decline alliance'><color:#E20000><u>[Decline]</u></hover></click>";
        public String noAllyInviteFrom = "<color:#E20000>No pending alliance invite from <color:#FFFFFF><name><color:#E20000>.";
        public String specifyAllyName = "<color:#E20000>Specify claim name <color:#A0A0A0>(<color:#FFFFFF>/claim ally accept <name><color:#A0A0A0>).";
        public String allyAccepted = "<color:#00E262>Allied with <color:#FFFFFF><name><color:#00E262>.";
        public String allyAcceptedTarget = "<color:#FFFFFF><name> <color:#00E262>accepted your alliance request.";
        public String allyDenied = "<color:#A0A0A0>Declined alliance request from <color:#FFFFFF><name><color:#A0A0A0>.";
        public String allyRemoved = "<color:#E2B700>Ended alliance with <color:#FFFFFF><name><color:#E2B700>.";
    }

    public static class BanMessages extends OkaeriConfig {
        public String cannotBanSelf = "<color:#E20000>You cannot ban yourself.";
        public String alreadyBanned = "<color:#E20000><player> is already banned from this claim.";
        public String notBanned = "<color:#E20000><player> is not banned from this claim.";
        public String playerBanned = "<color:#E20000>You have banned <color:#FFFFFF><player> <color:#E20000>from this claim.";
        public String bannedFromClaim = "<color:#E20000><b>⚠ ENTRY DENIED</b> <color:#A0A0A0>· You are banned from <color:#FFFFFF><owner><color:#A0A0A0>'s claim.";
        public String playerUnbanned = "<color:#00E262>You have unbanned <player>.";
        public String unbannedByOwner = "<color:#00E262><owner> unbanned you from their claim.";
        public String bannedTeleportedOut = "<color:#E20000>Ejected from <color:#FFFFFF><owner><color:#E20000>'s claim.";
        public String bannedNoEscape = "<color:#E20000>No safe exit found. Run <color:#FFFFFF>/claim unstuck<color:#E20000>.";
        public String listEmpty = "<color:#A0A0A0>No banned players.";
        public String listHeader = "<color:#E2B700>Banned players";
        public String listEntry = "<color:#A0A0A0>- <color:#FFFFFF><player>";
    }

    public static class WarpMessages extends OkaeriConfig {
        public String notInOwnClaim = "<color:#E20000>Stand inside your own claim to set a warp.";
        public String warpSet = "<color:#00E262>Warp <color:#FFFFFF><name> <color:#00E262>saved.";
        public String warpDeleted = "<color:#E2B700>Deleted warp <color:#FFFFFF><name><color:#E2B700>.";
        public String warpTeleport = "<color:#00E262>Teleported to <color:#FFFFFF><name><color:#00E262>.";
        public String warpNotFound = "<color:#E20000>Warp <color:#FFFFFF><name> <color:#E20000>not found.";
        public String warpLimitReached = "<color:#E20000>Warp limit reached.";
        public String warpLocationUpdated = "<color:#00E262>Updated location for <color:#FFFFFF><name><color:#00E262>.";
        public String warpIconUpdated = "<color:#00E262>Updated icon for <color:#FFFFFF><name><color:#00E262>.";
        public String warpMadePublic = "<color:#00E262>Warp <color:#FFFFFF><name> <color:#00E262>is now public <color:#A0A0A0>(<color:#FFFFFF>/claim publicwarps<color:#A0A0A0>).";
        public String warpMadePrivate = "<color:#E2B700>Warp <color:#FFFFFF><name> <color:#E2B700>is now private.";
        public String publicwarpsNoWarps = "<color:#A0A0A0>No public warps available.";
        public String publicwarpsTeleported = "<color:#00E262>Teleported to <owner><color:#A0A0A0>:<color:#FFFFFF><name><color:#00E262>.";
    }

    public static class ProtectionMessages extends OkaeriConfig {
        public String bucketDenied = "<color:#E20000>Bucket use is denied in this claim.";
        public String commandBlocked = "<color:#E20000>That command is blocked inside this claim.";
        public String visitorSettingsLocked = "<color:#E20000>Visitor settings are locked by server configuration.";
        public String pvpDenied = "<color:#E20000>PvP is disabled in this claim.";
        public String pvpEnabled = "<color:#E20000><b>⚠ PVP ENABLED</b> <color:#A0A0A0>inside this claim.";
        public String pvpEnabledTemp = "<color:#E20000><b>⚠ PVP ENABLED</b> <color:#A0A0A0>for <color:#FFFFFF><time>s<color:#A0A0A0>.";
        public String pvpDisabled = "<color:#00E262>PvP is now disabled inside this claim.";
        public String pvpForceLocked = "<color:#E20000>PvP is forced on by server configuration.";
        public String tntEnabled = "<color:#E2B700>TNT explosions <color:#FFFFFF>enabled <color:#E2B700>in this claim.";
        public String tntDisabled = "<color:#00E262>TNT explosions <color:#FFFFFF>blocked <color:#00E262>in this claim.";
        public String mobSpawningEnabled = "<color:#00E262>Mob spawning <color:#FFFFFF>enabled <color:#00E262>in this claim.";
        public String mobSpawningDisabled = "<color:#E2B700>Mob spawning <color:#FFFFFF>disabled <color:#E2B700>in this claim.";
        public String unstuckCooldown = "<color:#E20000>Wait <color:#FFFFFF><seconds>s <color:#E20000>before running unstuck again.";
        public String unstuckSuccess = "<color:#00E262>Moved to nearest safe wilderness.";
        public String unstuckNotInClaim = "<color:#E20000>You are not inside a claim.";
        public String unstuckSearching = "<color:#A0A0A0>Finding safe wilderness...";
        public String unstuckFailed = "<color:#E20000>No safe wilderness block found nearby.";
    }

    public static class TitleMessages extends OkaeriConfig {
        public String titleUpdated = "<color:#00E262>Claim title updated.";
        public String titleCancelled = "<color:#A0A0A0>Title update cancelled.";
    }

    public static class ChatNotificationMessages extends OkaeriConfig {
        public String claimEnterChat = "<color:#A0A0A0>Entered <color:#00E262><claim> <color:#A0A0A0>(<color:#FFFFFF><owner><color:#A0A0A0>)";
        public String claimLeaveChat = "<color:#A0A0A0>Left <color:#FFFFFF><claim>";
        public String wildernessEnterChat = "";
        public String claimNotifyEnabled = "<color:#00E262>Territory chat notifications enabled.";
        public String claimNotifyDisabled = "<color:#A0A0A0>Territory chat notifications disabled.";
    }

    public static class ActionbarMessages extends OkaeriConfig {
        public String actionbarWilderness = "<color:#A0A0A0>Wilderness";
        public String actionbarOwnedByYou = "<color:#00E262>★ Your Claim <color:#A0A0A0>· <color:#FFFFFF><claim>";
        public String actionbarOwnedByOther = "<color:#E20000><owner>'s Claim <color:#A0A0A0>· <color:#FFFFFF><claim>";
        public String actionbarTrusted = "<color:#00E262><owner>'s Claim <color:#A0A0A0>· <color:#FFFFFF>Trusted";
        public String actionbarAdmin = "<color:#E2B700><owner>'s Claim <color:#A0A0A0>· <color:#FFFFFF>Admin";
        public String actionbarBanned = "<color:#E20000><b>⚠ BANNED</b> <color:#A0A0A0>· <color:#FFFFFF><owner>";
    }

    public static class MinimapMessages extends OkaeriConfig {
        public String alreadyOwned = "<color:#E2B700>You already hold a Territory Map.";
        public String received = "<color:#00E262>Received Territory Map. <color:#A0A0A0>Hold to view nearby chunks.";
        public String zoomChanged = "<color:#A0A0A0>Map zoom <color:#00E262><mode>";
        public String zoomOverview = "16x16 Overview";
        public String zoomClose = "8x8 Detail";
    }

    public static class EconomyMessages extends OkaeriConfig {
        public String disabled = "<color:#E20000>Economy addon is not enabled.";
        public String invalidAmount = "<color:#E20000>Amount must be greater than 0.";
        public String insufficientFunds = "<color:#E20000>Insufficient funds <color:#A0A0A0>(Cost <color:#FFFFFF><cost><color:#A0A0A0>, Balance <color:#FFFFFF><balance><color:#A0A0A0>).";
        public String transactionFailed = "<color:#E20000>Transaction failed.";
        public String purchaseError = "<color:#E20000>Failed to complete purchase.";
        public String limitPurchasesDisabled = "<color:#E20000>Purchasing extra limits is disabled.";
        public String maxBonusClaimBlocksReached = "<color:#E20000>Bonus cap is <color:#FFFFFF><max><color:#E20000>. Remaining <color:#FFFFFF><remaining><color:#E20000>.";
        public String maxTotalClaimLimitReached = "<color:#E20000>Total claim cap is <color:#FFFFFF><max><color:#E20000>. Remaining <color:#FFFFFF><remaining><color:#E20000>.";
        public String boughtClaimBlocks = "<color:#00E262>Purchased <color:#FFFFFF><amount> <color:#00E262>claim blocks <color:#A0A0A0>(Limit <color:#FFFFFF><limit><color:#A0A0A0>).";
        public String boughtRoleSlot = "<color:#00E262>Purchased custom role slot <color:#A0A0A0>(Bonus <color:#FFFFFF><total><color:#A0A0A0>).";
        public String boughtMemberSlot = "<color:#00E262>Purchased member slot <color:#A0A0A0>(Bonus <color:#FFFFFF><total><color:#A0A0A0>).";
        public String boughtWarpSlot = "<color:#00E262>Purchased warp slot <color:#A0A0A0>(Bonus <color:#FFFFFF><total><color:#A0A0A0>).";
        public String sellCancelled = "<color:#A0A0A0>Marketplace listing cancelled.";
        public String sellError = "<color:#E20000>Failed to list claim profile.";
    }

    public static class AdminMessages extends OkaeriConfig {
        public String adminChunkClaimed = "<color:#00E262>Claimed chunk for Server Land.";
        public String adminAlreadyClaimed = "<color:#E20000>Chunk claimed by <color:#FFFFFF><owner><color:#E20000>. Unclaim it first.";
        public String adminBypassedUnclaim = "<color:#00E262>Unclaimed chunk via admin bypass.";
        public String adminClaimInfoOwnedBy = "<color:#A0A0A0>Owner <color:#00E262><owner> <color:#A0A0A0>(<color:#FFFFFF><uuid><color:#A0A0A0>)";
        public String adminClaimInfoId = "<color:#A0A0A0>Profile <color:#FFFFFF><id>";
        public String adminEditingProfile = "<color:#00E262>Editing claim profile for <player>.";
        public String adminAddChunkSuccess = "<color:#00E262>Added <color:#FFFFFF><amount> <color:#00E262>bonus chunks to <player>.";
        public String adminSetChunkSuccess = "<color:#00E262>Set bonus chunks for <player> to <color:#FFFFFF><amount> <color:#A0A0A0>(Limit <color:#FFFFFF><limit><color:#A0A0A0>).";
        public String adminRemoveChunkSuccess = "<color:#E2B700>Removed <color:#FFFFFF><amount> <color:#E2B700>bonus chunks from <color:#FFFFFF><player> <color:#A0A0A0>(Limit <color:#FFFFFF><limit><color:#A0A0A0>).";
        public String adminResetChunkSuccess = "<color:#E2B700>Reset bonus chunks for <color:#FFFFFF><player> <color:#A0A0A0>(Limit <color:#FFFFFF><limit><color:#A0A0A0>).";
        public String trustListHeader = "<color:#E2B700>Trusted players for <color:#FFFFFF><player>";
        public String trustListEntry = "<color:#A0A0A0>- <color:#00E262><player>";
        public String trustWhoHeader = "<color:#E2B700>Claims trusting <color:#FFFFFF><player>";
        public String trustWhoEntry = "<color:#A0A0A0>- <color:#FFFFFF><claim>";
        public String trustWhoEmpty = "<color:#A0A0A0>Player is not trusted in any loaded claim.";
        public String adminDecayRunStarted = "<color:#A0A0A0>Running inactive claim decay check...";
        public String adminDecayRunFinished = "<color:#00E262>Decay complete <color:#A0A0A0>(<color:#FFFFFF><claims> claims<color:#A0A0A0>, <color:#FFFFFF><chunks> chunks freed<color:#A0A0A0>).";
        public String decayAlreadyRunning = "<color:#E20000>A decay scan is already running.";
        public String adminDecayExemptSet = "<color:#00E262>Decay exemption for <player><color:#A0A0A0>: <color:#FFFFFF><status>";
        public String decayStatusExempt = "EXEMPT";
        public String decayStatusNotExempt = "NOT EXEMPT";
        public String decayListEmpty = "<color:#A0A0A0>No players are exempt from decay.";
        public String decayListHeader = "<color:#E2B700>Decay-exempt players <color:#A0A0A0>(<color:#FFFFFF><count><color:#A0A0A0>)";
        public String decayListEntry = "<color:#A0A0A0>- <color:#00E262><player> <color:#A0A0A0>(<uuid>)";
        public String decayListError = "<color:#E20000>Failed to retrieve exempt players.";
        public String aliasReset = "<color:#00E262>Reset owner alias for <color:#FFFFFF><claim><color:#00E262>.";
        public String aliasSet = "<color:#00E262>Set owner alias for <color:#FFFFFF><claim> <color:#00E262>to <color:#FFFFFF><alias><color:#00E262>.";
    }

    public static class GuiMessages extends OkaeriConfig {
        public String anvilSave = "<color:#00E262>Save";
        public String renameCancelled = "<color:#A0A0A0>Input cancelled.";
        public String defaultUnnamedClaim = "Unnamed Claim";
        public String defaultUnknownOwner = "Unknown";
        public String promptRenameClaim = "Rename Claim";
        public String promptOwnerAlias = "Set Owner Alias";
        public String promptHexColor = "Hex Color";
        public String promptEnterTitle = "Enter Title";
        public String promptLeaveTitle = "Leave Title";
        public String promptRoleName = "Role Name";
        public String promptRolePriority = "Priority";

        public String confirmYesName = "<color:#00E262>Confirm";
        public String confirmYesLore = "<color:#A0A0A0>Click to confirm";
        public String confirmNoName = "<color:#E20000>Cancel";
        public String confirmNoLore = "<color:#A0A0A0>Click to go back";
        public String confirmSellActive = "<color:#1F1F1F>Sell Active Profile?";
        public String confirmAbandonAll = "<color:#1F1F1F>Abandon ALL claims?";
        public String confirmResetClaimName = "<color:#1F1F1F>Reset claim name?";
        public String confirmTransfer = "<color:#1F1F1F>Transfer to <player>?";
        public String confirmKick = "<color:#1F1F1F>Kick <player>?";
        public String confirmBan = "<color:#1F1F1F>Ban <player>?";
        public String confirmRemoveTrust = "<color:#1F1F1F>Remove <player>?";
        public String confirmRemoveAlly = "<color:#1F1F1F>Remove Alliance?";
        public String confirmDeleteRole = "<color:#1F1F1F>Delete role <role>?";

        public String onlyOwnerTransfer = "<color:#E20000>Only the claim owner can transfer ownership.";
        public String ownershipTransferred = "<color:#00E262>Transferred ownership to <player>.";
        public String cannotDeleteDefaultRole = "<color:#E20000>Default roles cannot be deleted.";
        public String roleDeleted = "<color:#E2B700>Deleted role <color:#FFFFFF><role><color:#E2B700>.";
        public String roleLimitReached = "<color:#E20000>Custom role limit reached <color:#A0A0A0>(<color:#FFFFFF><limit><color:#A0A0A0>).";
        public String roleAssignedPlayer = "<color:#00E262>Changed <player>'s role to <color:#FFFFFF><role><color:#00E262>.";
        public String operationCancelled = "<color:#A0A0A0>Operation cancelled.";
        public String roleNameUpdated = "<color:#00E262>Role name updated.";
        public String roleNameInvalid = "<color:#E20000>Invalid role name.";
        public String rolePriorityUpdated = "<color:#00E262>Role priority updated.";
        public String rolePriorityInvalid = "<color:#E20000>Priority must be a number.";
        public String roleSaved = "<color:#00E262>Role saved.";

        public String defaultRoleBadge = "<color:#A0A0A0>Default Role";
        public String clickManage = "<color:#E2B700>Click to manage";
        public String clickSelect = "<color:#A0A0A0>Click to select";
        public String skullItemName = "<i:false><color:#00E262><name>";
        public String roleDefaultName = "<i:false><color:#00E262><role>";
        public String roleCustomName = "<i:false><color:#FFFFFF><role>";
        public String warpItemName = "<i:false><color:#00E262><name>";
        public String warpManageHint = "<color:#E2B700>Right-click <color:#A0A0A0>to manage";
        public String warpIconName = "<i:false><color:#FFFFFF><icon>";
        public String warpIconHint = "<color:#A0A0A0>Click to select this icon";
        public String clickAssignRole = "<color:#00E262>Click to assign to <player>";
        public String clickEditPermissions = "<color:#A0A0A0>Left-click <color:#FFFFFF>Edit permissions";
        public String clickEditRole = "<color:#A0A0A0>Left-click <color:#FFFFFF>Edit role";
        public String clickDeleteRole = "<color:#A0A0A0>Right-click <color:#E20000>Delete role";
        public String clickDeleteRoleDisabled = "<color:#A0A0A0><st>Right-click Delete role</st>";
        public String clickRemove = "<color:#A0A0A0>Right-click <color:#E20000>Remove";
        public String allyOwnerLine = "<color:#A0A0A0>Owner <color:#FFFFFF><owner>";
        public String memberRoleLine = "<color:#A0A0A0>Role <color:#FFFFFF><role>";

        public String titleStatusLabel = "<color:#A0A0A0>Status <value>";
        public String statusEnabled = "<color:#00E262>● Enabled";
        public String statusDisabled = "<color:#E20000>○ Disabled";
        public String titleCurrentText = "<color:#A0A0A0>Text <reset><text>";
        public String titleModeLabel = "<color:#A0A0A0>Mode <value>";
        public String titleModeTitle = "<color:#FFFFFF>Title";
        public String titleModeSubtitle = "<color:#00E262>Subtitle";
        public String titleLeftClickEdit = "<color:#A0A0A0>Left-click <color:#FFFFFF>Edit text";
        public String titleRightClickMode = "<color:#A0A0A0>Right-click <color:#FFFFFF>Toggle Title/Subtitle";

        public String warpSelectTitle = "<color:#1F1F1F>Select Warp <name>";
        public String warpItemDisplay = "<color:#00E262><name> <color:#A0A0A0>(<owner>)";
        public String warpOwnerLine = "<color:#A0A0A0>Owner <color:#FFFFFF><owner>";
        public String warpClaimLine = "<color:#A0A0A0>Claim <color:#FFFFFF><claim>";
        public String warpLocationLine = "<color:#A0A0A0>Location <color:#FFFFFF><location>";
        public String warpTeleportHint = "<color:#00E262>Left-click to teleport";
        public String warpCloseName = "<color:#E20000>Close";
        public String warpCloseLore = "<color:#A0A0A0>Click to close";
        public String warpPrevName = "<color:#00E262>← Previous Page";
        public String warpNextName = "<color:#00E262>Next Page →";
    }

    public static class BedrockMessages extends OkaeriConfig {
        public String geyserChatPrompt = "<color:#E2B700>[Bedrock] Type '<title>' in chat <color:#A0A0A0>(or 'cancel')<color:#E2B700>:";
        public String geyserChatCancelled = "<color:#A0A0A0>[Bedrock] Input cancelled.";
        public String geyserFormSent = "<color:#A0A0A0>[Bedrock] Sent form '<title>'.";
        public String geyserFormCancelled = "<color:#A0A0A0>[Bedrock] Form closed.";
        public String geyserConfirmTitle = "Confirm Action";
        public String geyserConfirmButton = "Confirm";
        public String geyserCancelButton = "Cancel";
        public String geyserAbandonContent = "Abandon your claim? All claimed chunks will be released.";
        public String geyserUnclaimAllContent = "Unclaim ALL chunks in this profile? This cannot be undone.";
        public String geyserBanContent = "Ban <player> from your claim? They will be blocked from entering.";
    }

    public static class UpdateMessages extends OkaeriConfig {
        public String available = "<color:#A0A0A0>[<color:#00E262>LandClaim<color:#A0A0A0>] <color:#E2B700>Update <color:#00E262><latest> <color:#E2B700>available <color:#A0A0A0>(current <color:#FFFFFF><current><color:#A0A0A0>) · <click:open_url:'<url>'><hover:show_text:'<color:#A0A0A0>Open Modrinth release'><color:#00E262><u>[Download]</u></hover></click>";
    }
}
