# Database Documentation

## Supported Backends

| Backend | Class | Default |
|---|---|---|
| SQLite | `SQLiteDatabase` | Yes |
| MySQL/MariaDB | `MySQLDatabase` | Optional |

Configuration sets `database.type` (`SQLITE` or `MYSQL`) in `config.yml`.

## Connection pooling

MySQL connections use HikariCP with configurable pool settings.
- `maximumPoolSize` (default `10`)
- `minimumIdle` (default `2`)
- `connectionTimeout` (default `30000` ms)

## Schema

All tables use a configurable prefix (default `lc_`).

### `lc_claim_profiles`

Primary table for claim profile metadata and per-claim toggles.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `PRIMARY KEY` | Profile ID (owner UUID in single-profile mode) |
| `name` | `VARCHAR(64)` | `NOT NULL` | Display name |
| `claim_color` | `VARCHAR(16)` | Nullable | Hex color (for example `#00FF00`) |
| `vis_mode` | `VARCHAR(32)` | Default `'DISPLAY_ENTITY'` | Visualization mode |
| `title_enabled` | `BOOLEAN` | Default `FALSE` | Entry/exit title toggle |
| `enter_title` | `VARCHAR(255)` | Nullable | MiniMessage enter title |
| `leave_title` | `VARCHAR(255)` | Nullable | MiniMessage leave title |
| `enter_title_mode` | `VARCHAR(32)` | Default `'DEFAULT'` | Title display mode on enter |
| `leave_title_mode` | `VARCHAR(32)` | Default `'DEFAULT'` | Title display mode on leave |
| `owner_alias` | `VARCHAR(64)` | Nullable | Custom display name replacing owner username |
| `pvp_enabled` | `BOOLEAN` | Default `FALSE` | Per-claim PvP toggle |
| `pvp_timer_end` | `BIGINT` | Default `0` | Unix millis expiry for timed PvP state |
| `real_owner_id` | `VARCHAR(36)` | Nullable | Owning player UUID (multi-profile support) |
| `bonus_role_slots` | `INT` | Default `0` | Purchased extra custom role slots |
| `bonus_member_slots` | `INT` | Default `0` | Purchased extra member slots |
| `bonus_warp_slots` | `INT` | Default `0` | Purchased extra warp slots |
| `tnt_enabled` | `BOOLEAN` | Default `FALSE` | Per-claim TNT explosion toggle |
| `mob_spawning_enabled` | `BOOLEAN` | Default `TRUE` | Per-claim natural and spawner mob spawning toggle |

### `lc_claimed_chunks`

Stores all claimed chunk positions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `chunk_id` | `VARCHAR(128)` | `PRIMARY KEY` | Format `world:x:z` |
| `owner_id` | `VARCHAR(36)` | `NOT NULL`, indexed | FK to `claim_profiles.owner_id` |

### `lc_profile_roles`

