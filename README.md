# 💠 LandClaim Plugin - Ultimate Territory Protection for Minecraft

![LandClaim Banner](https://i.postimg.cc/jS6mh13k/minecraft-title-2.png)

Chunk-based land protection for Minecraft servers. Claim territory, protect builds, manage access.

[![Spigot](https://img.shields.io/badge/Spigot-1.21-orange)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![Discord](https://img.shields.io/discord/1378591879393710110?label=Discord)](https://discord.gg/pAPPvSmWRK)

## Features

- **Chunk-based claims** - 16x16 block protection zones
- **Trust system** - Grant BUILD, INTERACT, CONTAINER, TELEPORT permissions
- **Auto-claim** - Claim chunks while walking
- **Particle visualization** - See claim boundaries
- **Protection** - Block breaking, PvP, explosions, mob griefing
- **Admin tools** - Manage any player's claims
- **WorldGuard support** - Maintain gaps between claims and regions

## Installation

1. Drop `LandClaimPlugin.jar` into `plugins/`
2. Restart server
3. Configure `plugins/LandClaimPlugin/config.yml`

**Requirements:** Spigot/Paper 1.21+, Java 21+

## Commands

| Command | Description |
|---------|-------------|
| `/claim` | Claim current chunk |
| `/claim auto` | Toggle auto-claim |
| `/claim trust <player>` | Trust a player |
| `/claim untrust <player>` | Remove trust |
| `/claim trustlist` | Open trust GUI |
| `/claim visitor menu` | Manage visitor permissions |
| `/claim info` | View chunk info |
| `/claim visible [always/off]` | Toggle visualization |
| `/claim unstuck` | Escape others' claims |
| `/unclaim` | Unclaim current chunk |
| `/unclaim all confirm` | Unclaim everything |
| `/c`, `/uc` | Aliases for claim/unclaim |

**Admin:** `/claim reload`, `/claim admin unclaim`, `/claim admin unclaimall <player>`

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `landclaim.claim` | Basic claiming | true |
| `landclaim.auto` | Auto-claim mode | true |
| `landclaim.admin` | Admin commands | op |
| `landclaim.limit.<n>` | Custom claim limit | false |

## Configuration

```yaml
# Key settings in config.yml
chunk-claim-limit: 100
require-connected-claims: true
prevent-pvp: true
prevent-mob-griefing: true
prevent-explosion-damage: true

# Blocked worlds
block-world:
  - world_nether
  - world_the_end

# Trust permissions (default for new trusted players)
default-trust-permissions:
  BUILD: true
  INTERACT: true
  CONTAINER: true
  TELEPORT: true
```

All messages customizable in `messages.yml`.

## Data Storage

Claims stored in `claims.yml`, trust data in `trust.yml`:

```yaml
# claims.yml
claims:
  550e8400-e29b-41d4-a716-446655440000:
    - "world,10,20"
    - "world,10,21"
```

## Support

- [Discord](https://discord.gg/pAPPvSmWRK)
- [GitHub Issues](https://github.com/synkfr/LandClaimPlugin/issues)

