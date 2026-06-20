<div align="center">

<img src="https://i.postimg.cc/jS6mh13k/minecraft-title-2.png" alt="LandClaim Banner" width="100%">

# 💠 LandClaimPlugin
**Advanced Territory Protection for Minecraft**

A powerful, feature-rich chunk-based land protection plugin for Paper servers. Create claim profiles, protect your builds, manage members with granular roles, form alliances, set warps, and customize every aspect of your territory.

[![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Version](https://img.shields.io/github/v/release/synkfr/LandClaimPlugin?style=flat-square&color=40ae24&label=Version)](https://github.com/synkfr/LandClaimPlugin/releases)
[![License](https://img.shields.io/github/license/synkfr/LandClaimPlugin?style=flat-square&color=blue)](LICENSE)
[![Discord](https://img.shields.io/discord/1378591879393710110?color=5865F2&label=Discord&logo=discord&logoColor=white&style=flat-square)](https://discord.gg/pAPPvSmWRK)

</div>

<br>

> ⚠️ **DEVELOPMENT BUILDS**
>
> Development builds are automatically generated from the latest commits and may include unfinished features, breaking changes, or experimental code. **These builds are not recommended for production use.**
> 
> **If you choose to run a dev build:**
> - Expect bugs, crashes, or incomplete functionality.
> - Configs and data formats may change without notice.
> - No guarantee of backward compatibility.
> 
> Use them only for testing, early access, or contributing feedback.
> 📦 **Download latest dev builds:** [GitHub Releases](https://github.com/synkfr/LandClaimPlugin/releases)

---

## ✨ Features

### Core Claiming
- **Claim Profiles** — Manage your land using claim profiles. Depending on server configuration, players can own a single profile or utilize the **Multi-Profile System** to manage multiple independent bases and quickly switch between them via an interactive GUI.
- **Chunk-Based Claims** — 16×16 block protection zones, simple and intuitive.
- **Auto-Claim** — Automatically claim chunks as you walk.
- **Connected Claims** — Optionally require claims to be adjacent (with diagonal support).
- **Claim Map GUI** — Interactive in-game map showing nearby claims and ownership.
- **Interactive Selectors** — Programmatically select online players via their heads for Trust, Member, and Ally management.
- **Unstuck Command** — A safe `/claim unstuck` feature that teleports trapped players to the nearest safe wilderness block.

### Permission System
- **4-Tier Priority Chain** — `Owner > Role > Trusted > Visitor` — the first matching tier decides
- **Custom Roles** — Create and configure custom roles (e.g., Member, CoOwner) with granular flag-based permissions
- **Trusted Players** — Grant individual players specific permission overrides
- **Visitor Settings** — Configure what non-members can do in your claims
- **25+ Permission Flags** — Doors, trapdoors, containers, workstations, animals, vehicles, redstone, and more

### Social Systems
- **Member System** — Invite players to join your claim, assign them roles, and manage access
- **Ally System** — Form mutual alliances between claims with configurable inter-claim permissions
- **Trust System** — Grant individual players per-player permission overrides
- **Ban System** — Hard-deny a player from entering or interacting with your claim. Banned players are physically pushed back at chunk boundaries and (if online at the moment of the ban) teleported outside. Survives server restarts.
- **Warp System** — Set named warps within your claims with custom icons and per-player limits
- **Public Warps** — Publish any of your warps to the server-wide public list. Other players can browse and teleport via `/claim publicwarps`.

### Protection
- **Block Protection** — Prevent unauthorized breaking and placing
- **Entity Protection** — Protect animals, armor stands, and item frames
- **Explosion Protection** — Block TNT, creeper, and other explosion damage
- **Interaction Protection** — Control door, container, and workstation access
- **Item Protection** — Prevent unauthorized pickup and drop
- **Piston Protection** — Block pistons from pushing/pulling across claim borders
- **PvP Protection** — Disable PvP inside claims
- **Vehicle Protection** — Prevent unauthorized vehicle use and destruction
- **Command Blocking** — Block specific commands inside other players' claims
- **Wilderness Protection** — Opt-in `wildernessProtection.enabled` flag flips the default: players can only build/break inside claims, never in unclaimed chunks. Designed for clan/town-style servers.

### Customization
- **Owner Alias** — Set a custom display name to replace your Minecraft username in UI elements, map hooks, and titles
- **Claim Colors** — Pick a custom hex color for your claim used on maps and borders
- **Visualization Modes** — Toggle between Display Entity or Particle-based boundary rendering
- **Entry/Exit Titles** — Custom MiniMessage-formatted titles shown when players enter or leave your claim
- **Rename Claims** — Change your claim's display name at any time
- **Full Message Customization** — All plugin messages are configurable via Okaeri Configs

## 🔌 Supported Integrations

LandClaimPlugin seamlessly hooks into various popular server plugins to provide an interconnected experience out-of-the-box.

### 🗺️ Web Map Plugins
Visualize claims directly on your server's web map! Claims are drawn as colored polygons matching the claim's chosen color, complete with owner and size information.
- **BlueMap**
- **Dynmap**
- **Squaremap**
- **Pl3xMap**

### ⚔️ Combat Tagger Plugins
Prevent players from abusing claim commands (like teleporting or abandoning claims) while actively in combat. The plugin automatically detects when a player is combat-tagged.
- **DeluxeCombat**
- **PvPManager**
- **EternalCombat**

### 🛡️ WorldGuard
Respect WorldGuard regions when players attempt to claim land.
- **Volumetric Intersection Check:** 100% accurate 3D boundary checking to prevent players from claiming over your spawn or protected areas.
- **Gap Enforcement:** Configurable required distance between claims and WorldGuard regions.
- **Custom Flag Support:** Apply the custom `allow-land-claims` WorldGuard state flag to regions (e.g., Wilderness/Warzone) to explicitly allow players to claim land inside them, overriding the gap and intersection protections!

### 🎮 Bedrock / Geyser Support
Bedrock players get a tailored UI when Geyser 2.x is installed. No hard Geyser dependency — the plugin uses reflection to detect the form API and falls back to the Java-only chat prompts if Geyser is missing.
- **CustomForm text input** for anvil-based prompts (rename, color, role name)
- **ModalForm confirmation** for destructive actions (abandon, unclaim all, ban)
- Visualization automatically switches to particle mode for Bedrock clients

### Technical
- **Cloud Command Framework** — Async command execution with dedicated thread pools
- **Modular Architecture** — Separate command groups, listeners, and managers
- **Eager Class Preloading** — Eliminates first-execution lag spikes for GUI classes

---

## 🧩 Addons

The plugin's public API (`LandClaimAPI`) and event bus (`ClaimCreateEvent`,
`ClaimDeleteEvent`, `PlayerEnterClaimEvent`, `PlayerLeaveClaimEvent`) let
you build standalone addons in a sibling Maven project. Soft-depend on
LandClaimPlugin so the addon only loads when the parent is present.

### Official Addons

| Addon | Description | Repository |
|---|---|---|
| **LandClaimPlugin-Economy** | Charge for claiming, warps, and member invites; per-chunk daily tax with auto-unclaim; server-wide claim marketplace + time-limited auctions with a GUI browser. Vault-based. | [synkfr/LandClaimPlugin-Economy](https://github.com/synkfr/LandClaimPlugin-Economy) |

### Building Your Own

The API exposes:
- `getClaimAt(...)`, `getClaimsByOwner(UUID)`, `getClaimByName(String)`, `getClaimById(UUID)`
- `getAllClaimProfiles()` — every loaded claim, for global iteration (tax, leaderboards, etc.)
- `transferClaim(Player actor, UUID profileId, UUID newOwnerId)` and `unclaimAll(Player actor, UUID profileId)` — programmatic ownership / claim management (the actor is either the new owner or has `landclaim.admin`)
- `addBonusBlocks(UUID, int)` and `getBonusBlocks(UUID)` — for paid-claim-block plugins
- `isInCombat(Player)` — hook for combat-taggers
- The full claim profile model — `getOwnedChunks()`, `getRoles()`, `getTrustedPlayerFlags()`, etc.

For addons that want to act on claim lifecycle, listen for
`org.ayosynk.landClaimPlugin.api.event.*` events.

---

## 📥 Installation

1. Download the latest release and drop `LandClaimPlugin.jar` into your `plugins/` folder.
2. Restart your server.
3. Configure `plugins/LandClaimPlugin/config.yml` to your liking.

> **Requirements:** Paper 1.21.4+ | Java 21+

---

## 💬 Commands

*All commands use `/claim` (alias: `/c`) as the base.*

### General Commands
| Command | Description |
|---|---|
| `/claim` | Claim the chunk you're standing in |
| `/claim profiles` | Open the active profile selector (if Multi-Profile is enabled) |
| `/claim create <name>` | Create a new claim profile with the given name |
| `/claim auto` | Toggle auto-claim mode (claim chunks as you walk) |
| `/claim menu` | Open the main claim management GUI |
| `/claim info` | View info about the claim at your location |
| `/claim visible` | Toggle claim boundary visualization |
| `/claim toggle <mode>` | Switch visualization mode (`display_entities`, `particles`, `off`) |
| `/claim visualization <mode>` | Alias for `/claim toggle` |
| `/claim rename <name>` | Rename your active claim profile (3-32 alphanumeric characters) |
| `/claim color <color>` | Change claim color using a named color or hex code (e.g., `red`, `#FF5500`) |
| `/claim unstuck` | Safely teleport to the nearest wilderness block if trapped |
| `/claim abandon` | Delete your entire active claim profile and all its chunks |
| `/claim unclaimall` | Unclaim all chunks and delete your active profile (requires confirmation) |
| `/claim pvp <on/off> [time]` | Toggle PvP globally in the claim, with an optional time duration in seconds |
| `/unclaim` | Unclaim the chunk you're standing in |
| `/unclaim all` | Unclaim all chunks belonging to your active profile |

### Menu Shortcuts

Jump directly to specific GUI panels without navigating through the main menu.

| Command | Description |
|---|---|
| `/claim menu settings` | Open claim settings (color, PvP, visibility, etc.) |
| `/claim menu members` | Open member management |
| `/claim menu roles` | Open role management |
| `/claim menu trusted` | Open trusted player management |
| `/claim menu visitors` | Open visitor settings |
| `/claim menu allies` | Open ally management |
| `/claim menu map` | Open the claim map |
| `/claim menu warps` | Open warp management |

### Management Commands
<details>
<summary><b>Members</b></summary>

| Command | Description |
|---|---|
| `/claim member invite <player>` | Invite a player to join your claim |
| `/claim member kick <player>` | Remove a member from your claim |
| `/claim member list` | List all members and their roles |
| `/claim accept <name>` | Accept a pending member invitation |
| `/claim deny <name>` | Deny a pending member invitation |
</details>

<details>
<summary><b>Trust</b></summary>

| Command | Description |
|---|---|
| `/claim trust invite <player>` | Send a trust invitation to a player |
| `/claim trust accept` | Accept a pending trust invitation |
| `/claim trust deny` | Deny a pending trust invitation |
| `/claim trust remove <player>` | Remove a trusted player |
| `/claim trust list` | List all trusted players and their flags |
</details>

<details>
<summary><b>Allies</b></summary>

| Command | Description |
|---|---|
| `/claim ally invite <name>` | Send an alliance request to another claim |
| `/claim ally accept <name>` | Accept an alliance request |
| `/claim ally deny <name>` | Deny an alliance request |
| `/claim ally remove <name>` | Remove an existing alliance |
</details>

<details>
<summary><b>Warps & Admin</b></summary>

| Command | Description |
|---|---|
| `/claim setwarp <name>` | Set a warp at your current location |
| `/claim delwarp <name>` | Delete a warp |
| `/claim warp <name>` | Teleport to a warp |
| `/claim warps` | Open the warp management GUI |
| `/claim admin check` | View detailed claim info (owner UUID, profile name) |
| `/claim admin unclaim` | Force-unclaim the chunk you're standing in |
| `/claim admin edit <player>` | Open any player's claim management GUI |
| `/claim admin add chunk <player> <amount>` | Add bonus claim chunks to a player's limit |
| `/claim admin setalias <claim> <alias>` | Set or reset an owner's custom alias |
| `/claim admin trust list <owner>` | List players trusted by this owner |
| `/claim admin trust who <player>` | List claims where this player is trusted |

---

## 🔐 Permissions

| Permission | Description | Default |
|---|---|---|
| `landclaim.*` | All LandClaim permissions | `false` |
| `landclaim.claim` | Basic claiming ability | ✅ `true` |
| `landclaim.auto` | Use auto-claim mode | ✅ `true` |
| `landclaim.admin` | Admin commands & bypass all protection | `op` |
| `landclaim.limit.<n>` | Override the chunk claim limit (e.g., `landclaim.limit.50`) | `false` |
| `landclaim.list` | List claims | ✅ `true` |
| `landclaim.warps.limit.<n>` | Override the warps limit (e.g., `landclaim.warps.limit.10`) | `false` |
| `landclaim.createrole.<n>` | Override the max number of custom roles | `false` |

---

## 📊 bStats

This plugin collects **anonymous** usage statistics via [bStats](https://bstats.org/plugin/bukkit/LandClaimPlugin/28407). The data is publicly visible and helps us understand how the plugin is used across servers. No personal or identifiable information is collected.

*To opt out, navigate to `plugins/bStats/config.yml` and set `enabled` to `false`.*

---

<div align="center">

### 🔗 Quick Links

[![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.gg/fGyDyp3Ak4)
[![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)](https://github.com/synkfr/LandClaimPlugin)
[![Issues](https://img.shields.io/badge/Issues-EA4335?style=flat-square&logo=github&logoColor=white)](https://github.com/synkfr/LandClaimPlugin/issues)
[![bStats](https://img.shields.io/badge/bStats-313131?style=flat-square&logo=chart-dot&logoColor=white)](https://bstats.org/plugin/bukkit/LandClaimPlugin/28407)

</div>
