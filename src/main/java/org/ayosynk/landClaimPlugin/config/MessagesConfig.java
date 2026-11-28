package org.ayosynk.landClaimPlugin.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;

@Header("LandClaimPlugin - Messages Configuration")
@Header("MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html")
@Header("Palette roles: #38BDF8 accent, #34D399 confirm, #F87171 danger, #FBBF24 warn, #60A5FA value, #9AA5B4 label, #F1F5F9 detail, #1E293B GUI title")
public class MessagesConfig extends OkaeriConfig {

    @Comment("Chat prefix prepended to all player and console notifications.")
    public String prefix = "<color:#9AA5B4>[<color:#38BDF8>LandClaim<color:#9AA5B4>] ";

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
        public String accessDenied = "<color:#F87171>Permission denied.";
        public String noPermission = "<color:#F87171>Permission denied.";
        public String wildernessProtected = "<color:#F87171>Action blocked in wilderness. Claim land to build here.";
        public String playerNotFound = "<color:#F87171>Player not found.";
        public String playerNotOnline = "<color:#F87171>That player is offline.";
        public String invalidCommand = "<color:#F87171>Invalid command syntax.";
        public String inCombat = "<color:#F87171>You cannot use LandClaim commands while in combat.";
        public String reloaded = "<color:#34D399>Configuration and messages reloaded.";
    }

    public static class ClaimingMessages extends OkaeriConfig {
        public String chunkClaimed = "<color:#34D399>Claimed chunk. <color:#9AA5B4>(<color:#F1F5F9><chunks><color:#9AA5B4>/<color:#F1F5F9><limit><color:#9AA5B4>)";
        public String chunkUnclaimed = "<color:#FBBF24>Unclaimed chunk. <color:#9AA5B4>(<color:#F1F5F9><chunks><color:#9AA5B4>/<color:#F1F5F9><limit><color:#9AA5B4>)";
        public String claimFailed = "<color:#F87171>Could not claim this chunk.";
        public String claimRadiusSuccess = "<color:#34D399>Claimed <color:#60A5FA><count> <color:#34D399>chunks <color:#9AA5B4>(radius <color:#F1F5F9><radius><color:#9AA5B4>, <color:#F1F5F9><chunks><color:#9AA5B4>/<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String unclaimRadiusSuccess = "<color:#FBBF24>Unclaimed <color:#60A5FA><count> <color:#FBBF24>chunks <color:#9AA5B4>(radius <color:#F1F5F9><radius><color:#9AA5B4>, <color:#F1F5F9><chunks><color:#9AA5B4>/<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String invalidRadius = "<color:#F87171>Radius must be between <color:#F1F5F9>1 <color:#F87171>and <color:#F1F5F9>5<color:#F87171>.";
        public String worldBlocked = "<color:#F87171>Claiming is disabled in this world.";
        public String alreadyClaimed = "<color:#F87171>Chunk claimed by <color:#F1F5F9><owner><color:#F87171>.";
        public String claimLimitReached = "<color:#F87171>Claim limit reached <color:#9AA5B4>(<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String notConnected = "<color:#F87171>New claims must touch your existing territory.";
        public String tooCloseToWorldguard = "<color:#F87171>Too close to a WorldGuard region <color:#9AA5B4>(min gap <color:#F1F5F9><gap><color:#9AA5B4>).";
        public String tooCloseToOtherClaim = "<color:#F87171>Too close to another claim <color:#9AA5B4>(min gap <color:#F1F5F9><gap><color:#9AA5B4>).";
        public String notInClaim = "<color:#F87171>You are not inside a claim.";
        public String notOwner = "<color:#F87171>You do not own this claim.";
        public String autoClaimEnabled = "<color:#34D399>Auto-claim <color:#F1F5F9>enabled<color:#34D399>.";
        public String autoClaimDisabled = "<color:#9AA5B4>Auto-claim <color:#F1F5F9>disabled<color:#9AA5B4>.";
        public String autoUnclaimEnabled = "<color:#34D399>Auto-unclaim <color:#F1F5F9>enabled<color:#34D399>.";
        public String autoUnclaimDisabled = "<color:#9AA5B4>Auto-unclaim <color:#F1F5F9>disabled<color:#9AA5B4>.";
        public String invalidVisualizationMode = "<color:#F87171>Invalid mode. Use <color:#F1F5F9>display_entities<color:#9AA5B4>, <color:#F1F5F9>particles<color:#9AA5B4>, <color:#F87171>or <color:#F1F5F9>off<color:#F87171>.";
        public String visualizationModeChanged = "<color:#34D399>Visualization set to <color:#60A5FA><mode><color:#34D399>.";
        public String claimVisibilityToggled = "<color:#34D399>Claim visibility toggled.";
        public String claimInfoOwner = "<color:#9AA5B4>Owner <color:#60A5FA><owner>";
    }

    public static class ProfileMessages extends OkaeriConfig {
        public String noProfile = "<color:#F87171>You do not have a claim profile yet.";
        public String noProfileFound = "<color:#F87171>That player has no claim profile.";
        public String notOwnedByName = "<color:#F87171>No owned claim profile found named <color:#F1F5F9><name><color:#F87171>.";
        public String cannotClaimAsMember = "<color:#F87171>You cannot create a claim while belonging to another claim.";
        public String profileCreated = "<color:#34D399>Created claim profile <color:#60A5FA><name><color:#34D399>.";
        public String profileChanged = "<color:#34D399>Active profile switched to <color:#60A5FA><name><color:#34D399>.";
        public String profileLimitReached = "<color:#F87171>Profile limit reached <color:#9AA5B4>(<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String multiProfilesDisabled = "<color:#F87171>Multi-profile mode is disabled.";
        public String alreadyHasProfile = "<color:#F87171>You already own a claim profile.";
        public String profileAbandoned = "<color:#FBBF24>Abandoned claim profile <color:#9AA5B4>(<color:#F1F5F9><chunks> chunks freed<color:#9AA5B4>).";
        public String unclaimAllConfirm = "<color:#FBBF24>Confirm unclaiming all chunks <color:#9AA5B4>· <click:run_command:'/unclaim all confirm'><hover:show_text:'<color:#9AA5B4>Click to release all chunks'><color:#F87171><u>[/unclaim all confirm]</u></hover></click>";
        public String claimRenamed = "<color:#34D399>Renamed claim to <color:#60A5FA><name><color:#34D399>.";
        public String claimNameInvalid = "<color:#F87171>Use 3-32 letters, numbers, or spaces.";
        public String bannedClaimName = "<color:#F87171>That name contains a restricted word.";
        public String nameAlreadyInUse = "<color:#F87171>That claim name is already taken.";
        public String ownerAliasReset = "<color:#34D399>Owner alias reset.";
        public String ownerAliasSet = "<color:#34D399>Owner alias set to <color:#F1F5F9><alias><color:#34D399>.";
        public String claimColorChanged = "<color:#34D399>Claim color updated.";
        public String claimColorInvalid = "<color:#F87171>Invalid hex color. Example <color:#F1F5F9>#38BDF8";
    }

    public static class MemberMessages extends OkaeriConfig {
        public String cannotInviteSelf = "<color:#F87171>You cannot invite yourself.";
        public String alreadyMember = "<color:#F87171>That player is already a member.";
        public String alreadyInClaim = "<color:#F87171>That player is already in this claim.";
        public String targetAlreadyInClaim = "<color:#F87171><player> already belongs to another claim.";
        public String limitReached = "<color:#F87171>Member limit reached <color:#9AA5B4>(<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String membershipLimitReached = "<color:#F87171>You cannot join more than <color:#F1F5F9><limit> <color:#F87171>claims.";
        public String memberInvited = "<color:#34D399>Invited <color:#60A5FA><player> <color:#34D399>to your claim.";
        public String memberKicked = "<color:#F87171>Removed <color:#F1F5F9><player> <color:#F87171>from the claim.";
        public String youWereKicked = "<color:#F87171>You were removed from <color:#F1F5F9><owner><color:#F87171>'s claim.";
        public String notAMember = "<color:#F87171><player> is not a member of this claim.";
        public String inviteExpired = "<color:#F87171>That invitation expired.";
        public String inviteReceived = "<color:#9AA5B4>Invite from <color:#60A5FA><owner> <color:#9AA5B4>· <click:run_command:'/claim accept'><hover:show_text:'<color:#34D399>Join claim'><color:#34D399>[Accept]</hover></click> <click:run_command:'/claim deny'><hover:show_text:'<color:#F87171>Decline invite'><color:#F87171>[Decline]</hover></click>";
        public String inviteAccepted = "<color:#34D399>Joined <color:#60A5FA><owner><color:#34D399>'s claim.";
        public String inviteDenied = "<color:#9AA5B4>Invitation declined.";
        public String inviteWasDenied = "<color:#FBBF24><player> declined your claim invite.";
        public String noPendingInvite = "<color:#F87171>You have no pending invitations.";
        public String memberJoined = "<color:#34D399><player> joined your claim.";
        public String claimLeft = "<color:#FBBF24>Left <color:#60A5FA><name><color:#FBBF24>.";
        public String notInThatClaim = "<color:#F87171>You are not listed in that claim.";
        public String memberLeftClaim = "<color:#FBBF24><player> left your claim.";
        public String listHeader = "<color:#9AA5B4>Members";
        public String listEntry = "<color:#9AA5B4>- <color:#F1F5F9><player> <color:#9AA5B4>(<color:#60A5FA><role><color:#9AA5B4>)";
        public String listEmpty = "<color:#9AA5B4>No members listed.";
    }

    public static class TrustMessages extends OkaeriConfig {
        public String cannotTrustSelf = "<color:#F87171>You cannot trust yourself.";
        public String alreadyTrusted = "<color:#F87171>That player is already trusted.";
        public String notTrusted = "<color:#F87171>That player is not trusted in this claim.";
        public String trustInvited = "<color:#34D399>Sent trust invite to <color:#60A5FA><player><color:#34D399>.";
        public String trustInviteReceived = "<color:#9AA5B4>Trust invite from <color:#60A5FA><owner> <color:#9AA5B4>· <click:run_command:'/claim trust accept'><hover:show_text:'<color:#34D399>Accept trust'><color:#34D399>[Accept]</hover></click> <click:run_command:'/claim trust deny'><hover:show_text:'<color:#F87171>Decline trust'><color:#F87171>[Decline]</hover></click>";
        public String trustInviteAccepted = "<color:#34D399>Trust invitation accepted.";
        public String trustInviteDenied = "<color:#9AA5B4>Trust invitation declined.";
        public String trustInviteWasDenied = "<color:#FBBF24><player> declined your trust invite.";
        public String noPendingTrustInvite = "<color:#F87171>You have no pending trust invitations.";
        public String trustAdded = "<color:#34D399>Added trust for <color:#60A5FA><player><color:#34D399>. <color:#9AA5B4>Configure flags in <click:run_command:'/claim menu trusted'><hover:show_text:'<color:#9AA5B4>Open Trust Management'><color:#60A5FA><u>/claim menu trusted</u></hover></click><color:#9AA5B4>.";
        public String trustRemoved = "<color:#F87171>Removed trust for <color:#F1F5F9><player><color:#F87171>.";
        public String listHeader = "<color:#9AA5B4>Trusted players";
        public String listEntry = "<color:#9AA5B4>- <color:#F1F5F9><player>";
        public String listEmpty = "<color:#9AA5B4>No trusted players listed.";
    }

    public static class AllyMessages extends OkaeriConfig {
        public String claimNotFound = "<color:#F87171>Claim not found.";
        public String cannotAllySelf = "<color:#F87171>You cannot ally your own claim.";
        public String alreadyAllied = "<color:#F87171>Already allied with that claim.";
        public String notAllied = "<color:#F87171>Not allied with that claim.";
        public String allyInviteSent = "<color:#34D399>Sent alliance invite to <color:#60A5FA><name><color:#34D399>.";
        public String allyInviteReceived = "<color:#9AA5B4>Alliance request from <color:#60A5FA><name> <color:#9AA5B4>· <click:run_command:'/claim ally accept <name>'><hover:show_text:'<color:#34D399>Accept alliance'><color:#34D399>[Accept]</hover></click> <click:run_command:'/claim ally deny <name>'><hover:show_text:'<color:#F87171>Decline alliance'><color:#F87171>[Decline]</hover></click>";
        public String noAllyInviteFrom = "<color:#F87171>No pending alliance invite from <color:#F1F5F9><name><color:#F87171>.";
        public String specifyAllyName = "<color:#F87171>Specify claim name <color:#9AA5B4>(/claim ally accept <name>).";
        public String allyAccepted = "<color:#34D399>Allied with <color:#60A5FA><name><color:#34D399>.";
        public String allyAcceptedTarget = "<color:#60A5FA><name> <color:#34D399>accepted your alliance request.";
        public String allyDenied = "<color:#9AA5B4>Declined alliance request from <color:#F1F5F9><name><color:#9AA5B4>.";
        public String allyRemoved = "<color:#FBBF24>Ended alliance with <color:#F1F5F9><name><color:#FBBF24>.";
    }

    public static class BanMessages extends OkaeriConfig {
        public String cannotBanSelf = "<color:#F87171>You cannot ban yourself.";
        public String alreadyBanned = "<color:#F87171><player> is already banned from this claim.";
        public String notBanned = "<color:#F87171><player> is not banned from this claim.";
        public String playerBanned = "<color:#F87171>Banned <color:#F1F5F9><player> <color:#F87171>from this claim.";
        public String bannedFromClaim = "<color:#F87171><b>⚠</b> Entry denied. You are banned from <color:#F1F5F9><owner><color:#F87171>'s claim.";
        public String playerUnbanned = "<color:#34D399>Unbanned <color:#60A5FA><player><color:#34D399>.";
        public String unbannedByOwner = "<color:#34D399><owner> unbanned you from their claim.";
        public String bannedTeleportedOut = "<color:#F87171>Ejected from <color:#F1F5F9><owner><color:#F87171>'s claim.";
        public String bannedNoEscape = "<color:#F87171>No safe exit found. Run <color:#F1F5F9>/claim unstuck<color:#F87171>.";
        public String listEmpty = "<color:#9AA5B4>No banned players.";
        public String listHeader = "<color:#F87171>Banned players";
        public String listEntry = "<color:#9AA5B4>- <color:#F1F5F9><player>";
    }

    public static class WarpMessages extends OkaeriConfig {
        public String notInOwnClaim = "<color:#F87171>Stand inside your own claim to set a warp.";
        public String warpSet = "<color:#34D399>Warp <color:#60A5FA><name> <color:#34D399>saved.";
        public String warpDeleted = "<color:#FBBF24>Deleted warp <color:#F1F5F9><name><color:#FBBF24>.";
        public String warpTeleport = "<color:#34D399>Teleported to <color:#60A5FA><name><color:#34D399>.";
        public String warpNotFound = "<color:#F87171>Warp <color:#F1F5F9><name> <color:#F87171>not found.";
        public String warpLimitReached = "<color:#F87171>Warp limit reached.";
        public String warpLocationUpdated = "<color:#34D399>Updated location for <color:#60A5FA><name><color:#34D399>.";
        public String warpIconUpdated = "<color:#34D399>Updated icon for <color:#60A5FA><name><color:#34D399>.";
        public String warpMadePublic = "<color:#34D399>Warp <color:#60A5FA><name> <color:#34D399>is now <color:#F1F5F9>public <color:#9AA5B4>(/claim publicwarps).";
        public String warpMadePrivate = "<color:#FBBF24>Warp <color:#F1F5F9><name> <color:#FBBF24>is now private.";
        public String publicwarpsNoWarps = "<color:#9AA5B4>No public warps available.";
        public String publicwarpsTeleported = "<color:#34D399>Teleported to <color:#60A5FA><owner><color:#9AA5B4>:<color:#60A5FA><name><color:#34D399>.";
    }

    public static class ProtectionMessages extends OkaeriConfig {
        public String bucketDenied = "<color:#F87171>Bucket use is denied in this claim.";
        public String commandBlocked = "<color:#F87171>That command is blocked inside this claim.";
        public String visitorSettingsLocked = "<color:#F87171>Visitor settings are locked by server configuration.";
        public String pvpDenied = "<color:#F87171>PvP is disabled in this claim.";
        public String pvpEnabled = "<color:#F87171><b>⚠ PVP ENABLED</b> <color:#9AA5B4>inside this claim.";
        public String pvpEnabledTemp = "<color:#F87171><b>⚠ PVP ENABLED</b> <color:#9AA5B4>for <color:#F1F5F9><time>s<color:#9AA5B4>.";
        public String pvpDisabled = "<color:#34D399>PvP is now disabled inside this claim.";
        public String pvpForceLocked = "<color:#F87171>PvP is forced on by server configuration.";
        public String tntEnabled = "<color:#FBBF24>TNT explosions <color:#F1F5F9>enabled <color:#FBBF24>in this claim.";
        public String tntDisabled = "<color:#34D399>TNT explosions <color:#F1F5F9>blocked <color:#34D399>in this claim.";
        public String mobSpawningEnabled = "<color:#34D399>Mob spawning <color:#F1F5F9>enabled <color:#34D399>in this claim.";
        public String mobSpawningDisabled = "<color:#FBBF24>Mob spawning <color:#F1F5F9>disabled <color:#FBBF24>in this claim.";
        public String unstuckCooldown = "<color:#F87171>Wait <color:#F1F5F9><seconds>s <color:#F87171>before running unstuck again.";
        public String unstuckSuccess = "<color:#34D399>Moved to nearest safe wilderness.";
        public String unstuckNotInClaim = "<color:#F87171>You are not inside a claim.";
        public String unstuckSearching = "<color:#9AA5B4>Finding safe wilderness...";
        public String unstuckFailed = "<color:#F87171>No safe wilderness block found nearby.";
    }

    public static class TitleMessages extends OkaeriConfig {
        public String titleUpdated = "<color:#34D399>Claim title updated.";
        public String titleCancelled = "<color:#9AA5B4>Title update cancelled.";
    }

    public static class ChatNotificationMessages extends OkaeriConfig {
        public String claimEnterChat = "<color:#9AA5B4>Entered <color:#60A5FA><claim> <color:#9AA5B4>(<color:#F1F5F9><owner><color:#9AA5B4>)";
        public String claimLeaveChat = "<color:#9AA5B4>Left <color:#60A5FA><claim>";
        public String wildernessEnterChat = "";
        public String claimNotifyEnabled = "<color:#34D399>Territory chat notifications enabled.";
        public String claimNotifyDisabled = "<color:#9AA5B4>Territory chat notifications disabled.";
    }

    public static class ActionbarMessages extends OkaeriConfig {
        public String actionbarWilderness = "<color:#9AA5B4>Wilderness";
        public String actionbarOwnedByYou = "<color:#34D399>★ Your Claim <color:#9AA5B4>· <color:#F1F5F9><claim>";
        public String actionbarOwnedByOther = "<color:#F87171><owner>'s Claim <color:#9AA5B4>· <color:#F1F5F9><claim>";
        public String actionbarTrusted = "<color:#60A5FA><owner>'s Claim <color:#9AA5B4>· Trusted";
        public String actionbarAdmin = "<color:#FBBF24><owner>'s Claim <color:#9AA5B4>· Admin";
        public String actionbarBanned = "<color:#F87171>⚠ Banned <color:#9AA5B4>· <color:#F1F5F9><owner>";
    }

    public static class MinimapMessages extends OkaeriConfig {
        public String alreadyOwned = "<color:#FBBF24>You already hold a Territory Map.";
        public String received = "<color:#34D399>Received Territory Map. <color:#9AA5B4>Hold to view nearby chunks.";
        public String zoomChanged = "<color:#9AA5B4>Map zoom <color:#60A5FA><mode>";
        public String zoomOverview = "16x16 Overview";
        public String zoomClose = "8x8 Detail";
    }

    public static class EconomyMessages extends OkaeriConfig {
        public String disabled = "<color:#F87171>Economy addon is not enabled.";
        public String invalidAmount = "<color:#F87171>Amount must be greater than 0.";
        public String insufficientFunds = "<color:#F87171>Insufficient funds <color:#9AA5B4>(Cost <color:#F1F5F9><cost><color:#9AA5B4>, Balance <color:#F1F5F9><balance><color:#9AA5B4>).";
        public String transactionFailed = "<color:#F87171>Transaction failed.";
        public String purchaseError = "<color:#F87171>Failed to complete purchase.";
        public String limitPurchasesDisabled = "<color:#F87171>Purchasing extra limits is disabled.";
        public String maxBonusClaimBlocksReached = "<color:#F87171>Bonus cap is <color:#F1F5F9><max><color:#F87171>. Remaining allowed <color:#F1F5F9><remaining><color:#F87171>.";
        public String maxTotalClaimLimitReached = "<color:#F87171>Total claim cap is <color:#F1F5F9><max><color:#F87171>. Remaining allowed <color:#F1F5F9><remaining><color:#F87171>.";
        public String boughtClaimBlocks = "<color:#34D399>Purchased <color:#60A5FA><amount> <color:#34D399>claim blocks <color:#9AA5B4>(Limit <color:#F1F5F9><limit><color:#9AA5B4>).";
        public String boughtRoleSlot = "<color:#34D399>Purchased custom role slot <color:#9AA5B4>(Bonus <color:#F1F5F9><total><color:#9AA5B4>).";
        public String boughtMemberSlot = "<color:#34D399>Purchased member slot <color:#9AA5B4>(Bonus <color:#F1F5F9><total><color:#9AA5B4>).";
        public String boughtWarpSlot = "<color:#34D399>Purchased warp slot <color:#9AA5B4>(Bonus <color:#F1F5F9><total><color:#9AA5B4>).";
        public String sellCancelled = "<color:#9AA5B4>Marketplace listing cancelled.";
        public String sellError = "<color:#F87171>Failed to list claim profile.";
    }

    public static class AdminMessages extends OkaeriConfig {
        public String adminChunkClaimed = "<color:#34D399>Claimed chunk for Server Land.";
        public String adminAlreadyClaimed = "<color:#F87171>Chunk claimed by <color:#F1F5F9><owner><color:#F87171>. Unclaim it first.";
        public String adminBypassedUnclaim = "<color:#34D399>Unclaimed chunk via admin bypass.";
        public String adminClaimInfoOwnedBy = "<color:#9AA5B4>Owner <color:#60A5FA><owner> <color:#9AA5B4>(<color:#F1F5F9><uuid><color:#9AA5B4>)";
        public String adminClaimInfoId = "<color:#9AA5B4>Profile <color:#F1F5F9><id>";
        public String adminEditingProfile = "<color:#34D399>Editing claim profile for <color:#60A5FA><player><color:#34D399>.";
        public String adminAddChunkSuccess = "<color:#34D399>Added <color:#60A5FA><amount> <color:#34D399>bonus chunks to <color:#60A5FA><player><color:#34D399>.";
        public String adminSetChunkSuccess = "<color:#34D399>Set bonus chunks for <color:#60A5FA><player> <color:#34D399>to <color:#60A5FA><amount> <color:#9AA5B4>(Limit <color:#F1F5F9><limit><color:#9AA5B4>).";
        public String adminRemoveChunkSuccess = "<color:#FBBF24>Removed <color:#F1F5F9><amount> <color:#FBBF24>bonus chunks from <color:#F1F5F9><player> <color:#9AA5B4>(Limit <color:#F1F5F9><limit><color:#9AA5B4>).";
        public String adminResetChunkSuccess = "<color:#FBBF24>Reset bonus chunks for <color:#F1F5F9><player> <color:#9AA5B4>(Limit <color:#F1F5F9><limit><color:#9AA5B4>).";
        public String trustListHeader = "<color:#9AA5B4>Trusted players for <color:#60A5FA><player>";
        public String trustListEntry = "<color:#9AA5B4>- <color:#F1F5F9><player>";
        public String trustWhoHeader = "<color:#9AA5B4>Claims trusting <color:#60A5FA><player>";
        public String trustWhoEntry = "<color:#9AA5B4>- <color:#F1F5F9><claim>";
        public String trustWhoEmpty = "<color:#9AA5B4>Player is not trusted in any loaded claim.";
        public String adminDecayRunStarted = "<color:#9AA5B4>Running inactive claim decay check...";
        public String adminDecayRunFinished = "<color:#34D399>Decay complete <color:#9AA5B4>(<color:#F1F5F9><claims> claims<color:#9AA5B4>, <color:#F1F5F9><chunks> chunks freed<color:#9AA5B4>).";
        public String decayAlreadyRunning = "<color:#F87171>A decay scan is already running.";
        public String adminDecayExemptSet = "<color:#34D399>Decay exemption for <color:#60A5FA><player><color:#9AA5B4>: <color:#F1F5F9><status>";
        public String decayStatusExempt = "EXEMPT";
        public String decayStatusNotExempt = "NOT EXEMPT";
        public String decayListEmpty = "<color:#9AA5B4>No players are exempt from decay.";
        public String decayListHeader = "<color:#9AA5B4>Decay-exempt players <color:#9AA5B4>(<color:#F1F5F9><count><color:#9AA5B4>)";
        public String decayListEntry = "<color:#9AA5B4>- <color:#F1F5F9><player> <color:#9AA5B4>(<uuid>)";
        public String decayListError = "<color:#F87171>Failed to retrieve exempt players.";
        public String aliasReset = "<color:#34D399>Reset owner alias for <color:#60A5FA><claim><color:#34D399>.";
        public String aliasSet = "<color:#34D399>Set owner alias for <color:#60A5FA><claim> <color:#34D399>to <color:#F1F5F9><alias><color:#34D399>.";
    }

    public static class GuiMessages extends OkaeriConfig {
        public String anvilSave = "<color:#34D399>Save";
        public String renameCancelled = "<color:#9AA5B4>Input cancelled.";
        public String defaultUnnamedClaim = "Unnamed Claim";
        public String defaultUnknownOwner = "Unknown";
        public String promptRenameClaim = "Rename Claim";
        public String promptOwnerAlias = "Set Owner Alias";
        public String promptHexColor = "Hex Color";
        public String promptEnterTitle = "Enter Title";
        public String promptLeaveTitle = "Leave Title";
        public String promptRoleName = "Role Name";
        public String promptRolePriority = "Priority";

        public String confirmYesName = "<color:#34D399>Confirm";
        public String confirmYesLore = "<color:#9AA5B4>Click to confirm";
        public String confirmNoName = "<color:#F87171>Cancel";
        public String confirmNoLore = "<color:#9AA5B4>Click to go back";
        public String confirmSellActive = "<color:#1E293B>Sell Active Profile?";
        public String confirmAbandonAll = "<color:#1E293B>Abandon ALL claims?";
        public String confirmResetClaimName = "<color:#1E293B>Reset claim name?";
        public String confirmTransfer = "<color:#1E293B>Transfer to <player>?";
        public String confirmKick = "<color:#1E293B>Kick <player>?";
        public String confirmBan = "<color:#1E293B>Ban <player>?";
        public String confirmRemoveTrust = "<color:#1E293B>Remove <player>?";
        public String confirmRemoveAlly = "<color:#1E293B>Remove Alliance?";
        public String confirmDeleteRole = "<color:#1E293B>Delete role <role>?";

        public String onlyOwnerTransfer = "<color:#F87171>Only the claim owner can transfer ownership.";
        public String ownershipTransferred = "<color:#34D399>Transferred ownership to <color:#60A5FA><player><color:#34D399>.";
        public String cannotDeleteDefaultRole = "<color:#F87171>Default roles cannot be deleted.";
        public String roleDeleted = "<color:#FBBF24>Deleted role <color:#F1F5F9><role><color:#FBBF24>.";
        public String roleLimitReached = "<color:#F87171>Custom role limit reached <color:#9AA5B4>(<color:#F1F5F9><limit><color:#9AA5B4>).";
        public String roleAssignedPlayer = "<color:#34D399>Changed <color:#60A5FA><player><color:#34D399>'s role to <color:#F1F5F9><role><color:#34D399>.";
        public String operationCancelled = "<color:#9AA5B4>Operation cancelled.";
        public String roleNameUpdated = "<color:#34D399>Role name updated.";
        public String roleNameInvalid = "<color:#F87171>Invalid role name.";
        public String rolePriorityUpdated = "<color:#34D399>Role priority updated.";
        public String rolePriorityInvalid = "<color:#F87171>Priority must be a number.";
        public String roleSaved = "<color:#34D399>Role saved.";

        public String defaultRoleBadge = "<color:#9AA5B4>Default Role";
        public String clickManage = "<color:#FBBF24>Click to manage";
        public String clickSelect = "<color:#9AA5B4>Click to select";
        public String skullItemName = "<i:false><color:#60A5FA><name>";
        public String roleDefaultName = "<i:false><color:#38BDF8><role>";
        public String roleCustomName = "<i:false><color:#F1F5F9><role>";
        public String warpItemName = "<i:false><color:#38BDF8><name>";
        public String warpManageHint = "<color:#FBBF24>Right-click <color:#9AA5B4>to manage";
        public String warpIconName = "<i:false><color:#38BDF8><icon>";
        public String warpIconHint = "<color:#9AA5B4>Click to select this icon";
        public String clickAssignRole = "<color:#34D399>Click to assign to <player>";
        public String clickEditPermissions = "<color:#9AA5B4>Left-click <color:#F1F5F9>Edit permissions";
        public String clickEditRole = "<color:#9AA5B4>Left-click <color:#F1F5F9>Edit role";
        public String clickDeleteRole = "<color:#9AA5B4>Right-click <color:#F87171>Delete role";
        public String clickDeleteRoleDisabled = "<color:#9AA5B4><st>Right-click Delete role</st>";
        public String clickRemove = "<color:#9AA5B4>Right-click <color:#F87171>Remove";
        public String allyOwnerLine = "<color:#9AA5B4>Owner <color:#F1F5F9><owner>";
        public String memberRoleLine = "<color:#9AA5B4>Role <color:#F1F5F9><role>";

        public String titleStatusLabel = "<color:#9AA5B4>Status <value>";
        public String statusEnabled = "<color:#34D399>Enabled";
        public String statusDisabled = "<color:#F87171>Disabled";
        public String titleCurrentText = "<color:#9AA5B4>Text <reset><text>";
        public String titleModeLabel = "<color:#9AA5B4>Mode <value>";
        public String titleModeTitle = "<color:#60A5FA>Title";
        public String titleModeSubtitle = "<color:#34D399>Subtitle";
        public String titleLeftClickEdit = "<color:#9AA5B4>Left-click <color:#F1F5F9>Edit text";
        public String titleRightClickMode = "<color:#9AA5B4>Right-click <color:#F1F5F9>Toggle Title/Subtitle";

        public String warpSelectTitle = "<color:#1E293B>Select Warp <name>";
        public String warpItemDisplay = "<color:#60A5FA><name> <color:#9AA5B4>(<owner>)";
        public String warpOwnerLine = "<color:#9AA5B4>Owner <color:#F1F5F9><owner>";
        public String warpClaimLine = "<color:#9AA5B4>Claim <color:#60A5FA><claim>";
        public String warpLocationLine = "<color:#9AA5B4>Location <color:#F1F5F9><location>";
        public String warpTeleportHint = "<color:#34D399>Left-click to teleport";
        public String warpCloseName = "<color:#F87171>Close";
        public String warpCloseLore = "<color:#9AA5B4>Click to close";
        public String warpPrevName = "<color:#F1F5F9>Previous Page";
        public String warpNextName = "<color:#F1F5F9>Next Page";
    }

    public static class BedrockMessages extends OkaeriConfig {
        public String geyserChatPrompt = "<color:#FBBF24>[Bedrock] Type '<title>' in chat <color:#9AA5B4>(or 'cancel')<color:#FBBF24>:";
        public String geyserChatCancelled = "<color:#9AA5B4>[Bedrock] Input cancelled.";
        public String geyserFormSent = "<color:#9AA5B4>[Bedrock] Sent form '<title>'.";
        public String geyserFormCancelled = "<color:#9AA5B4>[Bedrock] Form closed.";
        public String geyserConfirmTitle = "Confirm Action";
        public String geyserConfirmButton = "Confirm";
        public String geyserCancelButton = "Cancel";
        public String geyserAbandonContent = "Abandon your claim? All claimed chunks will be released.";
        public String geyserUnclaimAllContent = "Unclaim ALL chunks in this profile? This cannot be undone.";
        public String geyserBanContent = "Ban <player> from your claim? They will be blocked from entering.";
    }

    public static class UpdateMessages extends OkaeriConfig {
        public String available = "<color:#9AA5B4>[<color:#38BDF8>LandClaim<color:#9AA5B4>] <color:#FBBF24>Update <color:#34D399><latest> <color:#FBBF24>available <color:#9AA5B4>(current <color:#F1F5F9><current><color:#9AA5B4>) · <click:open_url:'<url>'><hover:show_text:'<color:#9AA5B4>Open Modrinth release'><color:#34D399><u>[Download]</u></hover></click>";
    }
}
