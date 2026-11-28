# Configuration Reference

## `config.yml`

Main plugin configuration file located at `plugins/LandClaimPlugin/config.yml`. Existing flat configurations from `3.4.0` and earlier are automatically backed up (`config.yml.bk`, `locales/<message>.yml.bk`, and `menus/<menu>.yml.bk`) and migrated on startup.

### General (`general.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `general.configVersion` | String | `3.5.0` | Internal schema version used for one-time upgrade backups |
| `general.language` | String | `en-US` | Locale file loaded from `locales/messages_<language>.yml` |
| `general.bannedClaimNamesFile` | String | `banned-claim-name.txt` | File containing blocked claim name words (one word per line) |
| `general.cooldownUnstuck` | Integer | `30` | Cooldown in seconds between `/claim unstuck` uses |
| `general.geyserForms` | Boolean | `true` | Send native Bedrock forms when Geyser 2.x / Floodgate are available (falls back to Java/chat prompts when disabled) |

::: tip Placeholder Brackets
Messages support both angle brackets `<owner>` and curly braces `{owner}` interchangeably. Chat prefix (`prefix`) and all localized templates live in `plugins/LandClaimPlugin/locales/messages_<language>.yml` using MiniMessage formatting.
:::

### Claiming Rules & Multi-Profile (`claiming.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `claiming.requireConnectedClaims` | Boolean | `false` | Require new claims to touch existing claims owned by the profile |
| `claiming.allowDiagonalConnections` | Boolean | `true` | Allow diagonal adjacency when `requireConnectedClaims` is true |
| `claiming.minClaimGap` | Integer | `0` | Minimum chunks between different players' claims (`0` = disabled) |
| `claiming.worldguardGap` | Integer | `0` | Minimum chunks between claims and WorldGuard regions (`0` = disabled) |
| `claiming.multiProfilesEnabled` | Boolean | `false` | Enable multi-profile support via `/claim profiles` |
| `claiming.maxProfilesPerPlayer` | Integer | `2` | Maximum profiles a single player can own |
| `claiming.blockWorld` | List | `[world_nether, world_the_end]` | Worlds where claiming is disabled |
| `claiming.blockCmd` | List | `[setwarp, warp, sethome]` | Commands blocked inside another player's claim |

::: warning
Disabling `claiming.multiProfilesEnabled` after players have already created multiple profiles hides all secondary profiles from their owners. Treat this setting as permanent once enabled.
:::

::: tip
Changes to `claiming.blockWorld` and `claiming.blockCmd` take effect immediately on server startup. To unblock a dimension, remove it from `claiming.blockWorld` and restart.
:::

### Claim Limits (`limits.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `limits.chunkClaimLimit` | Integer | `5` | Default base chunk limit per player |
| `limits.maxBonusClaimBlocks` | Integer | `0` | Hard cap on bonus chunks a player can acquire (`0` = unlimited) |
| `limits.maxTotalClaimLimit` | Integer | `0` | Hard cap on total chunk limit including base limit and bonus chunks (`0` = unlimited) |
| `limits.maxMemberships` | Integer | `1` | Maximum claims a player may belong to as a member |
| `limits.maxClaimMembers` | Integer | `5` | Default maximum members allowed inside one claim profile |
| `limits.maxWarps` | Integer | `3` | Base claim warp limit per profile |

Permission overrides include `landclaim.limit.<n>` and `landclaim.warps.limit.<n>`.

### Permissions (`permissions.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `permissions.useSeparatePermission` | Boolean | `false` | When `true`, enforce granular nodes such as `landclaim.menu.<menu>`, `landclaim.menu.*`, `landclaim.unstuck`, and subcommand permissions. When `false`, regular players can use standard claim menus and commands without extra nodes. |

### Database (`database.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `database.type` | String | `SQLITE` | Storage backend (`SQLITE`, `MYSQL`, or `MARIADB`) |
| `database.host` | String | `localhost` | MySQL or MariaDB host |
| `database.port` | Integer | `3306` | Database port |
| `database.databaseName` | String | `landclaim` | Database name |
| `database.username` | String | `""` | Database username |
| `database.password` | String | `""` | Database password (or set the `LANDCLAIM_DB_PASSWORD` environment variable) |
| `database.tablePrefix` | String | `lc_` | Prefix for all SQL tables |
| `database.maximumPoolSize` | Integer | `10` | HikariCP max pool size |
| `database.minimumIdle` | Integer | `2` | HikariCP min idle connections |
| `database.connectionTimeout` | Long | `30000` | Connection timeout in ms |

### Redis (`redis.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `redis.enabled` | Boolean | `false` | Enable Redis pub/sub sync |
| `redis.host` | String | `localhost` | Redis host |
| `redis.port` | Integer | `6379` | Redis port |
| `redis.password` | String | `""` | Redis password (empty = no auth) |
| `redis.channel` | String | `landclaim:sync` | Pub/sub channel name |

