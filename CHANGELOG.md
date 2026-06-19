# Changelog

All notable changes to LandClaimPlugin will be documented in this file.

## [2.5.0] - 2026-06-19

### Added
- **Per-Claim Ban System:** Owners can now permanently ban a player from entering or interacting with their claim. Banned players lose every flag, are physically pushed back at chunk boundaries, and (if online at the moment of the ban) are teleported outside the claim. Survives server restarts and is independent of role/trust removal. New commands:
  - `/claim ban <player>` — ban a player from your active claim
  - `/claim unban <player>` — reverse a ban
  - `/claim banlist` — list all banned players
  - `landclaim.ban` permission (default `true`)
  - "Ban" button added to the Player Control Panel GUI alongside Kick/Transfer.
- **Geyser 2.x Native Form Support:** Bedrock players now get native Bedrock UI forms (SimpleForm, CustomForm, ModalForm) when the plugin would otherwise prompt them. Reflection-based, no hard Geyser dependency.
  - `AnvilInputGUI` (rename, color, role name, etc.) → CustomForm text input
  - `/claim abandon` → ModalForm confirmation
  - `/unclaim all` → ModalForm confirmation
  - `/claim ban <player>` → ModalForm confirmation
  - Geyser-Spigot and Floodgate added to `soft-depend`. `PluginConfig.geyserForms` (default `true`) master-toggles the form path.

### Bug Fixes
- **SquareMap markers not appearing after claiming:** The world-name comparison was a strict string equality, but SquareMap's `WorldIdentifier.asString()` can return the namespaced form (`minecraft:overworld`) while `ChunkPosition.world()` stores the plain Bukkit world name. The filter now tolerates namespace prefix and case differences, so newly claimed chunks are visible on the live map.
- **SquareMap silent activation failure:** The original `SquaremapHook` checked for the plugin once and would silently stay inactive if SquareMap loaded after LandClaimPlugin. The hook now (a) listens for `PluginEnableEvent` so it activates when SquareMap enables later, and (b) retries `SquaremapProvider.get()` on the next tick if the API isn't yet registered. `soft-depend` now also includes `squaremap` and `Pl3xMap` so the server orders them ahead of LandClaimPlugin.

## [2.3.0] - 2026-05-29

### Added
- **Dynamic Custom Claim Colors:** Customized claim colors are now automatically applied across scoreboard, PAPI placeholders (`%landclaim_owner%`, `%landclaim_name%`, `%landclaim_profile%`), Spigot action bars, transition titles, and the entire GUI menu system.
- **LuckPerms Separate Permissions:** Added optional granular permission control (`useSeparatePremission = false` by default) to restrict individual GUI menus (`landclaim.menu.<menu>`, `landclaim.menu.*`) and commands.
- **Banned Claim Names Filter:** Added configurable banned word filter to restrict offensive or inappropriate claim names via `banned-claim-name.txt` and `bannedClaimNamesFile` configuration parameter.

### Improvements
- **GUI Ally Invite Selector:** Replaced the old text-based Anvil GUI for inviting allies with a highly intuitive player selector screen (`OnlinePlayerSelectorGUI`), matching the member selection workflow.
- **Tab-Completion for `/claim leave`:** Implemented custom Cloud SuggestionProvider to autocomplete leavable claim names for players.
- **Dynamic Action Bar Refresh:** Action bars and transition titles now instantly update on chunk unclaimed, abandon, and trust status changes without requiring player movement.
- **Trust Consent Flow:** Adding trusted members via the GUI now sends proper trust invitations rather than auto-adding them without consent.

## [2.2.0] - 2026-05-21

### Bug Fixes
- **Multi-Profile Claiming:** Fixed claims always targeting the first profile instead of the active one when Multi-Profile mode is enabled. Root cause: `claimChunk()` and `claimChunks()` used `getProfile(playerId)` which only finds profiles keyed by the player's UUID, not the randomly-generated multi-profile IDs.
- **Multi-Profile Unclaiming:** Fixed `/unclaim` failing for chunks owned by multi-profile profiles due to incorrect ownership check comparing `profileId` with `playerId`.
- **Multi-Profile Abandon:** Fixed `/claim abandon` always deleting the first profile instead of the active one.
- **Blocked Worlds Not Loading:** Fixed `blockWorld` and `blockCmd` lists from `config.yml` not being applied until the first `/claim admin reload`. Changes now take effect immediately on server startup.

### New Commands
- `/claim rename <name>` — Rename your active claim profile via CLI (3-32 alphanumeric characters).
- `/claim color <color>` — Change claim color via CLI using named colors (`red`, `blue`, `lime`, etc.) or hex codes (`#FF5500`). Includes tab-completion for all named colors.
- `/claim visualization <mode>` — Alias for `/claim toggle <mode>`.
- `/claim unclaimall` — Unclaim all chunks and delete your active profile, with two-step confirmation (`/claim unclaimall confirm`).
- `/claim menu settings` — Open claim settings GUI directly.
- `/claim menu members` — Open member management GUI directly.
- `/claim menu roles` — Open role management GUI directly.
- `/claim menu trusted` — Open trusted player management GUI directly.
- `/claim menu visitors` — Open visitor settings GUI directly.
- `/claim menu allies` — Open ally management GUI directly.
- `/claim menu map` — Open claim map GUI directly.
- `/claim menu warps` — Open warp management GUI directly.

### Improvements
- All `/claim menu` subcommands enforce the same permission checks as their GUI button equivalents (`MANAGE_SETTINGS`, `MANAGE_MEMBERS`, `MANAGE_ROLES`).
- Updated `unclaimAllConfirm` message to include a clickable `/claim unclaimall confirm` command.
