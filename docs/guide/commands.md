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
| `/claim unstuck` | Safely teleport to the nearest wilderness block |
| `/claim abandon` | Delete your entire active claim profile and all its chunks |
| `/claim pvp <on/off> [time]` | Toggle PvP in the claim, with optional duration in seconds |
| `/unclaim` | Unclaim the chunk you're standing in |
| `/unclaim all` | Unclaim all chunks belonging to your active profile |

## Member Commands

| Command | Description |
|---|---|
| `/claim member invite <player>` | Invite a player to join your claim |
| `/claim member kick <player>` | Remove a member from your claim |
| `/claim member list` | List all members and their roles |
| `/claim accept <name>` | Accept a pending member invitation |
| `/claim deny <name>` | Deny a pending member invitation |

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
| `/claim admin check` | View detailed claim info (owner UUID, profile name) |
| `/claim admin unclaim` | Force-unclaim the chunk you're standing in |
| `/claim admin edit <player>` | Open any player's claim management GUI |
| `/claim admin add chunk <player> <amount>` | Add bonus claim chunks to a player's limit |
| `/claim admin setalias <claim> <alias>` | Set or reset an owner's custom alias |
| `/claim admin trust list <owner>` | List players trusted by this owner |
| `/claim admin trust who <player>` | List claims where this player is trusted |
| `/claim admin reload` | Reload the plugin configuration |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `landclaim.*` | All LandClaim permissions | `false` |
| `landclaim.claim` | Basic claiming ability | `true` |
| `landclaim.auto` | Use auto-claim mode | `true` |
| `landclaim.admin` | Admin commands and bypass all protection | `op` |
| `landclaim.limit.<n>` | Override the chunk claim limit | `false` |
| `landclaim.list` | List claims | `true` |
| `landclaim.warps.limit.<n>` | Override the warps limit | `false` |
| `landclaim.createrole.<n>` | Override the max number of custom roles | `false` |