### Map Integrations (`maps.*`)

Each web map integration under `maps` shares identical keys.

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `maps.<map>.enabled` | Boolean | `true` | Enable this map hook |
| `maps.<map>.fillColor` | String | `3366FF` | Fill hex color without `#` |
| `maps.<map>.fillOpacity` | Double | `0.3` | Fill opacity (`0.0` to `1.0`) |
| `maps.<map>.borderColor` | String | `3366FF` | Border hex color without `#` |
| `maps.<map>.borderOpacity` | Double | `0.8` | Border opacity (`0.0` to `1.0`) |

Supported map keys under `maps.` are `dynmap`, `bluemap`, `squaremap`, and `pl3xmap`.

### Display & Action Bar (`display.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `display.actionbarEnabled` | Boolean | `true` | Show territory status in the player action bar |
| `display.actionbarUpdateInterval` | Integer | `20` | Ticks between action bar updates (`20` ticks = 1 second) |
| `display.claimChatNotifications` | Boolean | `false` | Send claim enter and leave messages in chat by default (toggled per player with `/claim notify`) |

### Held Territory Map (`minimap.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `minimap.enabled` | Boolean | `true` | Enable or disable the held territory map feature |
| `minimap.mapId` | Integer | `-1` | Persisted MapView ID assigned to the territory map (`-1` creates one automatically) |
| `minimap.itemName` | String | `<color:#38BDF8><b>Territory Map</b></color>` | Display name for the Territory Map item (MiniMessage format) |
| `minimap.itemLore` | List | `[...]` | Instruction lore lines for the map item |

### Update Checker (`updateChecker.*`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `updateChecker.enabled` | Boolean | `true` | Check for new plugin releases on Modrinth |
| `updateChecker.modrinthProjectId` | String | `PAlDTriF` | Modrinth project ID (`PAlDTriF`) or slug |
| `updateChecker.notifyPermission` | String | `landclaim.admin` | Permission required to receive join update notices (or OP) |

::: tip Bedrock Support
Geyser-Spigot and Floodgate are listed in `soft-depend` so the server attempts to load them before LandClaimPlugin. The Bedrock form code uses reflection and introduces no hard dependency on either plugin.
:::

::: note Map plugins
The `squaremap` and `Pl3xMap` hooks both wait for the corresponding plugin's `PluginEnableEvent` and retry their API call on the next tick if the API isn't ready yet. Markers should appear automatically after a `/claim reload` or after a fresh claim. If you do not see your claims on the live map, confirm the world is enabled in your map plugin's config (for example, `squaremap/config.yml` `worlds`), confirm the chunk uses the Bukkit world name reported to the plugin, and run `/claim admin reload` to force a full map refresh.
:::

### Wilderness Protection

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `wildernessProtection.enabled` | Boolean | `false` | When `true`, the flags in `deniedFlags` are denied in unclaimed chunks. Admins (`landclaim.admin`) always bypass. |
| `wildernessProtection.exceptionWorlds` | List | `[]` | Worlds where wilderness protection does not apply. Use the plain Bukkit world name (e.g. `world`, `world_nether`). |
| `wildernessProtection.deniedFlags` | List | build / interaction flags only (see below) | The permission flags denied in the wilderness. The default list contains only build / interaction flags — combat flags (DAMAGE_ANIMALS, DAMAGE_MONSTERS, BREED_ANIMALS, SHEAR_ENTITIES, TRADE_VILLAGERS, FEED_ANIMALS, LEASH_ENTITIES) and PvP are NOT denied by default, so players can still hunt and fight mobs in the wilderness. Add a flag to the list to deny it; remove to allow it. Matching is case-insensitive. |

**Default `deniedFlags` list** (build / interaction only):

```
BLOCK_BREAK, BLOCK_PLACE, BLOCK_IGNITE, USE_BUCKETS, USE_FERTILIZER, MODIFY_SIGNS,
USE_DOORS, USE_TRAPDOORS, USE_FENCE_GATES, USE_CONTAINERS, USE_WORKSTATIONS,
USE_BEDS, USE_REDSTONE, USE_LECTERNS, USE_BELLS, TRAMPLE_CROPS,
MODIFY_ARMOR_STANDS, MODIFY_ITEM_FRAMES
```

::: tip Use case
Enable wilderness protection on a towny-style survival server where the only safe place to build is inside a clan claim. Players can still walk, chat, hunt mobs, drop items, throw ender pearls, and fight in the wilderness, but cannot break or place blocks until they find a claim or get admin help.
:::

