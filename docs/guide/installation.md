# Installation

## Requirements

- **Paper 26.2+** (or Folia / Paper forks)
- **Java 25+**

## Quick start

1. Download the release JAR from [GitHub Releases](https://github.com/synkfr/LandClaimPlugin/releases) or [Modrinth](https://modrinth.com/plugin/landclaimplugin).
2. Drop `LandClaimPlugin.jar` into your server's `plugins/` folder.
3. Restart your server.
4. Edit `plugins/LandClaimPlugin/config.yml`, `plugins/LandClaimPlugin/locales/messages_<lang>.yml`, and menus under `plugins/LandClaimPlugin/menus/`.

::: tip Upgrading from 3.4.0
Updating to `3.5.0` automatically creates `.bk` backups (`config.yml.bk`, `locales/<message>.yml.bk`, and `menus/<menu>.yml.bk`) before migrating flat keys into grouped sections and refreshing menu files with the Obsidian and Cobalt GUI defaults. Run `/claim admin reload` after editing any YAML file.
:::

---

## Features

### Core claiming
- **Claim profiles.** Players use a single claim profile by default or enable Multi-Profile mode to manage independent bases through an interactive GUI.
- **Chunk and radius claims.** 16x16 block protection zones with bulk `/claim radius <1-5>` support.
- **Auto-claim and connected claims.** Automatically claim chunks while walking and optionally require claims to touch existing territory.
- **Held territory map.** Live 128x128 pixel radar map in hand, off-hand, or item frames.
- **Boundary visualization.** Range-culled Display Entities (within 64 blocks on loaded chunks) or Particle borders, switching to particles automatically for Bedrock clients.
- **Unstuck.** `/claim unstuck` asynchronously searches for the nearest safe wilderness location.

### Permission system
- **Four-tier priority chain.** `Owner > Role > Trusted > Visitor`. The first matching tier decides.
- **Custom roles.** Built-in `Member` and `CoOwner` roles plus player-defined roles with 39 permission flags.
- **Trusted players and visitor rules.** Grant per-player overrides or lock visitor behavior globally.

### Social and protection
- **Members, allies, bans, and warps.** Invite members, form claim alliances, permanently ban troublemakers from entering, and publish public or private claim warps.
- **Protections and claim toggles.** Block breaking, placing, containers, entities, pistons, vehicles, explosions, and PvP, plus per-claim TNT explosion and mob spawning toggles in `/claim menu settings`.
- **Wilderness protection and decay.** Optional protection in unclaimed land and automated cleanup of long-inactive claims.

## Integrations

- **Web map plugins.** Colored claim polygons on BlueMap, Dynmap, Squaremap, and Pl3xMap.
- **Combat taggers.** DeluxeCombat, CombatLogX, PvPManager, and EternalCombat prevent safe-zone escapes during combat.
- **WorldGuard.** 3D region intersection checks, chunk buffers, and the custom `allow-land-claims` state flag.

---

## Disclosures

### AI assistance
This project uses AI assistance tools for code drafting, refactoring, and documentation under direct maintainer architecture, review, and verification.

### External network interactions
- **Modrinth API.** Asynchronously checks `api.modrinth.com` on startup when `updateChecker.enabled: true`.
- **bStats analytics.** Collects anonymous server counts unless disabled in `plugins/bStats/config.yml`.
- **Redis and MySQL.** Connect only when enabled and configured by the server administrator.
