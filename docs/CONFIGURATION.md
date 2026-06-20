# Configuration Reference

## `config.yml`

Main plugin configuration file located at `plugins/LandClaimPlugin/config.yml`.

### General Settings

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `prefix` | String | `<dark_gray>[<gold>LandClaim<dark_gray>]</gold>` | Chat prefix (MiniMessage format) |
| `language` | String | `en-US` | Locale file to load from `locales/` folder |
| `requireConnectedClaims` | Boolean | `false` | Require new claims to touch existing claims |
| `allowDiagonalConnections` | Boolean | `true` | Allow diagonal adjacency for connected claims |
| `cooldownUnstuck` | Integer | `30` | Seconds cooldown for unstuck command |
| `useSeparatePremission` | Boolean | `false` | If true, separate LuckPerms permissions like `landclaim.menu.<menu>`, `landclaim.menu.*`, and command-specific permissions will be checked. If false, these separate permissions are bypassed. |
| `bannedClaimNamesFile` | String | `banned-claim-name.txt` | The file name containing banned words (one per line) that cannot be used in claim names. |

::: tip Placeholder Brackets
For all messages and placeholders, the plugin supports both **angle brackets** `<owner>` and **curly braces** `{owner}` interchangeably. 
:::

### Claim Limits

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `chunkClaimLimit` | Integer | `5` | Default max chunks per player |
| `maxWarps` | Integer | `3` | Default max warps per player |

Permission overrides: `landclaim.limit.<n>`, `landclaim.warps.limit.<n>`

### Multi-Profile System

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `multiProfilesEnabled` | Boolean | `false` | Enable multi-profile support |
| `maxProfilesPerPlayer` | Integer | `2` | Max profiles a player can **own** |

::: warning
If you disable `multiProfilesEnabled` after players have already created multiple profiles, they will lose access to all secondary profiles. This setting should be treated as permanent once enabled.
:::

### World & Command Blocking

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `blockWorld` | List | `[world_nether, world_the_end]` | Worlds where claiming is disabled |
| `blockCmd` | List | `[setwarp, warp]` | Commands blocked inside others' claims |

::: tip
Changes to `blockWorld` and `blockCmd` take effect immediately on server startup — no `/claim admin reload` needed. To unblock a dimension, simply remove it from the list and restart.
:::

### Protection Gaps

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `worldguardGap` | Integer | `0` | Minimum chunks between claims and WorldGuard regions (0 = disabled) |
| `minClaimGap` | Integer | `0` | Minimum chunks between different players' claims (0 = disabled) |

### Database

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `database.type` | String | `SQLITE` | Database backend: `SQLITE` or `MYSQL` |
| `database.host` | String | `localhost` | MySQL host |
| `database.port` | Integer | `3306` | MySQL port |
| `database.databaseName` | String | `landclaim` | MySQL database name |
| `database.username` | String | `root` | MySQL username |
| `database.password` | String | `password` | MySQL password (or set `LANDCLAIM_DB_PASSWORD` env var) |
| `database.tablePrefix` | String | `lc_` | Prefix for all SQL tables |
| `database.maximumPoolSize` | Integer | `10` | HikariCP max pool size |
| `database.minimumIdle` | Integer | `2` | HikariCP min idle connections |
| `database.connectionTimeout` | Long | `30000` | Connection timeout in ms |

### Redis (Cross-Server Sync)

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `redis.enabled` | Boolean | `false` | Enable Redis pub/sub sync |
| `redis.host` | String | `localhost` | Redis host |
| `redis.port` | Integer | `6379` | Redis port |
| `redis.password` | String | `""` | Redis password (empty = no auth) |
| `redis.channel` | String | `landclaim:sync` | Pub/sub channel name |

### Map Integrations

Each map plugin has identical configuration:

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `<map>.enabled` | Boolean | `true` | Enable this map hook |
| `<map>.fillColor` | String | `3366FF` | Fill color (hex) |
| `<map>.fillOpacity` | Double | `0.3` | Fill opacity (0.0-1.0) |
| `<map>.borderColor` | String | `3366FF` | Border color (hex) |
| `<map>.borderOpacity` | Double | `0.8` | Border opacity (0.0-1.0) |

Supported maps: `dynmap`, `bluemap`, `squaremap`, `pl3xmap`

### Visualization

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `actionbarUpdateInterval` | Integer | `20` | Ticks between actionbar updates |

### Bedrock / Geyser Support

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `geyserForms` | Boolean | `true` | When Geyser 2.x is installed, Bedrock players receive native Bedrock forms (CustomForm text input for renames/colors, ModalForm confirmations for abandon/ban/unclaim-all) instead of Java-only chat-prompt fallbacks. If Geyser is not installed, this option has no effect. |

::: tip
Geyser-Spigot and Floodgate are listed in `soft-depend` so the server attempts to load them before LandClaimPlugin. The Bedrock form code is reflection-based — it does not introduce a hard dependency on either plugin.
:::