::: warning Combat flags and PvP
By default, combat flags (`DAMAGE_ANIMALS`, `DAMAGE_MONSTERS`, `BREED_ANIMALS`, etc.) and PvP are NOT in `deniedFlags`, so players retain the ability to hunt and fight in the wilderness. If you want to lock those down too, add them to the list (e.g. `deniedFlags: [..., DAMAGE_ANIMALS, DAMAGE_MONSTERS, PVP]`).
:::

::: warning Explosions
Wilderness protection does not affect explosion damage (`ExplosionProtectionListener`) — that listener has its own logic. To make wilderness fully off-limits, combine this with a `blockWorld` entry for the world, or use another plugin to disable mob spawning / explosions.
:::

### PvP

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `pvp.forceEnabled` | Boolean | `false` | When `true`, `PvpProtectionListener` always allows PvP regardless of the per-claim `pvpEnabled` flag, and `/claim pvp` refuses to toggle (sends `pvp-force-locked`). Designed for PvP-focused servers that want every claim to have PvP on with no opt-out. |

### Combat Tag (`pvp.combatTagBypass`)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `pvp.combatTagBypass` | Boolean | `true` | Players tagged in PvP combat via DeluxeCombat, CombatLogX, PvPManager, or EternalCombat temporarily lose claim PvP sanctuary until their combat tag expires |

### Inactive Claim Auto-Decay

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `decay.enabled` | Boolean | `false` | Enable automatic claim decay for inactive players |
| `decay.inactiveDays` | Integer | `14` | Days of inactivity (player offline) before claims automatically decay |
| `decay.checkIntervalHours` | Integer | `12` | Frequency in hours to scan for inactive claims |
| `decay.exemptPermission` | String | `landclaim.decay.exempt` | Permission node that exempts players from auto-decay |
| `decay.onlyCheckOwner` | Boolean | `true` | If `true`, only owner activity is checked. If `false`, active claim members protect the claim from decay |

### Visitor Settings & Locking

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `visitorSettings.locked` | Boolean | `false` | Lock visitor settings server-wide. When `true`, claim owners/members cannot open or edit visitor settings via GUI or `/claim menu visitors`, and `defaultFlags` are strictly enforced across ALL claims for visitors. |
| `visitorSettings.defaultFlags` | List | `["DAMAGE_MONSTERS"]` | Default visitor flags. When `locked: true`, all claims enforce these flags for visitors (flags in the list are allowed, omitted flags are denied). When `locked: false`, these flags are assigned as starting flags for new claims. |

::: tip Open Visitor World Example
To allow visitors to do everything in claims except breaking and placing blocks, set:
```yaml
visitorSettings:
  locked: true
  defaultFlags:
    - BLOCK_IGNITE
    - USE_BUCKETS
    - USE_FERTILIZER
    - MODIFY_SIGNS
    - TRAMPLE_CROPS
    - USE_DOORS
    - USE_TRAPDOORS
    - USE_FENCE_GATES
    - USE_CONTAINERS
    - USE_WORKSTATIONS
    - USE_BEDS
    - USE_REDSTONE
    - USE_LECTERNS
    - USE_BELLS
    - DAMAGE_ANIMALS
    - DAMAGE_MONSTERS
    - BREED_ANIMALS
    - SHEAR_ENTITIES
    - TRADE_VILLAGERS
    - FEED_ANIMALS
    - LEASH_ENTITIES
    - MODIFY_ARMOR_STANDS
    - MODIFY_ITEM_FRAMES
    - RIDE_VEHICLES
    - MOUNT_ANIMALS
    - PLACE_VEHICLES
    - DESTROY_VEHICLES
    - USE_ENDER_PEARLS
    - USE_CHORUS_FRUIT
    - PICKUP_ITEMS
    - DROP_ITEMS
```
:::

---

## Permission flags

Complete list of permission flags used across roles, trusted players, and visitor settings.

### Building and environment flags

| Flag | Description |
|---|---|
| `BLOCK_BREAK` | Break blocks inside the claim |
| `BLOCK_PLACE` | Place blocks inside the claim |
| `BLOCK_IGNITE` | Ignite blocks with flint and steel or fire charges |
| `USE_BUCKETS` | Empty or fill buckets |
| `USE_FERTILIZER` | Apply bone meal to crops and saplings |
| `MODIFY_SIGNS` | Edit or dye placed signs |
| `TRAMPLE_CROPS` | Trample farmland by jumping on crops |

### Block interaction flags

| Flag | Description |
|---|---|
| `USE_DOORS` | Open and close doors |
| `USE_TRAPDOORS` | Open and close trapdoors |
| `USE_FENCE_GATES` | Open and close fence gates |
| `USE_CONTAINERS` | Access chests, barrels, hoppers, and similar storage |
| `USE_WORKSTATIONS` | Use crafting tables, anvils, enchanting tables, and furnaces |
| `USE_BEDS` | Sleep in beds |
| `USE_REDSTONE` | Use buttons, levers, and pressure plates |
| `USE_LECTERNS` | Read or take books from lecterns |
| `USE_BELLS` | Ring bells |

