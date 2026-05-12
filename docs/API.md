# Internal API Reference

::: info
This is the **internal** API reference for contributors. If you're a plugin developer looking to integrate with LandClaimPlugin, see the [Public API](/guide/api) page instead.
:::

## Managers

### ClaimManager

Central business logic for claim operations.

| Method | Returns | Description |
|--------|---------|-------------|
| `initialize()` | `void` | Load all profiles from DB into cache |
| `claimChunk(Player, Chunk)` | `boolean` | Claim a single chunk for a player |
| `claimChunks(Player, Set)` | `int` | Claim multiple chunks at once |
| `unclaimChunk(Chunk)` | `boolean` | Unclaim a single chunk |
| `abandonProfile(UUID)` | `int` | Delete entire profile, returns chunk count |
| `transferOwnership(UUID, UUID)` | `boolean` | Transfer profile to new owner |
| `getProfileAt(ChunkPosition)` | `ClaimProfile` | Get profile owning a chunk |
| `getProfile(UUID)` | `ClaimProfile` | Get profile by owner UUID |
| `getProfileByName(String)` | `ClaimProfile` | Get profile by display name |
| `isChunkClaimed(ChunkPosition)` | `boolean` | Check if chunk is claimed |
| `getChunkOwner(ChunkPosition)` | `UUID` | Get owner UUID of a chunk |
| `isClaimNameUnique(String)` | `boolean` | Check if claim name is available |
| `canCreateProfile(UUID)` | `boolean` | Check if player can create a new profile |
| `getClaimLimit(Player)` | `int` | Get effective claim limit (with perms + bonus) |
| `getSelection(UUID)` | `ChunkSelection` | Get player's chunk selection |
| `clearSelection(UUID)` | `void` | Clear player's chunk selection |
| `getTotalClaims()` | `int` | Total claimed chunks across all profiles |
| `addAllyInvite(UUID, UUID)` | `void` | Add pending ally invite |
| `removeAllyInvite(UUID, UUID)` | `void` | Remove pending ally invite |
| `hasAllyInvite(UUID, UUID)` | `boolean` | Check pending ally invite |

### PermissionResolver

Implements the 4-tier permission chain: Owner › Role › Trusted › Ally › Visitor.

| Method | Returns | Description |
|--------|---------|-------------|
| `hasPermission(ClaimProfile, UUID, String)` | `boolean` | Check if player has a flag |
| `getPlayerStatus(ClaimProfile, UUID)` | `String` | Get player's status in the claim |

### BlockPermissionResolver

Maps Minecraft blocks to claim permission flags.

| Method | Returns | Description |
|--------|---------|-------------|
| `resolve(Block)` | `BlockPermission` | Get required permission for a block (null = free) |

Resolution order: explicit overrides → InventoryHolder check → Tag matching → workstation switch.

### CacheManager

Caffeine-backed in-memory caches.

| Cache | Key | Value | Expiry |
|-------|-----|-------|--------|
| `profileCache` | UUID (owner) | `ClaimProfile` | None (max 10,000) |
| `claimCache` | UUID (claim id) | `Claim` | 30 min access (max 10,000) |
| `playerCache` | UUID (player) | `ClaimPlayer` | None (max 5,000) |

### ConfigManager

Configuration lifecycle management using Okaeri Configs.

| Method | Returns | Description |
|--------|---------|-------------|
| `getPluginConfig()` | `PluginConfig` | Main config instance |
| `getMessagesConfig()` | `MessagesConfig` | Messages config instance |
| `getMessage(String, String...)` | `String` | Get formatted message with placeholders |
| `isWorldBlocked(String)` | `boolean` | Check if world is blocked |
| `requireConnectedClaims()` | `boolean` | Connected claims setting |
| `allowDiagonalConnections()` | `boolean` | Diagonal connections setting |
| `getWorldGuardGap()` | `int` | WorldGuard gap setting |
| `getMinClaimGap()` | `int` | Min claim gap setting |
| `reloadMainConfig()` | `void` | Reload configuration |

### CombatManager

Detects combat-tagged players via hooked plugins.

| Method | Returns | Description |
|--------|---------|-------------|
| `isInCombat(Player)` | `boolean` | Check if player is in combat |

Supported hooks: DeluxeCombat, PvPManager, EternalCombat.

### WarpManager

Manages warp teleport points.

| Method | Returns | Description |
|--------|---------|-------------|
| `setWarp(UUID, String, Location, Material)` | `boolean` | Create/update a warp |
| `deleteWarp(UUID, String)` | `boolean` | Delete a warp |
| `getWarp(UUID, String)` | `Warp` | Get a specific warp |
| `getWarps(UUID)` | `Map` | Get all warps for a player |
| `getWarpLimit(Player)` | `int` | Get effective warp limit |
| `getWarpCount(UUID)` | `int` | Get current warp count |
| `loadFromDatabase()` | `CompletableFuture` | Load all warps from DB |

### VisualizationManager

Renders claim boundaries using display entities or particles.

| Method | Returns | Description |
|--------|---------|-------------|
| `invalidateCache(UUID)` | `void` | Invalidate cached visualization |
| `cleanupLocalDisplays()` | `void` | Remove all active displays |

