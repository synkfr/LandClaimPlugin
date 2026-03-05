# 💠 LandClaim — Advanced Territory Protection for Minecraft

![LandClaim Banner](https://i.postimg.cc/jS6mh13k/minecraft-title-2.png)

A powerful, feature-rich chunk-based land protection plugin for Paper servers. Create claim profiles, protect your builds, manage members with granular roles, form alliances, set warps, and customize every aspect of your territory.

[![Paper](https://img.shields.io/badge/Paper-1.21+-blue)](https://papermc.io/)
[![Version](https://img.shields.io/badge/Version-2.0.0-brightgreen)](https://github.com/synkfr/LandClaimPlugin/releases)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Discord](https://img.shields.io/discord/1378591879393710110?label=Discord&logo=discord&color=5865F2)](https://discord.gg/pAPPvSmWRK)

---

## ✨ Features

### Core Claiming
- **Claim Profiles** — Each player owns a single named claim profile that holds all their claimed land, settings, and permissions
- **Chunk-Based Claims** — 16×16 block protection zones, simple and intuitive
- **Auto-Claim** — Automatically claim chunks as you walk
- **Connected Claims** — Optionally require claims to be adjacent (with diagonal support)
- **Claim Map GUI** — Interactive in-game map showing nearby claims and ownership

### Permission System
- **4-Tier Priority Chain** — `Owner > Role > Trusted > Visitor` — the first matching tier decides
- **Custom Roles** — Create and configure custom roles (e.g., Member, CoOwner) with granular flag-based permissions
- **Trusted Players** — Grant individual players specific permission overrides
- **Visitor Settings** — Configure what non-members can do in your claims
- **25+ Permission Flags** — Doors, trapdoors, containers, workstations, animals, vehicles, redstone, and more

### Social Systems
- **Member System** — Invite players to join your claim, assign them roles, and manage access
- **Ally System** — Form mutual alliances between claims with configurable inter-claim permissions
- **Warp System** — Set named warps within your claims with custom icons and per-player limits

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

### Customization
- **Claim Colors** — Pick a custom hex color for your claim used on maps and borders
- **Visualization Modes** — Toggle between Display Entity or Particle-based boundary rendering
- **Entry/Exit Titles** — Custom MiniMessage-formatted titles shown when players enter or leave your claim
- **Rename Claims** — Change your claim's display name at any time
- **Full Message Customization** — All plugin messages are configurable via Okaeri Configs

### Integrations
- **Database** — SQLite (default) or MySQL with HikariCP connection pooling
- **Caching** — Caffeine-powered in-memory cache for instant lookups
- **Redis** — Optional cross-server claim sync for networks
- **Map Plugins** — Dynmap, BlueMap, Squaremap, and Pl3xMap support with customizable colors and opacity
- **Combat Plugins** — DeluxeCombat, PvPManager, and EternalCombat hook for combat tagging (blocks commands while in combat)
- **WorldGuard** — Configurable gap enforcement between claims and WorldGuard regions

### Technical
- **Cloud Command Framework** — Async command execution with dedicated thread pools
- **Modular Architecture** — Separate command groups, listeners, and managers
- **Eager Class Preloading** — Eliminates first-execution lag spikes for GUI classes

---

## 📥 Installation

1. Drop `LandClaimPlugin.jar` into your `plugins/` folder
2. Restart the server
3. Configure `plugins/LandClaimPlugin/config.yml` to your liking

**Requirements:** Paper 1.21+, Java 21+

---

## 💬 Commands

All commands use `/claim` (alias: `/c`) as the base.

### General

| Command | Description |
|---|---|
| `/claim` | Claim the chunk you're standing in |
| `/claim create <name>` | Create a new claim profile with the given name |
| `/claim auto` | Toggle auto-claim mode (claim chunks as you walk) |
| `/claim menu` | Open the main claim management GUI |
| `/claim info` | View info about the claim at your location |
| `/claim visible` | Toggle claim boundary visualization |
| `/claim abandon` | Delete your entire claim profile and all claimed chunks |
| `/unclaim` | Unclaim the chunk you're standing in |
| `/unclaim all` | Unclaim all your chunks |

### Members

| Command | Description |
|---|---|
| `/claim member invite <player>` | Invite a player to join your claim |
| `/claim member kick <player>` | Remove a member from your claim |
| `/claim member list` | List all members and their roles |
| `/claim accept <name>` | Accept a pending member invitation |
| `/claim deny <name>` | Deny a pending member invitation |

### Trust

| Command | Description |
|---|---|
| `/claim trust add <player>` | Trust a player (per-player permission overrides) |
| `/claim trust remove <player>` | Remove a trusted player |
| `/claim trust list` | List all trusted players and their flags |

### Allies

| Command | Description |
|---|---|
| `/claim ally invite <name>` | Send an alliance request to another claim |
| `/claim ally accept <name>` | Accept an alliance request |
| `/claim ally deny <name>` | Deny an alliance request |
| `/claim ally remove <name>` | Remove an existing alliance |

### Warps

| Command | Description |
|---|---|
| `/claim setwarp <name>` | Set a warp at your current location |
| `/claim delwarp <name>` | Delete a warp |
| `/claim warp <name>` | Teleport to a warp |
| `/claim warps` | Open the warp management GUI |

### Admin

| Command | Description |
|---|---|
| `/claim admin check` | View detailed claim info (owner UUID, profile name) |
| `/claim admin unclaim` | Force-unclaim the chunk you're standing in |

---

## 🔐 Permissions

| Permission | Description | Default |
|---|---|---|
| `landclaim.*` | All LandClaim permissions | false |
| `landclaim.claim` | Basic claiming ability | ✅ true |
| `landclaim.auto` | Use auto-claim mode | ✅ true |
| `landclaim.admin` | Admin commands & bypass all protection | op |
| `landclaim.limit.<n>` | Override the chunk claim limit (e.g., `landclaim.limit.50`) | false |
| `landclaim.list` | List claims | ✅ true |
| `landclaim.warps.limit.<n>` | Override the warps limit (e.g., `landclaim.warps.limit.10`) | false |
| `landclaim.createrole.<n>` | Override the max number of custom roles a player can create | false |

---

## 📊 bStats

This plugin collects **anonymous** usage statistics via [bStats](https://bstats.org/plugin/bukkit/LandClaimPlugin/28407). The data is publicly visible and helps us understand how the plugin is used across servers. No personal or identifiable information is collected.

To opt out, navigate to `plugins/bStats/config.yml` and set `enabled` to `false`.

---

## 🔗 Links

- **Discord** — [discord.gg/pAPPvSmWRK](https://discord.gg/pAPPvSmWRK)
- **GitHub** — [github.com/synkfr/LandClaimPlugin](https://github.com/synkfr/LandClaimPlugin)
- **Issues** — [GitHub Issues](https://github.com/synkfr/LandClaimPlugin/issues)
- **bStats** — [bstats.org/plugin/bukkit/LandClaimPlugin/28407](https://bstats.org/plugin/bukkit/LandClaimPlugin/28407)