::: note Map plugins
The `squaremap` and `Pl3xMap` hooks both wait for the corresponding plugin's `PluginEnableEvent` and retry their API call on the next tick if the API isn't ready yet. Markers should appear automatically after a `/claim reload` or after a fresh claim. If you do not see your claims on the live map:

1. Confirm the world is enabled in your map plugin's config (e.g. `squaremap/config.yml`'s `worlds:` section).
2. Confirm the chunk is in the same Bukkit world name the map plugin reports (the plugin handles `minecraft:world` ↔ `world` automatically).
3. Run `/claim admin reload` to force a full map refresh.
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

---

## Permission Flags

Complete list of permission flags used across roles, trusted players, and visitor settings.

### Block Interaction Flags

| Flag | Description |
|------|-------------|
| `USE_DOORS` | Open/close doors |
| `USE_TRAPDOORS` | Open/close trapdoors |
| `USE_FENCE_GATES` | Open/close fence gates |
| `USE_CONTAINERS` | Access chests, barrels, hoppers, etc. |
| `USE_WORKSTATIONS` | Use crafting tables, anvils, enchanting tables, etc. |
| `USE_BEDS` | Sleep in beds |
| `USE_REDSTONE` | Use buttons, levers, pressure plates |
| `USE_LECTERNS` | Read lecterns |
| `USE_BELLS` | Ring bells |

### Entity Interaction Flags

| Flag | Description |
|------|-------------|
| `DAMAGE_ANIMALS` | Harm passive mobs |
| `DAMAGE_MONSTERS` | Harm hostile mobs |
| `BREED_ANIMALS` | Breed animals |
| `SHEAR_ENTITIES` | Shear sheep |
| `TRADE_VILLAGERS` | Trade with villagers |
| `FEED_ANIMALS` | Feed animals |
| `LEASH_ENTITIES` | Use leads on entities |
| `MODIFY_ARMOR_STANDS` | Edit armor stands |
| `MODIFY_ITEM_FRAMES` | Rotate/place items in frames |

### Vehicle Flags

| Flag | Description |
|------|-------------|
| `RIDE_VEHICLES` | Enter minecarts, boats, etc. |
| `PLACE_VEHICLES` | Place vehicles |
| `DESTROY_VEHICLES` | Break vehicles |

### Misc Flags

| Flag | Description |
|------|-------------|
| `USE_ENDER_PEARLS` | Throw ender pearls |
| `USE_CHORUS_FRUIT` | Eat chorus fruit |
| `PICKUP_ITEMS` | Pick up dropped items |
| `DROP_ITEMS` | Drop items from inventory |

### Administrative Flags
These flags grant control over the claim management systems.

| Flag | Description |
|------|-------------|
| `CLAIM_LAND` | Allow members to claim new chunks for the owner's profile |
| `ADMIN_MENU` | Allow members to open the `/claim menu` while standing in the claim |
| `MANAGE_SETTINGS` | Access to claim settings (PvP, Color, Toggles) |
| `MANAGE_MEMBERS` | Access to member management (Add/Remove/Trust) |
| `MANAGE_ROLES` | Access to role management (Create/Edit/Delete) |

::: warning Owner-Only Restrictions
Even with `ADMIN_MENU` and management flags, the following actions are **strictly owner-only** for regular player claims:
- **Abandoning/Deleting** the entire profile or claim.
- **Renaming** the claim.
- **Transferring Ownership**.
:::

### Admin & Server Land
Administrators with the `landclaim.admin` permission have special bypasses for managing server-owned land.

- **Admin Profile**: A global, dedicated profile (UUID `00000000-0000-0000-0000-000000000000`) is used for all Server Land.
- **Management Bypass**: Any player with `landclaim.admin` can manage the Admin Profile using `/claim admin menu`.
- **Full Control**: Unlike regular members, admins bypass the "Owner-Only" restrictions when managing the Admin Profile, allowing them to **Rename** (e.g., to "Spawn") or **Abandon** server chunks.
- **Command Bypass**: `/claim admin claim` allows instant claiming for the server profile, bypassing all connection and limit checks.

---

## Default Roles

Every new `ClaimProfile` gets two built-in roles:

### Member
Priority: 100 (lower priority than CoOwner)
Flags: `USE_DOORS`, `USE_TRAPDOORS`, `USE_FENCE_GATES`, `USE_CONTAINERS`, `USE_WORKSTATIONS`, `USE_BEDS`, `USE_REDSTONE`

### CoOwner
Priority: 10 (higher priority than Member)
Flags: All 25 flags listed above

---

## Menu Configuration

All GUI menus are fully configurable via YAML files in the `plugins/LandClaimPlugin/menus/` directory. Each menu config lets you customize:

- **Materials** — Item types for buttons, fillers, and navigation
- **Display Names** — MiniMessage-formatted text for all items
- **Lore** — Item descriptions and instructions
- **Layout** — Slot positions and structure patterns

Available menu configs:
- `mainmenu.yml` — Main claim management GUI
- `profile-selector.yml` — Profile selector GUI
- `ClaimSettings.yml` — Claim settings toggles
- And many more for members, allies, warps, roles, etc.
