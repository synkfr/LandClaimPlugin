# Installation

## Requirements

- **Paper 1.21.4+** (or any Paper fork like Purpur)
- **Java 21+**

## Quick Start

1. Download the latest release from [GitHub Releases](https://github.com/synkfr/LandClaimPlugin/releases)
2. Drop `LandClaimPlugin.jar` into your server's `plugins/` folder
3. Restart your server
4. Configure `plugins/LandClaimPlugin/config.yml` to your liking

::: tip
The plugin generates all configuration files on first startup. Edit them and use `/claim admin reload` to apply changes without restarting.
:::

::: warning DEVELOPMENT BUILDS
Development builds are automatically generated from the latest commits and may include unfinished features or breaking changes. Use them only for testing.
:::

---

## Features

### Core Claiming
- **Claim Profiles** — Manage your land using claim profiles. Depending on server configuration, players can own a single profile or utilize the **Multi-Profile System** to manage multiple independent bases.
- **Chunk-Based Claims** — 16×16 block protection zones, simple and intuitive.
- **Auto-Claim** — Automatically claim chunks as you walk.
- **Connected Claims** — Optionally require claims to be adjacent (with diagonal support).
- **Claim Map GUI** — Interactive in-game map showing nearby claims and ownership.
- **Unstuck Command** — A safe `/claim unstuck` feature that teleports trapped players to the nearest safe wilderness block.

### Permission System
- **4-Tier Priority Chain** — Owner › Role › Trusted › Visitor — the first matching tier decides
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
- **Owner Alias** — Set a custom display name to replace your Minecraft username
- **Claim Colors** — Pick a custom hex color for your claim used on maps and borders
- **Visualization Modes** — Toggle between Display Entity or Particle-based boundary rendering
- **Entry/Exit Titles** — Custom MiniMessage-formatted titles shown when players enter or leave
- **Rename Claims** — Change your claim's display name at any time
- **Full Message Customization** — All plugin messages are configurable via YAML

---

## Supported Integrations

### Web Map Plugins
Visualize claims directly on your server's web map as colored polygons with owner info.
- **BlueMap** · **Dynmap** · **Squaremap** · **Pl3xMap**

### Combat Tagger Plugins
Prevent players from abusing claim commands while in combat.
- **DeluxeCombat** · **PvPManager** · **EternalCombat**

### WorldGuard
- **Volumetric Intersection Check** — 100% accurate 3D boundary checking
- **Gap Enforcement** — Configurable required distance between claims and WorldGuard regions
- **Custom Flag Support** — Apply the `allow-land-claims` flag to regions to explicitly allow claiming
