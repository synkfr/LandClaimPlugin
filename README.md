# LandClaim - Ultimate Territory Protection for Minecraft

![LandClaim Banner]([https://i.imgur.com/vYb0W0m.png](https://i.postimg.cc/J0qzLndL/minecraft-title-2.png))

**LandClaimPlugin** is a premium land protection plugin that empowers players to claim territory, protect their builds, and manage access with powerful features. With robust protection against griefing, PvP, and mob damage, plus advanced claim management tools, LandClaim provides the ultimate territory control solution for your server.

## 🌟 Key Features

- **Chunk-Based Claiming**: Simple 16x16 chunk claiming system
- **Auto-Claim/Unclaim**: Automatically claim or unclaim chunks as you move
- **Trust Management**: Grant building permissions to trusted players
- **World Restrictions**: Block claiming in specific worlds
- **Command Restrictions**: Block commands in claimed land
- **Unstuck System**: Escape from others' claims safely
- **Comprehensive Protection**:
  - Block protection against griefing
  - PvP prevention in claims
  - Mob griefing protection
  - Explosion damage prevention
- **Connected Claims**: Optional adjacency requirements
- **Action Bar Display**: Shows claim ownership when entering chunks

## 📥 Installation

1. Download the latest version of LandClaim
2. Place the `LandClaimPlugin.jar` in your server's `plugins` folder
3. Restart your server
4. Configure settings in `plugins/LandClaim/config.yml`
5. Use `/claim help` in-game to get started

## ⚙️ Configuration

Customize LandClaimPlugin to fit your server's needs through `config.yml`:

```yaml

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

# 𝖤𝖣𝖨𝖳 𝖬𝖤𝖲𝖲𝖠𝖦𝖤𝖲
# Messages (supports color codes with '&')
messages:
  chunk-claimed: "&aChunk claimed successfully!"
  chunk-unclaimed: "&aChunk unclaimed!"
  auto-claim-enabled: "&aAuto-claim enabled. Walk to claim chunks."
  auto-claim-disabled: "&cAuto-claim disabled."
  auto-unclaim-enabled: "&aAuto-unclaim enabled. Walk to unclaim your chunks."
  auto-unclaim-disabled: "&cAuto-unclaim disabled."
  auto-unclaimed: "&eChunk auto-unclaimed!"
  claim-limit-reached: "&cYou've reached your claim limit of {limit} chunks!"
  already-claimed: "&cThis chunk is already claimed by {owner}!"
  not-owner: "&cYou don't own this land!"
  not-connected: "&cYou can only claim chunks adjacent to your existing claims!"
  player-trusted-all: "&aAdded {player} to trusted list for all your claims!"
  player-untrusted-all: "&aRemoved {player} from trusted list for all your claims!"
  trust-usage: "&cUsage: /claim trust <player>"
  untrust-usage: "&cUsage: /claim untrust <player>"
  invalid-command: "&cInvalid command. Use /claim help for help."
  help-header: "&6--- LandClaim Help ---"
  help-claim: "&e/claim &7- Claim the current chunk"
  help-unclaim: "&e/unclaim &7- Unclaim the current chunk"
  help-claim-auto: "&e/claim auto &7- Toggle auto claim"
  help-unclaim-auto: "&e/unclaim auto &7- Toggle auto unclaim"
  help-trust: "&e/claim trust <player> &7- Trust a player in all your claims"
  help-untrust: "&e/claim untrust <player> &7- Untrust a player from all your claims"
  help-unstuck: "&e/claim unstuck &7- Teleport out of someone else's claim"
  player-not-found: "&cPlayer not found!"
  access-denied: "&cYou don't have permission to build here!"
  access-denied-interact: "&cYou can't interact with that here!"
  reloaded: "&aConfiguration reloaded successfully!"
  actionbar-owner: "&e{owner}'s claim"
  cannot-trust-self: "&cYou cannot trust yourself!"
  player-not-trusted: "&cThat player is not trusted!"
  pvp-denied: "&cYou cannot PvP in this claimed land!"
  mob-grief-denied: "&cMobs cannot grief in claimed land!"
  command-blocked: "&cThis command is blocked in claimed land!"
  world-blocked: "&cYou cannot claim land in this world!"
  cannot-unstuck-here: "&cYou can only use this when trapped in someone else's claim!"
  unstuck-success: "&aYou've been teleported to a safe location!"
  unstuck-cooldown: "&cYou must wait {seconds} more seconds before using this again!"

```

## 📋 Commands

### Player Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/claim` | Claim current chunk | `landclaim.claim` |
| `/claim auto` | Toggle auto-claim mode | `landclaim.auto` |
| `/claim trust <player>` | Trust a player | `landclaim.claim` |
| `/claim untrust <player>` | Remove trust | `landclaim.claim` |
| `/claim unstuck` | Escape from others' claims | `landclaim.claim` |
| `/claim help` | Show help information | `landclaim.claim` |
| `/unclaim` | Unclaim current chunk | `landclaim.claim` |
| `/unclaim auto` | Toggle auto-unclaim | `landclaim.auto` |

### Admin Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/claim reload` | Reload configuration | `landclaim.admin` |

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `landclaim.claim` | Basic claiming permission | true |
| `landclaim.admin` | Bypass protection and limits | op |
| `landclaim.auto` | Use auto-claim feature | true |
| `landclaim.limit.<number>` | Custom claim limit | false |

## 🛡️ Protection Features

LandClaim offers comprehensive protection for claimed areas:

- **Block Protection**: Prevents unauthorized building/breaking
- **Container Protection**: Secures chests, furnaces, and other containers
- **Interaction Protection**: Controls access to doors, buttons, and redstone devices
- **PvP Protection**: Disables player combat in claimed chunks
- **Mob Griefing Prevention**: Stops creepers, endermen, and other mob damage
- **Explosion Protection**: Prevents TNT damage near claims
- **Command Restrictions**: Blocks specified commands in claims
- **Trust System**: Grant specific players building permissions

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

## 🖼️ Screenshots

![Claim Visualization](https://i.imgur.com/5Kz9Q7r.png)  
*Visual representation of claimed chunks*

![Action Bar Display](https://i.imgur.com/3GfYt2H.png)  
*Action bar showing claim ownership*

## ❓ Support

For support, bug reports, or feature requests:  
[![Discord](https://img.shields.io/discord/your-discord-server?style=for-the-badge)](https://discord.gg/your-invite-link)  
[![GitHub Issues](https://img.shields.io/github/issues/yourusername/LandClaim?style=for-the-badge)](https://github.com/synkfr/LandClaimPlugin/issues)

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