### Entity interaction flags

| Flag | Description |
|---|---|
| `DAMAGE_ANIMALS` | Harm passive mobs |
| `DAMAGE_MONSTERS` | Harm hostile mobs |
| `BREED_ANIMALS` | Breed animals |
| `SHEAR_ENTITIES` | Shear sheep and interact with shearables |
| `TRADE_VILLAGERS` | Trade with villagers |
| `FEED_ANIMALS` | Feed animals |
| `LEASH_ENTITIES` | Attach or detach leads on entities |
| `MODIFY_ARMOR_STANDS` | Edit armor stand equipment and poses |
| `MODIFY_ITEM_FRAMES` | Rotate, place, or remove items in item frames |

### Vehicle flags

| Flag | Description |
|---|---|
| `RIDE_VEHICLES` | Enter minecarts, boats, and rideables |
| `MOUNT_ANIMALS` | Mount tamed or rideable animals (horses, pigs, striders) |
| `PLACE_VEHICLES` | Place boats and minecarts |
| `DESTROY_VEHICLES` | Break boats and minecarts |

### Misc flags

| Flag | Description |
|---|---|
| `USE_ENDER_PEARLS` | Throw ender pearls |
| `USE_CHORUS_FRUIT` | Eat chorus fruit |
| `PICKUP_ITEMS` | Pick up dropped items |
| `DROP_ITEMS` | Drop items from inventory |
| `DAMAGE_PLAYERS` | Player damage check used with PvP rules |

### Administrative flags
These flags grant control over claim management menus and chunk expansion.

| Flag | Description |
|---|---|
| `CLAIM_LAND` | Allow members to claim new chunks for the owner's profile |
| `ADMIN_MENU` | Allow members to open `/claim menu` inside the claim |
| `MANAGE_SETTINGS` | Access claim settings (PvP, color, visibility, titles, TNT, mob spawning) |
| `MANAGE_MEMBERS` | Access member and trusted player management |
| `MANAGE_ROLES` | Access custom role management |

::: warning Owner-only restrictions
Even with `ADMIN_MENU` and management flags, abandoning the claim profile, renaming the claim, and transferring ownership remain restricted to the claim owner (or `landclaim.admin` on the Server Land profile).
:::

---

## Default roles

Every new `ClaimProfile` initializes two built-in roles.

- **Member (priority 100):** `USE_DOORS`, `USE_TRAPDOORS`, `USE_FENCE_GATES`, `USE_CONTAINERS`, `USE_WORKSTATIONS`, `USE_BEDS`, `USE_REDSTONE`, `DAMAGE_MONSTERS`, `DAMAGE_ANIMALS`.
- **CoOwner (priority 10):** Grants all 38 non-PvP gameplay and management flags listed above (excluding `DAMAGE_PLAYERS`, which follows claim PvP settings).

---

## Per-claim toggles (`ClaimSettings.yml`)

Players with ownership or `MANAGE_SETTINGS` can toggle per-claim protection options inside `/claim menu settings` (configured in `plugins/LandClaimPlugin/menus/ClaimSettings.yml`).

| Setting | Default | Config key | Description |
|---|---|---|---|
| PvP | `false` | `pvpToggle` | Allow or block PvP damage inside the claim |
| Titles | `true` | `titleToggle` | Show MiniMessage titles on claim entry and leave |
| Visibility | `DISPLAY_ENTITY` | `visibility` | Show or hide claim borders on web maps |
| TNT explosions | `false` | `tntToggle` | Allow or block TNT (`PRIMED_TNT` and TNT minecarts) explosions inside the claim while keeping creepers and other explosions blocked |
| Mob spawning | `true` | `mobSpawningToggle` | Toggle natural, spawner, trial spawner, patrol, raid, portal, trap, and reinforcement spawns while preserving breeding and custom spawns |

All GUI menus under `plugins/LandClaimPlugin/menus/` support custom Bukkit materials, MiniMessage names, and lore.

---

## Admin & server land

Administrators with `landclaim.admin` manage the dedicated Server Land profile (UUID `00000000-0000-0000-0000-000000000000`).

- **Management access.** Open the Server Land menu with `/claim admin menu`.
- **Owner-only bypass.** Unlike regular claim members, administrators managing the Admin Profile can rename or abandon server chunks.
- **Instant claims.** `/claim admin claim` claims chunks directly for Server Land without connection or chunk limit checks.

---

## Menu configuration files

All GUI files live in `plugins/LandClaimPlugin/menus/`, including `mainmenu.yml`, `profile-selector.yml`, `ClaimSettings.yml`, `VisitorSettings.yml`, and dedicated menus for members, trusted players, allies, roles, and warps.