Custom roles defined per profile.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(36)` | `PRIMARY KEY` | Role UUID |
| `owner_id` | `VARCHAR(36)` | `NOT NULL`, indexed | FK to `claim_profiles.owner_id` |
| `name` | `VARCHAR(64)` | `NOT NULL` | Role name (case-insensitive key) |
| `priority` | `INT` | `NOT NULL` | Lower runs higher priority |
| `flags` | `TEXT` | `NOT NULL` | Comma-separated permission flags |

### `lc_profile_trusted_players`

Per-player permission overrides.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `NOT NULL` | FK to `claim_profiles.owner_id` |
| `player_id` | `VARCHAR(36)` | `NOT NULL` | Trusted player UUID |
| `flags` | `TEXT` | `NOT NULL` | Comma-separated permission flags |
| | | `PRIMARY KEY (owner_id, player_id)` | |

### `lc_profile_visitor_flags`

Base permission layer for all non-members.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `NOT NULL` | FK to `claim_profiles.owner_id` |
| `flag` | `VARCHAR(64)` | `NOT NULL` | Permission flag name |
| | | `PRIMARY KEY (owner_id, flag)` | |

### `lc_profile_member_roles`

Player-to-role assignments within a profile.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `NOT NULL` | FK to `claim_profiles.owner_id` |
| `player_id` | `VARCHAR(36)` | `NOT NULL` | Member UUID |
| `role_name` | `VARCHAR(64)` | `NOT NULL` | Assigned role name |
| | | `PRIMARY KEY (owner_id, player_id)` | |

### `lc_profile_ally_flags`

Allied claim relationships with per-ally permissions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `NOT NULL` | FK to `claim_profiles.owner_id` |
| `ally_id` | `VARCHAR(36)` | `NOT NULL` | Allied profile ID |
| `flags` | `TEXT` | `NOT NULL` | Comma-separated permission flags |
| | | `PRIMARY KEY (owner_id, ally_id)` | |

### `lc_players`

Player preference data.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `uuid` | `VARCHAR(36)` | `PRIMARY KEY` | Player UUID |
| `auto_claim` | `BOOLEAN` | Default `FALSE` | Auto-claim toggle |
| `auto_unclaim` | `BOOLEAN` | Default `FALSE` | Auto-unclaim toggle |
| `vis_mode` | `VARCHAR(32)` | Default `'DEFAULT'` | Preferred visualization |
| `bonus_claim_blocks` | `INT` | Default `0` | Bonus claim limit |
| `decay_exempt` | `BOOLEAN` | Default `FALSE` | Inactivity decay exemption toggle |

### `lc_warps`

Warp teleport points.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `owner_id` | `VARCHAR(36)` | `NOT NULL` | Profile ID |
| `name` | VARCHAR(64) | NOT NULL | Warp name (lowercase) |
| `world` | VARCHAR(64) | NOT NULL | World name |
| `x` | DOUBLE | NOT NULL | X coordinate |
| `y` | DOUBLE | NOT NULL | Y coordinate |
| `z` | DOUBLE | NOT NULL | Z coordinate |
| `yaw` | FLOAT | NOT NULL | Yaw rotation |
| `pitch` | FLOAT | NOT NULL | Pitch rotation |
| `icon` | VARCHAR(64) | NOT NULL | Material name for icon |
| | | PRIMARY KEY (owner_id, name) | |

### Legacy Tables (V1 Migration)

These tables exist for backward compatibility with the V1 → V2 migration:

| Table | Status |
|-------|--------|
| `lc_claims` | Deprecated — migrated to profiles |
| `lc_claim_chunks` | Deprecated — use `claimed_chunks` |
| `lc_claim_members` | Deprecated — use `profile_member_roles` |
| `lc_claim_trusted` | Deprecated — use `profile_trusted_players` |

## DAO Architecture

```
ProfileDao (interface)
    └── SQLProfileDao (implementation)

ClaimDao (interface, legacy)
    └── SQLClaimDao (implementation)

PlayerDao (interface)
    └── SQLPlayerDao (implementation)

RoleDao (interface)
    └── SQLRoleDao (implementation)

WarpDao (interface)
    └── SQLWarpDao (implementation)
```

All DAO operations return `CompletableFuture<T>` for async execution. The `DatabaseManager` routes to the correct implementation based on the configured database type.

## Save Strategy

`SQLProfileDao.saveProfile()` uses a **clear-and-reinsert** strategy within a transaction:

```
BEGIN TRANSACTION
  1. UPSERT claim_profiles row
  2. DELETE FROM claimed_chunks WHERE owner_id = ?
  3. INSERT claimed_chunks (batch)
  4. DELETE FROM profile_visitor_flags WHERE owner_id = ?
  5. INSERT profile_visitor_flags (batch)
  6. DELETE FROM profile_trusted_players WHERE owner_id = ?
  7. INSERT profile_trusted_players (batch)
  8. DELETE FROM profile_roles WHERE owner_id = ?
  9. INSERT profile_roles (batch)
  10. DELETE FROM profile_member_roles WHERE owner_id = ?
  11. INSERT profile_member_roles (batch)
  12. DELETE FROM profile_ally_flags WHERE owner_id = ?
  13. INSERT profile_ally_flags (batch)
COMMIT
```

This approach is simple and ensures consistency, at the cost of more writes. For profiles with many chunks/members, consider batching optimizations in future versions.

## Cross-Server Sync (Redis)

When Redis is enabled, cache invalidation messages are published after DB writes:

```
Publish → landclaim:sync → "INVALIDATE_PROFILE:<owner_uuid>"
```

All servers subscribe and invalidate their local Caffeine cache on receipt.
