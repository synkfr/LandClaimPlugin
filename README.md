# 💠 LandClaim Plugin - Ultimate Territory Protection for Minecraft

![LandClaim Banner](https://i.postimg.cc/jS6mh13k/minecraft-title-2.png)

**LandClaimPlugin** is a premium land protection plugin that empowers players to claim territory, protect their builds, and manage access with powerful features. With robust protection against griefing, PvP, and mob damage, plus advanced claim management tools, LandClaim provides the ultimate territory control solution for your server.

## 🌟 Key Features

* Chunk-based claims (16x16)
* Auto-claim / Auto-unclaim while walking
* Trust system for friends/teammates
* Claim limit per player
* Block worlds (disable claim in nether, etc.)
* Block certain commands in claimed chunks
* `/claim unstuck` for getting out of other’s claims
* PvP, mob damage, and explosion protection
* Admin tools to unclaim others' land
* Claim visualizer (via particles)
* Live config reload (`/claim reload`)
* Auto-saving of claims/trusts

## 📥 Installation

1. Download the latest version of LandClaim
2. Place the `LandClaimPlugin.jar` in your server's `plugins` folder
3. Restart your server
4. Configure settings in `plugins/LandClaim/config.yml`
5. Use `/claim help` in-game to get started

**note**: if your updating to 1.2 to 1.3 Read This [Here](https://modrinth.com/plugin/landclaimplugin/version/1.3)

## ⚙️ Configuration

Customize LandClaimPlugin to fit your server's needs through `config.yml`:

```yaml

config-version: 1

#    ██╗      █████╗ ███╗  ██╗██████╗          █████╗ ██╗      █████╗ ██╗███╗   ███╗ ██████╗
#    ██║     ██╔══██╗████╗ ██║██╔══██╗        ██╔══██╗██║     ██╔══██╗██║████╗ ████║██╔════╝
#    ██║     ███████║██╔██╗██║██║  ██║        ██║  ╚═╝██║     ███████║██║██╔████╔██║╚█████╗
#    ██║     ██╔══██║██║╚████║██║  ██║        ██║  ██╗██║     ██╔══██║██║██║╚██╔╝██║ ╚═══██╗
#    ███████╗██║  ██║██║ ╚███║██████╔╝        ╚█████╔╝███████╗██║  ██║██║██║ ╚═╝ ██║██████╔╝
#    ╚══════╝╚═╝  ╚═╝╚═╝  ╚══╝╚═════╝          ╚════╝ ╚══════╝╚═╝  ╚═╝╚═╝╚═╝     ╚═╝╚═════╝
# claims.yml contains player's claims with their uuid and the chunk coordinates (you can see chunk coordinates in F3 menu)
# trust.yml contains player's uuid with other players uuid who they have trusted with the /claim trust command

# 𝖢𝖫𝖠𝖨𝖬 𝖲𝖤𝖳𝖳𝖨𝖭𝖦𝖲
# Set how many claims a player can have.
chunk-claim-limit: 100
# If set to false player can claim anywhere even if it's not connected to the previous claim.
# It's recommended to set this to true.
require-connected-claims: true
# If set to true then player can claim diagonally.
# If require-connected-claims is set to true then player can claim lands diagonally if this is set to true.
allow-diagonal-connections: false

# 𝖶𝖮𝖱𝖫𝖣 𝖱𝖤𝖲𝖳𝖱𝖨𝖢𝖳𝖨𝖮𝖭𝖲
# Block claims in certain worlds.
# If you put the world name in the list then player can't claim in that world.
# It's useful if you use multiverse-core plugin or if you have multiple worlds.
block-world:
  - world_nether
  - world_the_end
# - lobby
# - spawn
# - mining_world

# 𝖢𝖮𝖬𝖬𝖠𝖭𝖣𝖲 𝖱𝖤𝖲𝖳𝖱𝖨𝖢𝖳𝖨𝖮𝖭𝖲 𝖲𝖤𝖳𝖳𝖨𝖭𝖦𝖲
# This lets you block commands inside a claim.
# Claim owner and trusted player still can use the commands.
block-cmd:
  - sethome
  - setwarp

# 𝖯𝖱𝖮𝖳𝖤𝖢𝖳𝖨𝖮𝖭𝖲 𝖲𝖤𝖳𝖳𝖨𝖭𝖦𝖲
# If set to false player can pvp inside the claims.
prevent-pvp: true
# If set to true mobs can't grief inside your claims.
# For example a creeper can damage your build if this is set to false
prevent-mob-griefing: true
prevent-explosion-damage: true

# 𝖠𝖴𝖳𝖮 𝖢𝖫𝖠𝖨𝖬 𝖲𝖤𝖳𝖳𝖨𝖭𝖦𝖲
# If set to true auto-claim will be active by default
# It's recommended to set this false
# or use it  if you have something in your mind
auto-claim-default: false
# If set to true auto-unclaim will be active by default
# It's highly recommend to set this false
# or use it  if you have something in your mind
auto-unclaim-default: false

# 𝖴𝖭𝖲𝖳𝖴𝖢𝖪 𝖲𝖤𝖳𝖳𝖨𝖭𝖦
# Cooldown for /claim unstuck command
cooldown-unstuck: 30  # seconds


# 𝘝𝘐𝘚𝘜𝘈𝘓𝘐𝘡𝘈𝘛𝘐𝘖𝘕 𝘚𝘌𝘛𝘐𝘕𝘎𝘚
visualization:
  always-color: "0,255,0"  # Green
  temporary-color: "255,255,0"  # Yellow
  particle-spacing: 0.5
  update-interval: 20  # Ticks between updates for always-on mode

# 𝖤𝖣𝖨𝖳 𝖬𝖤𝖲𝖲𝖠𝖦𝖤𝖲

# Messages (supports color codes with '&')
prefix: "&8[&6✦ LandClaim ✦&8]&r "

messages:
  chunk-claimed: "&a✔ Chunk claimed successfully!"
  chunk-unclaimed: "&a✖ Chunk unclaimed!"

  auto-claim-enabled: "&b» &aAuto-Claim enabled. Walk to claim chunks."
  auto-claim-disabled: "&b» &cAuto-Claim disabled."
  auto-unclaim-enabled: "&b» &aAuto-Unclaim enabled. Walk to unclaim chunks."
  auto-unclaim-disabled: "&b» &cAuto-Unclaim disabled."
  auto-unclaimed: "&e⚠ Chunk auto-unclaimed!"

  claim-limit-reached: "&c⛔ Claim limit reached: &7({limit} chunks)"
  already-claimed: "&c⚠ This chunk is already claimed by &6{owner}&c."
  not-owner: "&c⛔ You don't own this land."
  not-connected: "&c❌ Chunks must be adjacent to existing claims."

  player-trusted-all: "&a✔ Added &e{player} &ato all trusted claims."
  player-untrusted-all: "&a✔ Removed &e{player} &afrom all trusted claims."
  trust-usage: "&cUsage: &e/claim trust <player>"
  untrust-usage: "&cUsage: &e/claim untrust <player>"
  player-not-trusted: "&c⚠ That player is not trusted!"
  cannot-trust-self: "&c⚠ You cannot trust yourself."

  player-not-found: "&c⚠ Player not found!"

  access-denied: "&c⛔ You don’t have permission to build here!"
  access-denied-interact: "&c⛔ You can’t interact with that here!"
  bucket-denied: "&c🚫 Fluid placement is blocked in claimed land."
  command-blocked: "&c🚫 This command is restricted in claimed land."
  mob-grief-denied: "&c👾 Mob griefing is disabled here."
  pvp-denied: "&c⚔ PvP is not allowed in claimed land."
  world-blocked: "&c🌍 Claiming is disabled in this world."

  unstuck-success: "&a✔ You’ve been teleported to a safe location!"
  cannot-unstuck-here: "&c⚠ You can only use this when stuck in someone else's claim."
  unstuck-cooldown: "&c⏳ Wait &e{seconds}s &cbefore using again."

  reloaded: "&a✔ Configuration reloaded successfully."

  visible-enabled-always: "&a📍 Claim visualization &f- &aAlways ON"
  visible-enabled-temporary: "&a📍 Claim visualization &f- &aEnabled"
  visible-disabled: "&c📍 Claim visualization &f- &cDisabled"

  help-header: "&6&l------ LandClaim Help ------"
  help-claim: "&e/claim &7- Claim your current chunk"
  help-unclaim: "&e/unclaim &7- Unclaim your current chunk"
  help-claim-auto: "&e/claim auto &7- Toggle Auto-Claim"
  help-unclaim-auto: "&e/unclaim auto &7- Toggle Auto-Unclaim"
  help-trust: "&e/claim trust <player> &7- Trust a player"
  help-untrust: "&e/claim untrust <player> &7- Untrust a player"
  help-trustlist: "&e/claim trustlist &7- View trusted players"
  help-unstuck: "&e/claim unstuck &7- Escape another's claim"
  help-visible: "&e/claim visible [always|off] &7- Toggle visuals"
  help-info: "&e/claim info &7- Show claim details"
  help-admin: "&e/claim admin unclaim|unclaimall &7- Admin tools"
  help-unclaimall: "&e/unclaim all confirm &7- Remove all claims"
  invalid-command: "&c⚠ Invalid command. Use &e/claim help &cfor help."

  admin-unclaimed: "&a✔ Admin unclaimed chunk from &e{owner}&a."
  admin-unclaimed-all: "&a✔ Admin unclaimed &e{count} &achunks from &e{player}&a."

  unclaimed-all: "&a✔ You unclaimed &e{count} &achunks."
  confirm-unclaimall: "&c⚠ Confirm with &4/unclaim all confirm &cto remove all claims!"

  trust-list-header: "&6☰ Trusted Players:"
  trust-list-item: "&7• &e{player}"
  no-trusted-players: "&e⚠ You haven't trusted any players."

  claim-info-owner: "&e🏷 Owner: &a{owner}"
  claim-info-trusted: "&e🤝 Trusted: &a{players}"
  claim-info-not-claimed: "&c⚠ This chunk is not claimed!"

  actionbar-own: "&a✔ Your Claim"
  actionbar-trusted: "&e✔ Trusted in &6{owner}&e's Claim"
  actionbar-admin: "&c⚠ Admin View: {owner}&c's Claim"


```

## 📋 Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/claim` | Claim current chunk | `landclaim.claim` |
| `/claim auto` | Toggle auto-claim mode | `landclaim.auto` |
| `/claim trust <player>` | Trust a player | `landclaim.claim` |
| `/claim untrust <player>` | Remove trust | `landclaim.claim` |
| `/claim trustlist` | see who is trusted | `landclaim.claim` |
| `/claim unstuck` | Escape from others' claims | `landclaim.claim` |
| `/claim help` | Show help information | `landclaim.claim` |
| `/unclaim` | Unclaim current chunk | `landclaim.claim` |
| `/unclaim auto` | Toggle auto-unclaim | `landclaim.auto` |_-
| `/unclaim visble [always/off]` | Toggle visibility of the claims | `landclaim.claim` |
| `/unclaim all confirm` | Unclaims all claims | `landclaim.claim` |


### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/claim reload` | Reload configuration | `landclaim.admin` |
| `/claim admin unclaim` | Unclaim other players claim | `landclaim.admin` |
| `/claim admin unclaimall <player>` | Unclaim other players claim | `landclaim.admin` |

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `landclaim.claim` | Basic claiming permission | true |
| `landclaim.admin` | Bypass protection and limits | op |
| `landclaim.auto` | Use auto-claim feature | true |
| `landclaim.limit.<number>` | Custom claim limit | false |

## 🛡️ Protection Features

LandClaim offers comprehensive protection for claimed areas:

* ✅ Block breaking/placing protection
* 🔐 Container & interaction lock
* 🚫 PvP block inside claims
* 💥 Explosion protection
* 🧟 Mob griefing block
* 📛 Block specific commands in claims
* 👥 Trust-based team claiming
* 🚷 Unstuck system (with cooldown)

## 🆘 Unstuck Command

The `/claim unstuck` command helps players escape from others' claims:

- Teleports player to nearest safe unclaimed location
- 30-second cooldown (configurable)
- Only works when trapped in others' claims
- Prevents exploits with strict usage rules

```bash
/claim unstuck
> You've been teleported to a safe location!
```

## 📊 Claim Data Storage

Claims are stored in an efficient player-centric format:

```yaml
claims:
  player-uuid-1:
    - "world,10,20"
    - "world,10,21"
  player-uuid-2:
    - "nether,5,-30"
```

## ❓ Support

For support, bug reports, or feature requests:  
[![Discord](https://img.shields.io/discord/1378591879393710110?style=for-the-badge)](https://discord.gg/pAPPvSmWRK)  
[![GitHub Issues](https://img.shields.io/github/issues/synkfr/LandClaimPlugin?style=for-the-badge)](https://github.com/synkfr/LandClaimPlugin/issues)

## 📜 License

LandClaim is licensed under the MIT License.

```license
Copyright 2023 AyoSynk

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

**LandClaim** - The Ultimate Territory Protection Solution for Minecraft Servers!  
*Protect your land, control access, and build with confidence.*
