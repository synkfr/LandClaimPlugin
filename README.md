<div align="center">

<img src="https://i.postimg.cc/jS6mh13k/minecraft-title-2.png" alt="LandClaimPlugin banner" width="100%">

# LandClaimPlugin

**Chunk-based territory protection for Paper servers.**

Create claim profiles, protect builds and entities, configure custom roles and visitor rules, form alliances, set public or private warps, and render territories on your server web map or held radar map.

[![Paper](https://img.shields.io/badge/Paper-26.2+-blue?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Modrinth](https://img.shields.io/badge/Modrinth-LandClaimPlugin-00AF5C?style=flat-square&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/landclaimplugin)
[![Version](https://img.shields.io/github/v/release/synkfr/LandClaimPlugin?style=flat-square&color=40ae24&label=Version)](https://github.com/synkfr/LandClaimPlugin/releases)
[![License](https://img.shields.io/github/license/synkfr/LandClaimPlugin?style=flat-square&color=blue)](LICENSE.md)
[![Discord](https://img.shields.io/discord/1378591879393710110?color=5865F2&label=Discord&logo=discord&logoColor=white&style=flat-square)](https://discord.gg/UAgsmk6KEr)

[Documentation](https://synkfr.github.io/LandClaimPlugin/) | [Installation](https://synkfr.github.io/LandClaimPlugin/guide/installation) | [Commands](https://synkfr.github.io/LandClaimPlugin/guide/commands) | [Configuration](https://synkfr.github.io/LandClaimPlugin/CONFIGURATION)

</div>

---

## Features

### Core claiming
- **Claim profiles.** Give each player a single claim profile or enable Multi-Profile mode so players manage separate bases and switch active profiles through a GUI.
- **Chunk and radius claiming.** Claim single 16x16 chunks with `/claim` or bulk squares up to 5 chunks out with `/claim radius <1-5>`.
- **Auto-claim and auto-unclaim.** Toggle walking claim/unclaim with `/claim auto` and `/unclaim auto`, with optional connected-chunk and diagonal-adjacency enforcement.
- **Held territory map.** Render a live 128x128 pixel radar onto a held `FILLED_MAP` or item frame showing claim colors, alliances, and player heading.
- **Boundary visualization.** Switch between 64-block range-culled `display_entities`, `particles`, or `off`. Automatically falls back to particles for Bedrock clients.
- **Safe unstuck.** `/claim unstuck` runs an async search to teleport trapped players to the nearest safe wilderness block.

### Permissions and social
- **Four-tier priority chain.** Evaluates `Owner > Role > Trusted > Visitor` on the first matching tier.
- **Custom roles and 39 flags.** Configure built-in `Member` and `CoOwner` roles or add custom roles covering blocks, containers, workstations, animals, vehicles, items, and management menus.
- **Trust, members, allies, and bans.** Invite members and trusted builders via interactive player-head selectors, form inter-claim alliances, or ban troublemakers so they are turned back at chunk borders across restarts.
- **Public and private warps.** Create named claim warps, publish bases to `/claim publicwarps`, and handle duplicate warp names through a disambiguation menu.

### Protection and toggles
- **Per-claim toggles.** Control claim PvP (`/claim pvp <on/off> [seconds]`), TNT explosions, natural and spawner mob spawning, and enter/leave titles directly inside `/claim menu settings`.
- **World and explosion safety.** Block pistons crossing claim borders, creepers, enderman griefing, vehicle theft, item drops/pickups, and configured commands inside claims.
- **Wilderness protection.** Optional `wildernessProtection.enabled` mode denies build and interaction flags outside claims while letting players fight mobs or travel freely unless locked down further.
- **Combat tag bypass and auto-decay.** Prevents combat-tagged players from hiding behind claim PvP protection and optionally unclaims land from long-inactive players with VIP/staff exemptions.

## Supported integrations

- **Web maps.** BlueMap, Dynmap, Squaremap, and Pl3xMap render colored claim polygons with owner metadata.
- **Combat taggers.** DeluxeCombat, CombatLogX, PvPManager, and EternalCombat block combat-escape warps and disable PvP sanctuary while tagged.
- **WorldGuard.** 3D volumetric region intersection checks, configurable chunk buffers, and the custom `allow-land-claims` state flag.
- **Geyser and Floodgate.** Reflection-based Bedrock CustomForm and ModalForm dialogs for renames, colors, abandons, unclaim-all, and bans with zero hard dependency.
- **PlaceholderAPI and addons.** Exposes `landclaim` and `claimplugin` placeholders plus the `LandClaimAPI` interface and Bukkit lifecycle events used by addons like [LandClaimPlugin-Eco-Addon](https://github.com/synkfr/LandClaimPlugin-Eco-Addon).

## Installation

1. Download the release JAR from [GitHub Releases](https://github.com/synkfr/LandClaimPlugin/releases) or [Modrinth](https://modrinth.com/plugin/landclaimplugin).
2. Place `LandClaimPlugin.jar` into your server `plugins/` folder.
3. Restart your server (requires Paper 26.2+ or Folia on Java 25+).
4. Edit `plugins/LandClaimPlugin/config.yml`, `plugins/LandClaimPlugin/locales/messages_en-US.yml`, and menus under `plugins/LandClaimPlugin/menus/`, then run `/claim admin reload`.

Upgrading from 3.4.0 automatically backs up existing files to `config.yml.bk`, `locales/<message>.yml.bk`, and `menus/<menu>.yml.bk` before migrating YAML layouts and generating refreshed 3.5.0 menu defaults.

## Commands

All subcommands use `/claim` (alias `/c`) or `/unclaim`.

| Command | Description |
|---|---|
| `/claim` | Claim the chunk you are standing in |
| `/claim radius <1-5>` | Claim a square of chunks centered on your position |
| `/claim profiles` | Open the active profile selector GUI |
| `/claim create <name>` | Create a new claim profile |
| `/claim auto` | Toggle auto-claim as you walk |
| `/claim map` | Receive the held Territory Map (alias `/claim minimap`) |
| `/claim menu` | Open the main claim management GUI |
| `/claim menu <tab>` | Jump to `settings`, `members`, `roles`, `trusted`, `visitors`, `allies`, `map`, or `warps` |
| `/claim info` | Inspect the claim at your current chunk |
| `/claim visible` | Toggle claim boundary visualization |
| `/claim toggle <mode>` | Switch mode (`display_entities`, `particles`, `off`) |
| `/claim pvp <on/off> [time]` | Toggle claim PvP with optional duration in seconds |
| `/claim rename <name>` | Rename your active profile |
| `/claim color <color>` | Set claim color by name or hex code |
| `/claim ban <player>` | Ban a player from entering or interacting with your claim |
| `/claim unstuck` | Teleport to the nearest safe wilderness block |
| `/claim abandon` | Delete your active claim profile and all its chunks |
| `/unclaim` | Unclaim the current chunk |
| `/unclaim radius <1-5>` | Unclaim your chunks within a radius |
| `/unclaim all [confirm]` | Unclaim every chunk in your active profile |

See the full [Commands Reference](https://synkfr.github.io/LandClaimPlugin/guide/commands) for member, trust, ally, warp, and admin commands (`add/set/remove/reset chunk`, decay controls, and `/claim reload`).

## Permissions

| Permission | Description | Default |
|---|---|---|
| `landclaim.*` | Grants all LandClaimPlugin nodes | `false` |
| `landclaim.claim` | Claim land | `true` |
| `landclaim.minimap` | Obtain and use the held Territory Map | `true` |
| `landclaim.auto` | Toggle auto-claim mode | `true` |
| `landclaim.admin` | Admin commands and full claim/wilderness bypass | `op` |
| `landclaim.decay.exempt` | Exempt player claims from automatic decay | `op` |
| `landclaim.update.notify` | Receive update alerts on join | `op` |
| `landclaim.limit.<n>` | Override max claimed chunks (for example `landclaim.limit.50`) | `false` |
| `landclaim.warps.limit.<n>` | Override max claim warps (for example `landclaim.warps.limit.10`) | `false` |
| `landclaim.createrole.<n>` | Override max custom roles | `false` |

## Disclosures

- **AI assistance.** Parts of this project use AI coding and documentation tools under direct maintainer review, testing, and architectural ownership.
- **External network calls.** Startup version checking queries the public Modrinth v2 API (`api.modrinth.com`) unless `updateChecker.enabled: false`. Anonymous metrics report via bStats unless disabled in `plugins/bStats/config.yml`. SQLite runs locally; MySQL and Redis connect only when configured.