### RedisManager

Cross-server cache invalidation via Redis pub/sub.

| Method | Returns | Description |
|--------|---------|-------------|
| `init()` | `void` | Connect and start subscriber |
| `publishUpdate(String, UUID)` | `void` | Publish cache invalidation |
| `shutdown()` | `void` | Close connections |

Message format: `ACTION:UUID` (e.g., `INVALIDATE_PROFILE:uuid`)

### HookManager

Third-party plugin integrations.

| Method | Returns | Description |
|--------|---------|-------------|
| `init()` | `void` | Detect and initialize hooks |
| `isWorldGuardEnabled()` | `boolean` | WorldGuard available |
| `refreshMapHooks()` | `void` | Update all map overlays |

Supported map plugins: BlueMap, Dynmap, Squaremap, Pl3xMap.

## Models

### ClaimProfile

Central data model — one per player (or multiple in multi-profile mode).

**Key Fields:**
- `ownerId` — UUID of the owner
- `name` — Display name
- `ownedChunks` — Set of claimed chunk positions
- `visitorFlags` — Base permission layer
- `trustedPlayerFlags` — Per-player permission overrides
- `roles` — Role definitions
- `memberRoles` — Player to role assignments
- `allyFlags` — Allied profiles with flags
- `warps` — Named warps
- `claimColor` — Hex color for visualization
- `visualizationMode` — `DISPLAY_ENTITY` or `PARTICLE`
- `enterTitleEnabled` — Entry title toggle
- `enterTitle` / `leaveTitle` — MiniMessage titles

**Default Roles:**
- **Member** (priority 100): Basic interact (doors, containers, workstations, beds, redstone)
- **CoOwner** (priority 10): All 25 flags

### Role

Permission role with flag-based access control.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique role identifier |
| `ownerId` | UUID | Profile owner |
| `name` | String | Display name |
| `priority` | int | Lower = checked first |
| `flags` | Set | Permission flags (lowercase) |

### ClaimPlayer

Per-player preferences (persisted in `lc_players`).

| Field | Type | Description |
|-------|------|-------------|
| `uniqueId` | UUID | Player UUID |
| `autoClaim` | boolean | Auto-claim enabled |
| `autoUnclaim` | boolean | Auto-unclaim enabled |
| `visualizationMode` | String | Preferred visualization |
| `bonusClaimBlocks` | int | Bonus claim limit |

### Warp

Named teleport point within a claim.

| Field | Type | Description |
|-------|------|-------------|
| `name` | String | Warp name (case-insensitive) |
| `location` | Location | Bukkit location |
| `icon` | Material | Menu icon material |

### BlockPermission

Enum mapping block types to permission flags.

| Enum Value | Flag |
|------------|------|
| `CONTAINERS` | `USE_CONTAINERS` |
| `DOORS` | `USE_DOORS` |
| `TRAPDOORS` | `USE_TRAPDOORS` |
| `FENCE_GATES` | `USE_FENCE_GATES` |
| `REDSTONE` | `USE_REDSTONE` |
| `BEDS` | `USE_BEDS` |
| `WORKSTATIONS` | `USE_WORKSTATIONS` |
| `LECTERNS` | `USE_LECTERNS` |
| `BELLS` | `USE_BELLS` |

### ChunkPosition

Immutable chunk coordinate with utility methods.

| Field | Type | Description |
|-------|------|-------------|
| `world` | String | World name |
| `x` | int | Chunk X |
| `z` | int | Chunk Z |

| Method | Returns | Description |
|--------|---------|-------------|
| `getNeighbors(boolean)` | `List` | Get adjacent chunks (optional diagonals) |
| `toChunk(World)` | `Chunk` | Get Bukkit Chunk object |

## GUI Framework

### CustomGui

Base GUI runtime. Implements `InventoryHolder` for reliable event routing.

```java
// Create a GUI
CustomGui gui = new CustomGui(Component.text("My GUI"), 3); // 3 rows

// Add items with click handlers
gui.setItem(0, itemStack, (player, event) -> {
    // Handle click
});

// Open for player (thread-safe — schedules to main if async)
gui.open(player);
```

### PaginatedGui
Extends `CustomGui` with page-based navigation. Automatically adds prev/next buttons.

### SlotDefinition
Maps structure characters to items + actions for declarative GUI layouts.

```java
SlotDefinition filler = new SlotDefinition(fillItem, null);
SlotDefinition button = new SlotDefinition(buttonItem, clickAction);

Map<Character, SlotDefinition> ingredients = Map.of(
    'F', filler,
    'B', button
);

gui.fillFromStructure(new String[]{
    "F F F F F F F F F",
    "F . B . . . B . F",
    "F F F F F F F F F"
}, ingredients);
```

## Command Interface

### LandClaimCommand
All commands implement this interface:

```java
public interface LandClaimCommand {
    void register(PaperCommandManager<Source> manager, Command.Builder<PlayerSource> claimBuilder);
}
```

Commands are registered modularly via `CommandHandler`.

### Combat Preprocessor
All commands share a preprocessor that blocks execution during combat:
```java
if (combatManager.isInCombat(player)) {
    throw new CombatBlockedException();
}
```
