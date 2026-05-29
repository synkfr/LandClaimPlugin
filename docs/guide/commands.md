# Commands

All commands use `/claim` (alias: `/c`) as the base.

## General Commands

| Command | Description |
|---|---|
| `/claim` | Claim the chunk you're standing in |
| `/claim profiles` | Open the active profile selector (Multi-Profile mode) |
| `/claim create <name>` | Create a new claim profile with the given name |
| `/claim auto` | Toggle auto-claim mode (claim chunks as you walk) |
| `/claim menu` | Open the main claim management GUI |
| `/claim info` | View info about the claim at your location |
| `/claim visible` | Toggle claim boundary visualization |
| `/claim toggle <mode>` | Switch visualization mode (`display_entities`, `particles`, `off`) |
| `/claim visualization <mode>` | Alias for `/claim toggle` |
| `/claim rename <name>` | Rename your active claim profile (3-32 alphanumeric characters) |
| `/claim color <color>` | Change claim color using a named color (e.g., `red`, `lime`) or hex code (e.g., `#FF5500`) |
| `/claim unstuck` | Safely teleport to the nearest wilderness block |
| `/claim abandon` | Delete your entire active claim profile and all its chunks |
| `/claim unclaimall` | Unclaim all chunks and delete your active profile (requires `/claim unclaimall confirm`) |
| `/claim pvp <on/off> [time]` | Toggle PvP in the claim, with optional duration in seconds |
| `/unclaim` | Unclaim the chunk you're standing in |
| `/unclaim all` | Unclaim all chunks belonging to your active profile |

## Menu Shortcuts

Jump directly to specific GUI panels without navigating through the main menu.
These commands respect the same permission checks as clicking the GUI buttons.

| Command | Permission Required | Description |
|---|---|---|
| `/claim menu settings` | `MANAGE_SETTINGS` | Open claim settings (color, PvP, visibility, titles) |
| `/claim menu members` | `MANAGE_MEMBERS` | Open member management |
| `/claim menu roles` | `MANAGE_ROLES` | Open role management |
| `/claim menu trusted` | `MANAGE_MEMBERS` | Open trusted player management |
| `/claim menu visitors` | `MANAGE_SETTINGS` | Open visitor settings |
| `/claim menu allies` | `MANAGE_SETTINGS` | Open ally management |
| `/claim menu map` | — | Open the claim map |
| `/claim menu warps` | — | Open warp management |

## Member Commands

| Command | Description |
|---|---|
| `/claim member invite <player>` | Invite a player to join your claim |
| `/claim member kick <player>` | Remove a member from your claim |
| `/claim member list` | List all members and their roles |
| `/claim accept <name>` | Accept a pending member invitation |
| `/claim deny <name>` | Deny a pending member invitation |
| `/claim leave <claim name>` | Leave a claim you are a member or trusted player of |

## Trust Commands

| Command | Description |
|---|---|
| `/claim trust invite <player>` | Send a trust invitation to a player |
| `/claim trust accept` | Accept a pending trust invitation |
| `/claim trust deny` | Deny a pending trust invitation |
| `/claim trust remove <player>` | Remove a trusted player |
| `/claim trust list` | List all trusted players and their flags |

## Ally Commands

| Command | Description |
|---|---|
| `/claim ally invite <name>` | Send an alliance request to another claim |
| `/claim ally accept <name>` | Accept an alliance request |
| `/claim ally deny <name>` | Deny an alliance request |
| `/claim ally remove <name>` | Remove an existing alliance |

## Warp Commands

| Command | Description |
|---|---|
| `/claim setwarp <name>` | Set a warp at your current location |
| `/claim delwarp <name>` | Delete a warp |
| `/claim warp <name>` | Teleport to a warp |
| `/claim warps` | Open the warp management GUI |

## Admin Commands

| Command | Description |
|---|---|
| `/claim admin claim` | Claim the current chunk for the global Admin Profile (Server Land) |
| `/claim admin menu` | Open the management menu for the global Admin Profile |
| `/claim admin edit <player>` | Open any player's claim management GUI with full override |
| `/claim admin check` | View detailed claim info (owner UUID, profile name) |
| `/claim admin unclaim` | Force-unclaim the chunk you're standing in |
| `/claim admin add chunk <player> <amount>` | Add bonus claim chunks to a player's limit |
| `/claim admin setalias <claim> <alias>` | Set or reset an owner's custom alias |
| `/claim admin trust list <owner>` | List players trusted by this owner |
| `/claim admin trust who <player>` | List claims where this player is trusted |
| `/claim admin reload` | Reload the plugin configuration and messages |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `landclaim.*` | All LandClaim permissions | `false` |
| `landclaim.claim` | Basic claiming ability | `true` |
| `landclaim.auto` | Use auto-claim mode | `true` |
| `landclaim.admin` | Admin commands and bypass all protection | `op` |
| `landclaim.unstuck` | Teleport to a safe wilderness location | `true` |
| `landclaim.unclaim` | Unclaim the current chunk | `true` |
| `landclaim.member` | Access to member subcommands | `true` |
| `landclaim.trust` | Access to trust subcommands | `true` |
| `landclaim.ally` | Access to ally subcommands | `true` |
| `landclaim.abandon` | Abandon active claim profile | `true` |
| `landclaim.create` | Create new claim profiles | `true` |
| `landclaim.visible` | Toggle boundary visibility | `true` |
| `landclaim.toggle` | Switch visualization mode | `true` |
| `landclaim.info` | View claim information | `true` |
| `landclaim.setwarp` | Set claim warps | `true` |
| `landclaim.delwarp` | Delete claim warps | `true` |
| `landclaim.warp` | Teleport to claim warps | `true` |
| `landclaim.pvp` | Toggle PvP state | `true` |
| `landclaim.rename` | Rename claims | `true` |
| `landclaim.color` | Change claim colors | `true` |
| `landclaim.unclaimall` | Unclaim all land | `true` |
| `landclaim.leave` | Leave a claim | `true` |
| `landclaim.menu.*` | Access to all GUI menus | `true` |
| `landclaim.menu.<menu>` | Access to a specific GUI menu (e.g. `mainmenu`, `claimsettings`, `membermanagement`, `rolemanagement`, `trustmanagement`, `visitorsettings`, `allymanagement`, `claimmap`, `warpmanagement`, `profileselector`) | `true` |
| `landclaim.limit.<n>` | Override the chunk claim limit | `false` |
| `landclaim.list` | List claims | `true` |
| `landclaim.warps.limit.<n>` | Override the warps limit | `false` |
| `landclaim.createrole.<n>` | Override the max number of custom roles | `false` |
